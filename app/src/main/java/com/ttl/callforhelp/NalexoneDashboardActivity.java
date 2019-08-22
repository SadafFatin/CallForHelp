package com.ttl.callforhelp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ttl.callforhelp.model.OpioidMarkers;
import com.ttl.callforhelp.model.OpioidRequest;
import com.ttl.callforhelp.model.User;
import com.ttl.callforhelp.util.MyPreference;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class NalexoneDashboardActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    //User mngmnt
    MyPreference myPreference;
    User user;

    //Status
    String requestStatus = "Help others";

    //Map and Location
    private LocationTracker tracker;
    public Location myLocation;
    private Marker myPosition;
    private GoogleMap mMap;
    private List<OpioidMarkers> opioidUsers = new ArrayList<>();


    //Firebase and firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collectionRef = db.collection("user_request");





    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naloxone_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);

        //location
        locationListener();


        //user mngmnt
        myPreference = MyPreference.getInstance(this);
        user = myPreference.getCurrentUser();

        //listen for db changes
        this.listenForNewSosRequests(true);


        toolbar.setTitle(" Welcome " + user.getName());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSnackBar(" Hi ");
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (tracker != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
            tracker.startListening();
        }
    }

    @Override
    protected void onPause() {
        tracker.stopListening();
        super.onPause();
    }

    //Map callback and marker add methods
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        addMyMarker();
    }

    private void addMyMarker() {
        if (myLocation != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            myPosition = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                    .title(" Me ").snippet(requestStatus));
            myPosition.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition.getPosition(), 16));
        } else {
            showSnackBar("Sorry coundn't find your location");
        }

    }


    //location tracking methods
    public void locationOn(Location location) {
        this.myLocation = location;
        showLog("found" + location.getLatitude() + location.getLongitude());
        if(mMap!=null){
            addMyMarker();
        }


    }

    void locationListener() {
        TrackerSettings settings =
                new TrackerSettings()
                        .setUseGPS(true)
                        .setUseNetwork(true)
                        .setUsePassive(true)
                        .setTimeBetweenUpdates(30 * 1000)
                        .setMetersBetweenUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showSnackBar("Location Permission required to operate");
        } else {
            tracker = new LocationTracker(getApplicationContext(), settings) {
                @Override
                public void onLocationFound(Location location) {
                    // Do some stuff
                    if (location != null) {
                        locationOn(location);
                    }
                }
                @Override
                public void onTimeout() {
                }
            };
            tracker.startListening();
        }

    }


    // my custom methods
    private void showSnackBar(String message) {
        View v = findViewById(R.id.fab);
        Snackbar.make(v, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void showLog(String msg) {
        Log.d(" Custom Log Msg ", msg);
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    // View click methods
    public void findSosRequests(View view) {
        db.collection("user_request").document();

    }


    // Request and Response methods

    private void listenForNewSosRequests(boolean b) {
        DocumentReference docRef = collectionRef.document(user.getEmail());
        collectionRef.whereEqualTo("isSolved",false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {

                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            showLog(" Firebase Listen Failed");
                            return;
                        }

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                            OpioidMarkers opioidMarkers = generateOpioidMarker(doc);


                        }
                    }
                });



    }

    private OpioidMarkers generateOpioidMarker(QueryDocumentSnapshot doc) {

        OpioidRequest opioidRequest = doc.toObject(OpioidRequest.class);
        //Marker marker = new Marker();
        return  null;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        }
        if (id == R.id.nav_profile) {
            startActivity(new Intent(NalexoneDashboardActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            myPreference.clearAll();
                            startActivity(new Intent(NalexoneDashboardActivity.this, SpalshTutorial.class));
                            finish();
                        }
                    });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }





}
