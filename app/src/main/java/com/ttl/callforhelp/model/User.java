package com.ttl.callforhelp.model;

public class User {


    public User(String type, String name, String email, String lat, String lon, String address, String imageUri) {
        this.type = type;
        this.name = name;
        this.email = email;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.imageUri = imageUri;
    }

    private  String type;
    private  String name;
    private String email;
    private String lat;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    private String lon;
    private String address;
    private String imageUri;





}
