package com.ttl.callforhelp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.ttl.callforhelp.model.OpioidMarkers;
import com.ttl.callforhelp.model.OpioidRequest;
import com.ttl.callforhelp.model.User;
import com.ttl.callforhelp.model.UserLocation;
import com.ttl.callforhelp.util.MyInfoWindowAdapter;
import com.ttl.callforhelp.util.MyPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class NalexoneDashboardActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    //User mngmnt
    MyPreference myPreference;
    User user;

    //Status
    String requestStatus = "Help others";
    boolean isAcceptedRequest = false;

    //Map and UserLocation
    private LocationTracker tracker;
    public Location myLocation;
    private Marker myPosition;
    private GoogleMap mMap;


    //Firebase and firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference user_requestRef = db.collection("user_request");
    CollectionReference locationRef = db.collection("location");


    //Opioid Request

    private List<OpioidMarkers> currentOpioidMarkers;


    //view
    TextView statView;
    FancyGifDialog.Builder dailogBuilder;
    private FusedLocationProviderClient fusedLocationClient;
    private Button cancelButton;
    private String helpedUseremail = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_naloxone_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        statView = findViewById(R.id.stat);

        //location
        locationListener();


        //user mngmnt
        myPreference = MyPreference.getInstance(this);
        user = myPreference.getCurrentUser();

        //listen for db changes
        currentOpioidMarkers = new ArrayList<>();
        this.listenForNewSosRequests(true);

        //view and others
        dailogBuilder = new FancyGifDialog.Builder(this);
        cancelButton = findViewById(R.id.respCancel);
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


        View v = View.inflate(this,R.layout.nav_header_opioid_dashboard,navigationView);

        TextView email = v.findViewById(R.id.email);
        email.setText(user.getEmail());

        TextView address = v.findViewById(R.id.subtitle_view);
        address.setText(user.getAddress());

        ImageView imageView = v.findViewById(R.id.imageView);
        Glide.with(this).load(R.drawable.dummy_user).into(imageView);

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
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter(NalexoneDashboardActivity.this));
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
            if (myPosition != null) {
                myPosition.remove();
            }
            myPosition = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(" Me ").snippet(requestStatus));
            myPosition.showInfoWindow();
            myPosition.setTag(user.getEmail());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition.getPosition(), 16));
        } else {
            showSnackBar("Sorry coundn't find your location");
        }

    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        if (!marker.getTag().equals(user.getEmail()) && !isAcceptedRequest) {
            OpioidMarkers opioidMarkers = (OpioidMarkers) marker.getTag();
            showDailog(opioidMarkers);
        }
        else {
            showToast("You are already helping someone");
        }

    }

    //location tracking methods
    public void locationOn(Location location) {
        this.myLocation = location;
        showLog("found" + location.getLatitude() + location.getLongitude());
        if (mMap != null) {
            addMyMarker();
        }
        if (isAcceptedRequest) {
            UserLocation currentLoc = new UserLocation(location.getLatitude(), location.getLongitude());
            locationRef.document(user.getEmail()).set(currentLoc);
        }


    }
    public void locationListener() {
        TrackerSettings settings =
                new TrackerSettings()
                        .setUseGPS(true)
                        .setUseNetwork(true)
                        .setUsePassive(true)
                        .setTimeBetweenUpdates(30 * 1000)
                        .setMetersBetweenUpdates(3);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showSnackBar("UserLocation Permission required to operate");
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


    // Request and Response methods
    private void listenForNewSosRequests(boolean b) { findOutAllRequest(); }
    private void findOutAllRequest() {
        user_requestRef.whereEqualTo("isSolved", false).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    showLog(" Firebase Listen Failed");
                    return;
                }
                int count = 0;
                if (mMap != null) {
                    for (OpioidMarkers opioidMarkers : currentOpioidMarkers) {
                        opioidMarkers.getMarker().remove();
                    }
                }
                count = currentOpioidMarkers.size();
                currentOpioidMarkers.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    OpioidMarkers opioidMarker = generateOpioidMarker(doc);
                    if(opioidMarker!=null){
                        currentOpioidMarkers.add(opioidMarker);
                    }
                }

                int newCount = currentOpioidMarkers.size() - count;
                if (newCount > 0) {
                    if (newCount != currentOpioidMarkers.size()) {
                        statView.setText("Requesting Opioid " + currentOpioidMarkers.size() + " new " + newCount);

                    } else {
                        statView.setText("Requesting Opioid " + currentOpioidMarkers.size());
                    }
                    playNewRequestsSound();
                } else {
                    statView.setText("Requesting Opioid " + currentOpioidMarkers.size());
                }


            }
        });

    }
    private OpioidMarkers generateOpioidMarker(QueryDocumentSnapshot doc) {
        OpioidRequest opioidRequest = doc.toObject(OpioidRequest.class);
        if(mMap!=null){
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(opioidRequest.getLatitude(), opioidRequest.getLongitude()))
                    .title(opioidRequest.getUser().getName()).snippet("is requesting"));
            marker.showInfoWindow();

            OpioidMarkers opioidMarkers = new OpioidMarkers(opioidRequest, marker);
            marker.setTag(opioidMarkers);
            return opioidMarkers;
        }
        return null;
    }
    public void respondingOrNot(View view) {
        if(isAcceptedRequest){
            final HashMap<String, Object> map = new HashMap<>();
            map.put("acceptedUserId", null);
            map.put("isSolved", false);
            map.put("solved", false);
            dailogBuilder.setTitle("Sure you want to canclel Help to" + helpedUseremail)

                    .setNegativeBtnText("Cancel")
                    .setPositiveBtnBackground("#FF4081")
                    .setPositiveBtnText("Ok")
                    .setNegativeBtnBackground("#FFA9A7A8")
                    .setGifResource(R.drawable.help)   //Pass your Gif here
                    .isCancellable(true)
                    .OnPositiveClicked(new FancyGifDialogListener() {
                        @Override
                        public void OnClick() {

                            user_requestRef.document(helpedUseremail).update(map);
                            isAcceptedRequest = false;
                            cancelButton.setText("SOS Requests");
                            locationRef.document(user.getEmail()).delete();
                        }
                    })
                    .OnNegativeClicked(new FancyGifDialogListener() {
                        @Override
                        public void OnClick() {
                            Toast.makeText(NalexoneDashboardActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .build();
        }

    }


    // my custom methods
    private void playNewRequestsSound() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_edit_location);
        mBuilder.setContentTitle("New sos requests around you");
        mBuilder.setContentText(" Find the requested users on map");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("Alert", "alertCh", NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        mNotificationManager.notify(001, mBuilder.build());
        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.sound);
        CountDownTimer cntr_aCounter = new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                mp.start();
            }
            public void onFinish() {
                mp.stop();
            }
        };
        cntr_aCounter.start();
    }
    private void showSnackBar(String message) {
        View v = findViewById(R.id.fab);
        Snackbar.make(v, message, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void showLog(String msg) {
        Log.d(" Custom Log Msg ", msg);
    }

    private void showToast(String s) {
        Toast.makeText(NalexoneDashboardActivity.this, s, Toast.LENGTH_LONG).show();
    }

    private float getDistance(Location destination) {
        return (myLocation.distanceTo(destination) / 1000);
    }

    private void showDailog(final OpioidMarkers opioidMarkers) {
        Location dest = new Location("");
        dest.setLongitude(opioidMarkers.getRequest().getLongitude());
        dest.setLatitude(opioidMarkers.getRequest().getLatitude());

        final HashMap<String, Object> map = new HashMap<>();
        map.put("acceptedUserId", user.getEmail());
        map.put("isSolved", true);
        map.put("solved", true);
        dailogBuilder.setTitle(" Want to Help " + opioidMarkers.getRequest().getUser().getName())
                .setMessage(" He is " + getDistance(dest) + " km away from you and your naloxone is 5km away from you. ")
                .setNegativeBtnText("Cancel")
                .setPositiveBtnBackground("#FF4081")
                .setPositiveBtnText("Ok")
                .setNegativeBtnBackground("#FFA9A7A8")
                .setGifResource(R.drawable.help)   //Pass your Gif here
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        helpedUseremail = opioidMarkers.getRequest().getUser().getEmail();
                        user_requestRef.document(helpedUseremail).update(map);
                        isAcceptedRequest = true;
                        UserLocation currentLoc = new UserLocation(myLocation.getLatitude(), myLocation.getLongitude());
                        locationRef.document(user.getEmail()).set(currentLoc);
                        requestStatus = " You are helping "+helpedUseremail;
                        cancelButton.setText("Cancel Help");
                        myPosition.setSnippet("You are helping "+helpedUseremail);
                        myPosition.showInfoWindow();


                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        Toast.makeText(NalexoneDashboardActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();


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
                            startActivity(new Intent(NalexoneDashboardActivity.this, RegisterTutorial.class));
                            finish();
                        }
                    });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
