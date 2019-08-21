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
    private ProgressDialog progressDialog;



    //a Uri object to store file path
    private Uri filePath;
    private StorageReference mStorageRef;
    private boolean isImageUploaded = false;

    User user;
    FirebaseFirestore db;
    private String useraddress;
    private LocationTracker tracker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        locationListener();

        myPreference = MyPreference.getInstance(this);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();
        user = new User(myPreference.getUserType(), myPreference.getUserName(), myPreference.getUserEmail(), null, null, null,null);


        detail = findViewById(R.id.detail);
        imageView = findViewById(R.id.imageView);
        address = findViewById(R.id.address);
        address.setText(myPreference.getUserAddress());
        progressDialog = new ProgressDialog(this);

        String text="";

        if (user.getType().equals(ReferenceTerms.naloxone)) {
            text =   "Welcome " + user.getName() +".Please provide the address where you keep your Naloxone and upload a picture to complete your profile..";
        } else {
            text = "Welcome "+ user.getName()+ ",please provide your address and profile picture to complete your profile.";
        }
        detail.setText(text);


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

    // view onclick methods
    public void choose(View view) {
        showFileChooser();
    }

    public void upload(View view) {
        uploadFile();
    }

    public void pickaplace(View view) { }

    public void completeProfile(View view) {
        this.updateProfileData(progressDialog);
    }



    //Custom Methods

    //method to show file chooser
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    //this method will controll upload logic
    private void uploadFile() {
        //if there is a file to upload
        if (filePath != null && address.getText().toString() != "") {
            //displaying a progress dialog while upload is going on
            uploadForUser();
        }
        //if there is not any file
        else {
            showToast("Please choose an image or fillup properaddress");
        }
    }

    private void uploadForUser() {

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
                                isImageUploaded = true;
                                user.setImageUri(uri.toString());
                                progressDialog.dismiss();

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

    private void updateProfileData(final ProgressDialog progressDialog) {
        if( isImageUploaded==false){
            showToast(" Please Upload Image First");
        }
        else if(user.getLat()==null || user.getLon()==null || user.getAddress()==null){
            showToast("Could not determine your location and address.Please try again");
        }
        else{
            progressDialog.setTitle("Updating your profile information.");
            progressDialog.show();
            db.collection(user.getType()).document(user.getEmail()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    showToast(" Your Profile is updated");
                    myPreference.savePreferences(user);
                    if(user.getType().equals(ReferenceTerms.opioid)){
                        startActivity(new Intent(CompleteProfileActivity.this, OpioidDashboardActivity.class));
                    }
                    else {
                        startActivity(new Intent(CompleteProfileActivity.this, NalexoneDashboardActivity.class));
                    }
                }
            });

        }

    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private void locationOn(Location location) {

        lati = location.getLatitude();
        longi = location.getLongitude();
        useraddress = EasyWayLocation.getAddress(this, lati, longi, false, true);
        user.setLon(String.valueOf(longi));
        user.setLat(String.valueOf(lati));
        address.setText(useraddress);
        user.setAddress(useraddress);

    }

    private void locationListener() {
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
