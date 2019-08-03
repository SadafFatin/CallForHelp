package com.ttl.callforhelp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.easywaylocation.EasyWayLocation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ttl.callforhelp.model.User;
import com.ttl.callforhelp.util.MyPreference;
import com.ttl.callforhelp.util.ReferenceTerms;

import java.io.IOException;


import fr.quentinklein.slt.LocationTracker;
import fr.quentinklein.slt.TrackerSettings;

import static com.ttl.callforhelp.util.ReferenceTerms.PICK_IMAGE_REQUEST;

public class CompleteProfileActivity extends AppCompatActivity {

    private Double lati, longi;
    MyPreference myPreference;


    //ImageView
    private TextView address;
    private ImageView imageView;
    private TextView detail;


    //a Uri object to store file path
    private Uri filePath;
    private StorageReference mStorageRef;

    User user;
    FirebaseFirestore db;
    private String useraddress;
    private TextView prompt;
    private LocationTracker tracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        locationListener();

        myPreference = MyPreference.getInstance(this);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        user = new User(myPreference.getUserType(), myPreference.getUserName(), myPreference.getUserEmail(), "93.3", "20.4", myPreference.getUserAddress(), myPreference.getUserImgUri());


        detail = findViewById(R.id.detail);
        imageView = findViewById(R.id.imageView);
        address = findViewById(R.id.address);
        address.setText(myPreference.getUserAddress());
        prompt = findViewById(R.id.prompt);

        detail.setText(user.getName() + " type " + user.getType());
        if (myPreference.getUserType().equals(ReferenceTerms.naloxone)) {
            prompt.setText("Please take a picture of the place where you keep your Naloxone..");
        } else {
            prompt.setText("Please update your profile pic to help others recognize you.");
        }


    }

    public void choose(View view) {
        showFileChooser();
    }

    public void upload(View view) {
        uploadFile();
    }


    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //handling the image chooser activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //this method will upload the file
    private void uploadFile() {
        //if there is a file to upload
        if (filePath != null && address.getText().toString() != "") {
            //displaying a progress dialog while upload is going on
            uploadForOpioidUser();
        }
        //if there is not any file
        else {
            showToast("Please choose an image or fillup properaddress");
        }
    }

    private void uploadForOpioidUser() {

        user.setLat(String.valueOf(this.lati));
        user.setLon(String.valueOf(this.longi));
        user.setAddress(useraddress);

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading");
        progressDialog.show();
        final StorageReference riversRef = mStorageRef.child("images/" + user.getEmail() + user.getType());
        riversRef.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                user.setImageUri(uri.toString());
                                myPreference.setUserImgUri(uri.toString());
                                db.collection(user.getType()).document(user.getEmail()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        showToast(" Your Profile is updated");
                                        startActivity(new Intent(CompleteProfileActivity.this, DashboardActivity.class));
                                    }
                                });

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressDialog.dismiss();
                        showToast(exception.getMessage());
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    }
                });

    }


    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    public void locationOn(Location location) {

        lati = location.getLatitude();
        longi = location.getLongitude();
        useraddress = EasyWayLocation.getAddress(this, lati, longi, false, true);
        user.setLon(String.valueOf(longi));
        user.setLat(String.valueOf(lati));

        showToast("found" + useraddress + longi + lati);
        address.setText(useraddress);
        user.setAddress(useraddress);
        myPreference.setUserAddress(useraddress);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tracker != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
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

    void locationListener() {
        TrackerSettings settings =
                new TrackerSettings()
                        .setUseGPS(true)
                        .setUseNetwork(true)
                        .setUsePassive(true)
                        .setTimeBetweenUpdates(30 * 1000)
                        .setMetersBetweenUpdates(1);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "Permission required", Toast.LENGTH_SHORT).show();
        } else {
            tracker = new LocationTracker(getApplicationContext(),settings) {
                @Override
                public void onLocationFound(Location location) {
                    // Do some stuff
                    if(location!= null){
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
}
