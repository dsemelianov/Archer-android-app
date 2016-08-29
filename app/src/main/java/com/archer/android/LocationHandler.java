package com.archer.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LocationHandler {

    private static final String TAG = "ARCHER";

    private Context mContext;

    private static LocationHandler mLocationHandler;

    private LocationManager mLocationManager = null;
    private boolean mInitialized = false;
    private Location mLocation;

    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 1f;
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    public boolean isActive = false;

    String mMID;

    private LocationHandler(Context c) {
        mLocation = new Location("cached");
        mLocation.setLatitude(0f);
        mLocation.setLongitude(0f);
        mContext = c;
        initializeLocationManager();

        SharedPreferences shared = mContext.getSharedPreferences("shared", mContext.MODE_PRIVATE);
        mMID = shared.getString("mid", null);


    }

    public static LocationHandler getInstance(Context c) {

        if (mLocationHandler == null) {
            mLocationHandler = new LocationHandler(c);
        }
        return mLocationHandler;
    }

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            // Toast.makeText(getBaseContext(), location.getLatitude() + ", " +location.getLongitude(), Toast.LENGTH_LONG).show();

            if (isBetterLocation(location, mLocation)) {

                Log.e(TAG, "onLocationChanged: " + location);

                mLocation.set(location);

                if (mMID != null) {
                    new putLocationTask().execute(mContext.getString(R.string.api_url) + "user/putLocation/" + mMID + mContext.getString(R.string.api_key));
                }

            }

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public void unregisterLocationManager() {
        if (mInitialized) {
            if (mLocationManager != null) {
                for (int i = 0; i < mLocationListeners.length; i++) {
                    try {
                        mLocationManager.removeUpdates(mLocationListeners[i]);
                        mInitialized = false;
                    } catch (SecurityException e) {
                        Toast.makeText(mContext, "You haven't given Archer location permissions!", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Log.i(TAG, "fail to remove location listeners, ignore", ex);
                    }
                }
            }
        }
        isActive = false;
    }



    // TRY USING HIGH ACCURACY CRITERIA!!!!!!!!!


    public void initializeLocationManager() {
        if (!mInitialized) {
            Log.e(TAG, "initializeLocationManager");
            mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[1]);
                mInitialized = true;
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            } catch (Exception ex) {
                Log.i(TAG, "fail to add location listeners, ignore", ex);
            }

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        mLocationListeners[0]);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            } catch (Exception ex) {
                Log.i(TAG, "fail to add location listeners, ignore", ex);
            }
        }
        isActive = true;
    }

    public Location getLocation() {
        return mLocation;
    }


    private class putLocationTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            //PUT task
            String result = "";
            try {

                HttpClient client = new DefaultHttpClient();
                HttpPut put= new HttpPut(urls[0]);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("latitude", Double.toString(mLocation.getLatitude())));
                nameValuePairs.add(new BasicNameValuePair("longitude", Double.toString(mLocation.getLongitude())));
                nameValuePairs.add(new BasicNameValuePair("k", "hdj36dHyFB47dnG7h5FrEEW6E8d99j00JpD3a1M"));
                put.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = client.execute(put);

                // receive response as inputStream
                InputStream inputStream = response.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
                //mCurrent.setText("Oops");
            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
      //      Toast.makeText(getBaseContext(), "2", Toast.LENGTH_LONG).show();
        }
    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
       // return true;

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;

        boolean isMoreAccurate = accuracyDelta < 0;
        if (isMoreAccurate) return true;

        boolean isSignificantlyLessAccurate = accuracyDelta > 5;
        boolean isAccurate = location.getAccuracy() < 8;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        //if it's accurate and not old, use it
        if (isAccurate && !isSignificantlyOlder) return true;

        //if it's a lot newer, use it
        if (isSignificantlyNewer) return true;

        //if new reading isn't older, use it     ????
        //if (!isSignificantlyOlder) return true;

        if (isNewer && isAccurate) return true;

       // If the new location is older and less accurate, it must be worse
        if (isSignificantlyOlder && isSignificantlyLessAccurate) return false;

        // Determine location quality using a combination of timeliness and accuracy
        if (isNewer && !isLessAccurate)  return true;
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) return true;

        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}