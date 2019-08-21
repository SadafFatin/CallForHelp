package com.ttl.callforhelp.model;

import java.util.Date;
import java.util.List;

public class OpioidRequest {
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
