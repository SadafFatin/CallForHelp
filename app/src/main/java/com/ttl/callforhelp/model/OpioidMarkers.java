package com.ttl.callforhelp.model;


import com.google.android.gms.maps.model.Marker;

public class OpioidMarkers  {
    public OpioidRequest getRequest() {
        return request;
    }

    public void setRequest(OpioidRequest request) {
        this.request = request;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    OpioidRequest request;
    Marker marker;

    public OpioidMarkers( OpioidRequest request, Marker marker) {
        this.request = request;
        this.marker = marker;
    }
}
