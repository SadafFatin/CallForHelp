package com.ttl.callforhelp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.ttl.callforhelp.util.ReferenceTerms.PICK_IMAGE_REQUEST;

public class CompleteProfileActivity extends AppCompatActivity {

    //location
    Location myLocation;
    private FusedLocationProviderClient client;
    private LocationManager locationManager;

    //isLocationEnabled


    //View
    private EditText address,phoneNum;
    private ImageView imageView;
    private TextView detail;
    private ProgressDialog progressDialog;
    LinearLayout linearLayout;
    Button skip;


    // Uri object to store file path
    private Uri filePath;
    private boolean isImageUploaded = false;
    User user;
    MyPreference myPreference;
    private String useraddress,userphnNum;

    //firebase
    FirebaseFirestore db;
    private StorageReference mStorageRef;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        requestLocationandAddress();
        myPreference = MyPreference.getInstance(this);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        db = FirebaseFirestore.getInstance();

        user = new User(myPreference.getUserType(), myPreference.getUserName(), myPreference.getUserEmail(), null, null, null, null,null);

        linearLayout = findViewById(R.id.imageLyt);
        detail = findViewById(R.id.detail);
        imageView = findViewById(R.id.imageView);
        address = findViewById(R.id.address);
        address.setText("");
        skip = findViewById(R.id.skip);
        progressDialog = new ProgressDialog(this);
        phoneNum = findViewById(R.id.phnNum);

        String text = "";
        if (user.getType().equals(ReferenceTerms.naloxone)) {
            text = "Welcome " + user.getName() + ".Please provide the address where you keep your Naloxone and upload a picture to complete your profile..";
        } else {
            text = "Welcome " + user.getName() + ",please provide your address and Phone Number and Profile Image.";
            skip.setVisibility(View.VISIBLE);
        }
        detail.setText(text);


    }

    @SuppressLint("MissingPermission")
    private void requestLocationandAddress() {
        client = LocationServices.getFusedLocationProviderClient(this);
        client.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
               if(location!=null){
                   myLocation = location;
                   getAddress(myLocation);
               }
            }
        });
    }

    private void getAddress(Location myLocation) {
        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(
                    myLocation.getLatitude(),
                    myLocation.getLongitude(),
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            showToast(ioException.getLocalizedMessage());
            useraddress = "Please prove the address";
        } catch (IllegalArgumentException illegalArgumentException) {
            useraddress = "Please prove the address";
            showToast(illegalArgumentException.getLocalizedMessage());
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            showToast("Could not find the address");
            useraddress = "Could not find the address";
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            useraddress = TextUtils.join(System.getProperty("line.separator"),
                    addressFragments);
        }

        user.setLon(String.valueOf(myLocation.getLongitude()));
        user.setLat(String.valueOf(myLocation.getLatitude()));
        address.setText(useraddress);
        user.setAddress(useraddress);

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
    public void pickaplace(View view) {
    }
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
            uploadProfileImage();
        }
        //if there is not any file
        else {
            showToast("Please choose an image or fillup properaddress");
        }
    }

    private void uploadProfileImage() {
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
                                showToast("Image uploaded successfully");
                                progressDialog.dismiss();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        isImageUploaded = false;
                        progressDialog.dismiss();
                        showToast(exception.getMessage());
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploading " + ((int) progress) + "%...");
                    }
                });

    }

    private void updateProfileData(final ProgressDialog progressDialog) {

        user.setPhoneNum(phoneNum.getText().toString());
        if(user.getType().equals(ReferenceTerms.naloxone)){
            if(user.getPhoneNum().length()<5){
                showToast(" Please Provide your phone number");
            }
            else if (isImageUploaded == false) {
                showToast(" Please Upload Image First");
            } else if (user.getLat() == null || user.getLon() == null || user.getAddress() == null ) {
                showToast("Could not determine your location and address.Please try again");
            } else {
                upLoadUserData(progressDialog);
            }
        }
        else {
            upLoadUserData(progressDialog);
        }
    }

    private void upLoadUserData(final ProgressDialog progressDialog) {

        db.collection(user.getType()).document(user.getEmail()).set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                progressDialog.dismiss();
                showToast(" Your Profile is updated");
                myPreference.savePreferences(user);
                if (user.getType().equals(ReferenceTerms.opioid)) {
                    startActivity(new Intent(CompleteProfileActivity.this, OpioidDashboardActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(CompleteProfileActivity.this, NalexoneDashboardActivity.class));
                    finish();
                }
            }
        });
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    public void skipNow(View view) {

        updateProfileData(progressDialog);
    }
}
