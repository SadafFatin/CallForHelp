package com.ttl.callforhelp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.AvoidType;
import com.akexorcist.googledirection.constant.TransitMode;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Info;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.ttl.callforhelp.model.OpioidRequest;
import com.ttl.callforhelp.model.User;
import com.ttl.callforhelp.model.UserLocation;
import com.ttl.callforhelp.util.MyPreference;

import java.util.ArrayList;

import javax.annotation.Nullable;

import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

public class OpioidDashboardActivity extends FragmentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    //User mngmnt
    MyPreference myPreference;
    User user, naloxoneProvider;
    //Status
    private String requestStatus = " Need naloxone ?";
    private boolean isRequested = false;
    //Map and UserLocation
    private LocationTracker tracker;
    public Location myLocation;
    private GoogleMap mMap;
    private Marker myPosition;
    private Marker naloxoneProviderMarker;
    private Marker naloxoneProviderHomeMarker;
    //Firebase and firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference user_requestRef = db.collection("user_request");
    CollectionReference locationRef = db.collection("location");
    CollectionReference naloxoneRef = db.collection("naloxone");
    //views
    FancyGifDialog.Builder dailogBuilder;
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isAnswered = false;
    TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opioid_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);

        //location
        locationListener();

        //user mngmnt
        myPreference = MyPreference.getInstance(this);
        user = myPreference.getCurrentUser();

        dailogBuilder = new FancyGifDialog.Builder(this);
        status = findViewById(R.id.stat);
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

        View v = View.inflate(this, R.layout.nav_header_opioid_dashboard, navigationView);

        TextView email = v.findViewById(R.id.email);
        email.setText(user.getEmail());

        TextView address = v.findViewById(R.id.subtitle_view);
        address.setText(user.getAddress());

        ImageView imageView = v.findViewById(R.id.imageView);
        Glide.with(this).load(myPreference.getUserImgUri()).into(imageView);

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

            if (myPosition != null) {
                myPosition.remove();
            }
            myPosition = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(" Me ").snippet(requestStatus));
            myPosition.showInfoWindow();

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition.getPosition(), 16));
        } else {
            showSnackBar("Sorry coundn't find your location");
        }

    }
    private void addNaloxoneProviderCurrentMarker(Location loc) {
        if (naloxoneProviderMarker != null) {
            naloxoneProviderMarker.remove();
        }
        naloxoneProviderMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(" Naloxone Provider ").snippet("on his way"));
        naloxoneProviderMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(naloxoneProviderMarker.getPosition(), 14));
        addRoutes(naloxoneProviderMarker , naloxoneProviderHomeMarker);
    }
    private void addNaloxoneProviderHomeMarker(Location loc) {
        if (naloxoneProviderHomeMarker != null) {
            naloxoneProviderHomeMarker.remove();
        }
        naloxoneProviderHomeMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title(" Naloxone is here ").snippet("provider has to get it first"));
        naloxoneProviderHomeMarker.showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(naloxoneProviderHomeMarker.getPosition(), 14));
        addRoutes(naloxoneProviderHomeMarker,myPosition);
    }

    //location tracking methods
    private void locationOn(Location location) {
        this.myLocation = location;
        showLog("found" + location.getLatitude() + location.getLongitude());
        if (mMap != null) {
            addMyMarker();
        }


    }
    private void locationListener() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showToast("You must provide location permission to this app");
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                locationOn(location);
                            }
                        }
                    });
        }
        TrackerSettings settings =
                new TrackerSettings()
                        .setUseGPS(true)
                        .setUseNetwork(true)
                        .setUsePassive(true)
                        .setTimeBetweenUpdates(30 * 1000)
                        .setMetersBetweenUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    // View click methods
    public void sendSosRequest(final View view) {
        final Button b = (Button) view;
        if (isRequested) {
            user_requestRef.document(user.getEmail()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    b.setText("SOS");
                    showSnackBar("Your Request has been cancelled");
                    isRequested = false;
                    requestStatus = " Need naloxone ?";
                    myPosition.setSnippet(requestStatus);
                    myPosition.showInfoWindow();
                }
            });
        } else {
            if (myLocation == null) {
                showSnackBar(" Sorry couldn't locate you.Failed to make request.");

            } else {
                OpioidRequest userRequest = new OpioidRequest(user, myLocation.getLatitude(), myLocation.getLongitude());
                user_requestRef.document(user.getEmail()).set(userRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showSnackBar(" Your SOS request has been sent successfully");
                        requestStatus = "Requested nalaxone";
                        myPosition.setSnippet(requestStatus);
                        myPosition.showInfoWindow();
                        isRequested = true;
                        b.setText("Cancel Request");
                        status.setText("Request has been made wating for acceptence");
                        listenForAcceptence(true);

                    }
                });
            }
        }


    }

    // Request and Response methods
    private void listenForAcceptence(boolean b) {
        final DocumentReference docRef = db.collection("user_request").document(user.getEmail());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    showLog("Listen failed.");
                    return;
                }
                if (snapshot != null && snapshot.exists()) {
                    showLog("Current data:");
                    OpioidRequest opioidRequest = snapshot.toObject(OpioidRequest.class);
                    //showToast("Changed " + opioidRequest.getSolved());
                    if (opioidRequest.getSolved()) {
                        showAcceptanceDailog(opioidRequest);
                    } else {
                        if (isAnswered) {
                            showSnackBar("Your request was cancelled");
                            isAnswered = false;
                            status.setText("Your request was cancelled by naloxone provider");
                        }

                    }
                } else {
                    showLog("Current data: null");
                }

            }
        });


    }
    private void showAcceptanceDailog(final OpioidRequest opioidRequest) {

        locationRef.document(opioidRequest.getAcceptedUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                dailogBuilder.setTitle(" A naloxone provider has answered")
                        .setNegativeBtnText("Cancel")
                        .setPositiveBtnBackground("#FF4081")
                        .setPositiveBtnText("Ok")
                        .setNegativeBtnBackground("#FFA9A7A8")
                        .setGifResource(R.drawable.help)   //Pass your Gif here
                        .isCancellable(true)
                        .OnPositiveClicked(new FancyGifDialogListener() {
                            @Override
                            public void OnClick() {
                                isAnswered = true;
                                putNaloxoneProviderHomeOnMap(opioidRequest);
                                status.setText("Help is on the way");
                            }
                        })
                        .OnNegativeClicked(new FancyGifDialogListener() {
                            @Override
                            public void OnClick() {

                            }
                        }).build();

            }
        });

    }
    private void putNaloxoneProviderHomeOnMap(final OpioidRequest opioidRequest) {
        naloxoneRef.document(opioidRequest.getAcceptedUserId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                naloxoneProvider = documentSnapshot.toObject(User.class);
                Location location = new Location("");
                location.setLatitude(Double.parseDouble(naloxoneProvider.getLat()));
                location.setLongitude(Double.parseDouble(naloxoneProvider.getLon()));
                addNaloxoneProviderHomeMarker(location);
                listenForNaloxoneLocChange(opioidRequest.getAcceptedUserId());
            }
        });
    }
    private void listenForNaloxoneLocChange(String acceptedUserId) {
        locationRef.document(acceptedUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                UserLocation naloxoneProviderCurrentLoc = documentSnapshot.toObject(UserLocation.class);
                Location location = new Location("");
                location.setLatitude(naloxoneProviderCurrentLoc.getLat());
                location.setLongitude(naloxoneProviderCurrentLoc.getLon());
                addNaloxoneProviderCurrentMarker(location);
            }
        });

    }

    private void addRoutes(Marker src,Marker dest) {
        GoogleDirection.withServerKey(OpioidDashboardActivity.this.getString(R.string.google_api_key))
                .from(new LatLng(src.getPosition().latitude, src.getPosition().longitude))
                .to(new LatLng(dest.getPosition().latitude, dest.getPosition().longitude))
                .avoid(AvoidType.FERRIES)
                .avoid(AvoidType.INDOOR)
                .transportMode(TransportMode.TRANSIT)
                .transitMode(TransitMode.BUS)
                .transitMode(TransitMode.SUBWAY)
                .transitMode(TransitMode.TRAM)
                .unit(Unit.METRIC)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            Route route = direction.getRouteList().get(0);
                            Leg leg = route.getLegList().get(0);
                            Info distanceInfo = leg.getDistance();
                            Info durationInfo = leg.getDuration();
                            String distance = distanceInfo.getText();
                            String duration = durationInfo.getText();
                            showSnackBar("Time " + duration + " distance " + distance);
                            status.setText(" Provider is "+distance+" km and "+duration+" min away");
                            ArrayList<LatLng> directionPositionList = leg.getDirectionPoint();
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(OpioidDashboardActivity.this, directionPositionList, 3, Color.rgb(92, 50, 168));
                            mMap.addPolyline(polylineOptions);

                        } else {
                            showSnackBar("Could not find route");
                        }
                    }
                    @Override
                    public void onDirectionFailure(Throwable t) {
                        showSnackBar("Could not find route");
                    }
                });

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
            startActivity(new Intent(OpioidDashboardActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            myPreference.clearAll();
                            startActivity(new Intent(OpioidDashboardActivity.this, SpalshTutorial.class));
                            finish();
                        }
                    });
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
