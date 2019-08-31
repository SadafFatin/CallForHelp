package com.ttl.callforhelp.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

class RequestQueue {

    private  static RequestQueue instance;
    private com.android.volley.RequestQueue requestQueue;
    private Context ctx;

    private RequestQueue(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized RequestQueue getInstance(Context context) {
        if (instance == null) {
            instance = new RequestQueue(context);
        }
        return instance;
    }

    public com.android.volley.RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
