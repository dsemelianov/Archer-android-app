package com.archer.android;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

/**
 * Created by davidemelianov on 2/5/16.
 */
public class Cache {

    private static Cache mCache;
    private Context mContext;
    private Location mLocation;
  //  private SharedPreferences mShared;

    private Cache(Context c) {
        mContext = c;
        mLocation = new Location("cached");
        mLocation.setLatitude(0f);
        mLocation.setLongitude(0f);
        Toast.makeText(mContext, "cleared", Toast.LENGTH_LONG).show();

 //       mShared = mContext.getSharedPreferences("shared", mContext.MODE_PRIVATE);
    }

    public static Cache getInstance(Context c) {

        if (mCache == null) {
             mCache = new Cache(c);
        }
        return mCache;
    }

    public void changeLocation(double lat, double lon){

       // SharedPreferences.Editor editor = mShared.edit();
       // editor.putFloat("longitude", (float) lon);
       // editor.putFloat("latitude", (float) lat);
       // editor.apply();
       // mShared = mContext.getSharedPreferences("shared", mContext.MODE_PRIVATE);
        mLocation.setLatitude(lat);
        mLocation.setLongitude(lon);

        Toast.makeText(mContext, "r lat " + String.valueOf(mLocation.getLatitude()), Toast.LENGTH_LONG).show();
        Toast.makeText(mContext, "r lon " + String.valueOf(mLocation.getLongitude()), Toast.LENGTH_LONG).show();
    }

    public Location getLocation() {
        if ((mLocation.getLongitude() == 0f) || (mLocation.getLatitude() == 0f)) {
            Toast.makeText(mContext, "null!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, "not null!", Toast.LENGTH_LONG).show();
        }
        return mLocation;
    }

    public double getLatitude() {
        return mLocation.getLatitude();
    }

    public double getLongitude() {
        return mLocation.getLongitude();
    }

}
