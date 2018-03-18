package com.sethphat.seth_googlemap_location.HttpRequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import java.net.URL;

/**
 * Created by Seth Phat on 3/18/2018.
 */

public class ImageHelper extends AsyncTask<Object, Void, Void> {

    Marker marker;
    Bitmap bmp;

    @Override
    protected Void doInBackground(Object... objects) {
        marker = (Marker) objects[0];
        URL imgUrl = (URL) objects[1];

        // load now
        try
        {
            bmp = BitmapFactory.decodeStream(imgUrl.openConnection().getInputStream());
        }
        catch (Exception e) {
            bmp = null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (bmp != null)
        {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(bmp));
        }
    }
}
