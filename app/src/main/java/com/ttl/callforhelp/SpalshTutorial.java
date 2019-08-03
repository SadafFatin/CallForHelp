package com.ttl.callforhelp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smarteist.autoimageslider.SliderLayout;
import com.smarteist.autoimageslider.SliderView;
import com.ttl.callforhelp.util.MyPreference;
import com.ttl.callforhelp.util.PermissionUtils;
import com.ttl.callforhelp.util.ReferenceTerms;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SpalshTutorial extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        PermissionUtils.PermissionResultCallback {

    SliderLayout sliderLayout;
    List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build());

    private String role = "";
    private MyPreference myPreference;


    ArrayList<String> permissions = new ArrayList<>();
    PermissionUtils permissionUtils;
    boolean isPermissionGranted;
    String address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_tutorial);

        myPreference = MyPreference.getInstance(this);

        permissionUtils = new PermissionUtils(this);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        //permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionUtils.check_permission(permissions, "Need GPS permission for getting your location", 1);





        FirebaseApp.initializeApp(this);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, DashboardActivity.class));
        }


        sliderLayout = findViewById(R.id.imageSlider);
        sliderLayout.setIndicatorAnimation(SliderLayout.Animations.FILL); //set indicator animation by using SliderLayout.Animations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderLayout.setScrollTimeInSec(5); //set scroll delay in seconds :


        setSliderViews();


    }




    public void opioidSignUp(View view) {
        this.signUp(ReferenceTerms.opioid);
    }

    public void naloxoneSignUp(View view) {
        this.signUp(ReferenceTerms.naloxone);
    }

    private void signUp(String role) {
        this.role = role;
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                ReferenceTerms.signInRequest);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ReferenceTerms.signInRequest) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                showLog(user.getDisplayName() + " " + user.getEmail() + " " + user.getPhoneNumber() + " " + user.getProviderId() + " "
                        + user.getMetadata() + " " + user.toString());
                setUserPreferences(user);
                if (isPermissionGranted) {
                    startActivity(new Intent(SpalshTutorial.this, CompleteProfileActivity.class));
                } else {
                    shoToast("You must turn on location and provide permission");
                }
            } else {
                shoToast("Failed To sign In");
            }
        }



    }

    private void showLog(String msg) {
        Log.d(" Custom Log Msg ", msg);
    }

    private void shoToast(String msg) {
        Toast.makeText(SpalshTutorial.this, msg, Toast.LENGTH_LONG).show();
    }




    private void setUserPreferences(FirebaseUser user) {
        myPreference.setUserName(user.getDisplayName());
        myPreference.setUserType(this.role);
        myPreference.setUserEmail(user.getEmail());
        myPreference.setUserImgUri(" ");
        myPreference.setUserAddress(this.address);
    }


    private void setSliderViews() {
        for (int i = 0; i < 4; i++) {
            SliderView sliderView = new SliderView(this);
            switch (i) {
                case 0:
                    sliderView.setImageDrawable(R.drawable.opioid_epidemic);
                    sliderView.setDescription("The user epidemic or user crisis is a term that generally refers to the rapid increase in the use of prescription and nonprescription user drugs.");
                    break;
                case 1:
                    sliderView.setImageDrawable(R.drawable.opioid_stats);
                    sliderView.setDescription("Every day, more than 130 people in the United States die after overdosing on opioids.");
                    break;
                case 2:
                    sliderView.setImageDrawable(R.drawable.naloxone);
                    sliderView.setDescription("Naloxone may be combined with an user (in the same pill) to decrease the risk of user misuse.");
                    break;
                case 3:
                    sliderView.setImageDrawable(R.drawable.opioids_map);
                    sliderView.setDescription("The overall objective of the project is to make naloxone more widely available for user drug users.");
                    break;


            }

            sliderView.setImageScaleType(ImageView.ScaleType.FIT_START);
            sliderView.setDescriptionTextSize(18);
            sliderView.setDescriptionTextColor(SpalshTutorial.this.getResources().getColor(R.color.colorDescription));
            sliderLayout.addSliderView(sliderView);
        }
    }


    @Override
    public void PermissionGranted(int request_code) {
        isPermissionGranted = true;
        shoToast(""+isPermissionGranted);
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        isPermissionGranted = false;
    }

    @Override
    public void PermissionDenied(int request_code) {
        isPermissionGranted = false;
    }

    @Override
    public void NeverAskAgain(int request_code) {
        isPermissionGranted = false;
    }


    public void login(View view) {

    }
}
