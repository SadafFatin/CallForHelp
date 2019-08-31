package com.ttl.callforhelp.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ttl.callforhelp.R;
import com.ttl.callforhelp.RegisterTutorial;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final String ADMIN_CHANNEL_ID ="admin_channel";
    //private MyPreference myPreference;
    private static final String TAG = "mFirebaseIIDService";
    private static final String SUBSCRIBE_TO = "RequestResponse";



    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        FirebaseMessaging.getInstance().subscribeToTopic(SUBSCRIBE_TO);
        Log.i(TAG, "onTokenRefresh completed with token: " + s);
        //myPreference.setFirebaseId(s);
    }





    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

     //if(remoteMessage!=null){
        // if(remoteMessage.getData().get("type").equals(ReferenceTerms.naloxone)) {
             final Intent intent = new Intent(this, RegisterTutorial.class);
             NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
             int notificationID = new Random().nextInt(3000);

      /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
      */
             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                 setupChannels(notificationManager);
             }

             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                     PendingIntent.FLAG_NO_CREATE);

             Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                     R.drawable.help);

             NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                     .setSmallIcon(R.drawable.ic_help)
                     .setLargeIcon(largeIcon)
                     .setContentTitle(remoteMessage.getData().get("title"))
                     .setContentText(remoteMessage.getData().get("message"))
                     .setAutoCancel(true)
                     .setSound(Uri.parse("android.resource://" + getPackageName() + "sound.mp3"))
                     .setContentIntent(pendingIntent);

             //Set notification color to match your app color template
             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                 notificationBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
             }
             notificationManager.notify(notificationID, notificationBuilder.build());
        // }






    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager){
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to devie notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}
