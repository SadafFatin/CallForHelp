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
    private String isAnswered = "isAnswered";
    private String isRequested = "isRequested";
    private String firebaseId = "firebaseId";

    public boolean getIsProfileComplete() {
        return this.sharedPreferences.getBoolean(isProfileComplete,false);
    }

    public void setIsProfileComplete(boolean isthisProfileComplete) {
        this.preferenceEditor.putBoolean(isProfileComplete,isthisProfileComplete).apply();
    }

    public boolean getIsAnswered() {
        return this.sharedPreferences.getBoolean(isAnswered,false);
    }

    public void setIsAnswered(boolean isAns) {
        this.preferenceEditor.putBoolean(isAnswered,isAns).apply();
    }

    public boolean getIsRequested() {
        return this.sharedPreferences.getBoolean(isRequested,false);
    }

    public void setIsRequested(boolean isReq) {
        this.preferenceEditor.putBoolean(isRequested,isReq).apply();
    }





    public String getFirebaseId() {
        return sharedPreferences.getString(firebaseId,"");
    }

    public void setFirebaseId(String uri) {
        this.preferenceEditor.putString(firebaseId,uri).apply();
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

    public String getUserPhnNum() {
        return sharedPreferences.getString(userPhn,"");
    }

    public void setUserPhnNum(String usersPhn) {
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
        instance.setUserImgUri("");
        instance.setUserName("");
        instance.setUserEmail("");
        instance.setIsProfileComplete(false);
        instance.setUserAddress("");
        instance.setUserType("");
    }

    public void savePreferences(User user) {
        instance.setUserImgUri(user.getImageUri());
        instance.setUserName(user.getName());
        instance.setUserEmail(user.getEmail());
        instance.setIsProfileComplete(true);
        instance.setUserAddress(user.getAddress());
        instance.setUserType(user.getType());
        instance.setUserPhnNum(user.getPhoneNum());
    }

    public User getCurrentUser() {
        User user = new User(instance.getUserType(), instance.getUserName(), instance.getUserEmail(),null,null, instance.getUserAddress(), instance.getUserImgUri(), instance.getUserPhnNum());
        return user;
    }
}