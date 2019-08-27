package com.ttl.callforhelp.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.ttl.callforhelp.R;

public class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private final View myContentsView;

    public MyInfoWindowAdapter(Context context) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        myContentsView = inflater.inflate(R.layout.custom_info_contents, null);
    }

    @Override
    public View getInfoContents(Marker marker) {

        TextView tvTitle = myContentsView.findViewById(R.id.title);
        tvTitle.setText(marker.getTitle());
        TextView tvSnippet = myContentsView.findViewById(R.id.snippet);
        tvSnippet.setText(marker.getSnippet());

        return myContentsView;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // TODO Auto-generated method stub
        return null;
    }
}