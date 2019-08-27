package com.ttl.callforhelp.model;

public class UserLocation {

    Double lat,lon;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public UserLocation(Double lat, Double lon) {
        this.lat = lat;
        this.lon = lon;
    }


    public UserLocation() {
    }
}
