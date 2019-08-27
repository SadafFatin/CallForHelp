package com.ttl.callforhelp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import com.ttl.callforhelp.R;

public class CheckIfLocationEnabled {
    Context context;
    Activity activity;
    LocationManager locationManager;
    boolean gps_enabled = false;
    boolean network_enabled = false;
    FancyGifDialog.Builder fancyDailogBuilder;

    public CheckIfLocationEnabled(Context context, Activity activity) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.activity = activity;
        fancyDailogBuilder = new FancyGifDialog.Builder(this.activity);
    }

    public boolean checkIfLocationEnablde() {
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gps_enabled && network_enabled;
    }

    public void locationTurnOnDailog() {
        fancyDailogBuilder.
                setGifResource(R.drawable.connectio).setTitle("You must turn on your location to use this app").
                setNegativeBtnText("Cancel")
                .isCancellable(false)
                .setPositiveBtnText("Turn On location")
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        activity.finish();
                    }
                }).OnPositiveClicked(new FancyGifDialogListener() {
            @Override
            public void OnClick() {
                context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).build();
    }


}
