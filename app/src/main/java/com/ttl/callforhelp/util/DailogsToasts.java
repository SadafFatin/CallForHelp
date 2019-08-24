package com.ttl.callforhelp.util;

import android.app.Activity;
import android.content.Context;

import com.shashank.sony.fancygifdialoglib.FancyGifDialog;

public class DailogsToasts {

    private static DailogsToasts dailogsToasts;
    private Context ctx;
    FancyGifDialog.Builder dailogBuilder;
    private DailogsToasts(Activity context){
        this.ctx = context;
        dailogBuilder = new FancyGifDialog.Builder(context);
    }

    public static DailogsToasts getInstance(Activity context) {
        if (dailogsToasts == null) {
            dailogsToasts = new DailogsToasts(context);
        }
        return dailogsToasts;
    }









}
