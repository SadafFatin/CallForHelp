package com.ttl.callforhelp.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.ttl.callforhelp.model.OpioidRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseSendNotification {

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAuEBSmpU:APA91bGjKG9MENwwDsP8M3Hsof1l91hkE3UR5zC-KnSEjkqDEpyOcwKq0QljE0hRSElXKD8yYgsz6f5agpST1QcZyOF55M5gY1YljpI6hXzn1ltS5TB7jzWLZZtCMX21s4tMkXtSkJ8b";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;
    Context context;
    OpioidRequest opioidRequest;

    public FirebaseSendNotification(Context context, OpioidRequest opioidRequest) {
        this.context = context;
        this.opioidRequest = opioidRequest;
    }

    public void sendNotification(){
        TOPIC = "/topics/RequestResponse"; //topic must match with what the receiver subscribed to
        NOTIFICATION_TITLE = opioidRequest.getUser().getName()+" is requesting for naloxone";
        NOTIFICATION_MESSAGE = "Help him if you can.";

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", NOTIFICATION_TITLE);
            notifcationBody.put("message", NOTIFICATION_MESSAGE);
            notifcationBody.put("click_action" , ".RegisterTutorial");
            notifcationBody.put("type",opioidRequest.getUser().getType());
            notification.put("to", TOPIC);
            notification.put("data", notifcationBody);
            notification.put("notification",notifcationBody);

        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage() );
        }
        notify(notification);
    }

    private void notify(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        RequestQueue.getInstance(context.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

}
