package com.ttl.callforhelp.model;

import java.util.Date;

public class OpioidRequest {
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAcceptedUserId() {
        return acceptedUserId;
    }

    public void setAcceptedUserId(String acceptedUserId) {
        this.acceptedUserId = acceptedUserId;
    }

    public Boolean getSolved() {
        return isSolved;
    }

    public void setSolved(Boolean solved) {
        isSolved = solved;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public User user;
    public Double latitude;
    public Double longitude;
    public String acceptedUserId;
    public Boolean isSolved= false;
    public String timestamp;

    public OpioidRequest() {
    }

    public OpioidRequest(User user, Double latitude, Double longitude) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = new Date().toString();
    }
}
