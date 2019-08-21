package com.ttl.callforhelp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ttl.callforhelp.model.User;

public class MyPreference {
    private static MyPreference instance;
    private Editor preferenceEditor;
    private SharedPreferences sharedPreferences;


    private String userName = "userName";
    private String userEmail = "userEmail";
    private String userPhn = "userPhn";
    private String userType = "userType";
    private String userAddress = "userAddress";
    private String userImgUri = "userImgUri";
    private String isProfileComplete = "isProfileComplete";
    private String isLoggedIn = "isLoggedIn";




    public boolean getIsProfileComplete() {
        return this.sharedPreferences.getBoolean(isProfileComplete,false);
    }

    public void setIsProfileComplete(boolean isthisProfileComplete) {
        this.preferenceEditor.putBoolean(isProfileComplete,isthisProfileComplete).apply();
    }



    public String getUserImgUri() {
        return sharedPreferences.getString(userImgUri,"");
    }

    public void setUserImgUri(String uri) {
        this.preferenceEditor.putString(userImgUri,uri).apply();
    }



    public String getUserName() {
        return sharedPreferences.getString(userName,"");
    }

    public void setUserName(String usersName) {
        this.preferenceEditor.putString(userName,usersName).apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(userEmail,"");
    }

    public void setUserEmail(String usersEmail) {
        this.preferenceEditor.putString(userEmail,usersEmail).apply();
    }

    public String getUserPhn() {
        return sharedPreferences.getString(userPhn,"");
    }

    public void setUserPhn(String usersPhn) {
        this.preferenceEditor.putString(userPhn,usersPhn).apply();
    }

    public String getUserType() {
        return sharedPreferences.getString(userType,"");
    }

    public void setUserType(String usersType) {
        this.preferenceEditor.putString(userType,usersType).apply();
    }

    public String getUserAddress() {
        return sharedPreferences.getString(userAddress,"");
    }

    public void setUserAddress(String usersAddress) {
        this.preferenceEditor.putString(userAddress,usersAddress).apply();
    }








    

    private MyPreference(Context context) {
        this.sharedPreferences = context.getSharedPreferences("Pref", 0);
        this.preferenceEditor = this.sharedPreferences.edit();
    }

    public static MyPreference getInstance(Context context) {
        if (instance == null) {
            instance = new MyPreference(context);
        }
        return instance;
    }

    public void clearAll() {
        this.instance.setUserImgUri("");
        this.instance.setUserName("");
        this.instance.setUserEmail("");
        this.instance.setIsProfileComplete(false);
        this.instance.setUserAddress("");
        this.instance.setUserType("");
    }

    public void savePreferences(User user) {
        this.instance.setUserImgUri(user.getImageUri());
        this.instance.setUserName(user.getName());
        this.instance.setUserEmail(user.getEmail());
        this.instance.setIsProfileComplete(true);
        this.instance.setUserAddress(user.getAddress());
        this.instance.setUserType(user.getType());
    }

    public User getCurrentUser() {
        User user = new User(this.instance.getUserType(),this.instance.getUserName(),this.instance.getUserEmail(),null,null,this.instance.getUserAddress(),this.instance.getUserImgUri());
        return user;
    }
}