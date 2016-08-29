package com.archer.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.Fabric;

public class LocationService extends Service {

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private boolean mInitialized = false;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 1f;

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    Location mLastLocation;

    String mMID;

    SharedPreferences shared;

    Cache mCache;

    public Timer mTimer = new Timer();
    public TimerTask mTimerTask = new myTimerTask();

    @Override
    public void onCreate() {
        Fabric.with(this, new Crashlytics());

        mCache = Cache.getInstance(LocationService.this);

        shared = getSharedPreferences("shared", MODE_PRIVATE);
        mMID = shared.getString("mid", "null");

        Log.e(TAG, "onCreate");
        initializeLocationManager();

        mTimer.schedule(mTimerTask, 1, 100000);

    }

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            // Toast.makeText(getBaseContext(), location.getLatitude() + ", " +location.getLongitude(), Toast.LENGTH_LONG).show();

            if (isBetterLocation(location, mLastLocation)) {

                Log.e(TAG, "onLocationChanged: " + location);

                //if (mLastLocation.distanceTo(location) > 10) {

                //mLastLocation = location;
                mLastLocation.set(location);

           //     Toast.makeText(getBaseContext(), "1", Toast.LENGTH_LONG).show();
                mCache.changeLocation(location.getLatitude(), location.getLongitude());

                new putLocationTask().execute("http://pointme-env.29xnyiqa8x.us-west-2.elasticbeanstalk.com/api/user/putLocation/" + mMID + ".json");

            }
            //  }

            /*if ((mLastLocation.getLatitude() != 0) && (mLastLocation.getLongitude() != 0)) {
                new putLocationTask().execute("http://pointme-env.29xnyiqa8x.us-west-2.elasticbeanstalk.com/api/user/putLocation/" + mMID + ".json");
            }*/

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

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }*/

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        stopService(new Intent(LocationService.this, LocationService.class));
        unregisterLocationManager();
    }

    private void unregisterLocationManager() {
        if (mInitialized) {
            if (mLocationManager != null) {
                for (int i = 0; i < mLocationListeners.length; i++) {
                    try {
                        mLocationManager.removeUpdates(mLocationListeners[i]);
                        mInitialized = false;
                    } catch (SecurityException e) {
                        Toast.makeText(getBaseContext(), "You haven't given Archer location permissions!", Toast.LENGTH_SHORT).show();
                    } catch (Exception ex) {
                        Log.i(TAG, "fail to remove location listeners, ignore", ex);
                    }
                }
            }
        }
    }

    private void initializeLocationManager() {
        if (!mInitialized) {
            Log.e(TAG, "initializeLocationManager");
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
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
                nameValuePairs.add(new BasicNameValuePair("latitude", Double.toString(mLastLocation.getLatitude())));
                nameValuePairs.add(new BasicNameValuePair("longitude", Double.toString(mLastLocation.getLongitude())));
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
        boolean isSignificantlyLessAccurate = accuracyDelta > 5;
        boolean isAccurate = location.getAccuracy() < 8;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        //if it's accurate and not old, use it
        if (isAccurate && !isSignificantlyOlder) return true;

        //if it's a lot newer, use it
        if (isSignificantlyNewer) return true;

        //if new reading is more accurate and isn't old, use it     ????
        if (isMoreAccurate && !isSignificantlyOlder) return true;

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

    public class myTimerTask extends TimerTask {
        @Override
        public void run() {
            new GetArrowsTask().execute("http://pointme-env.29xnyiqa8x.us-west-2.elasticbeanstalk.com/api/arrows/" + mMID + ".json?k=hdj36dHyFB47dnG7h5FrEEW6E8d99j00JpD3a1M");
        }
    }

    private class GetArrowsTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            //GET task
            String result = "";
            try {

                // create HttpClient
                HttpClient httpclient = new DefaultHttpClient();

                // make GET request to the given URL
                HttpResponse httpResponse = httpclient.execute(new HttpGet(urls[0]));

                // receive response as inputStream
                InputStream inputStream = httpResponse.getEntity().getContent();

                // convert inputstream to string
                if(inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {

                JSONObject obj = new JSONObject(result);
                int mErrorCode = obj.getInt("error");
                if (mErrorCode == 0) {

                    JSONArray mPeopleArray = obj.getJSONArray("arrows");
                    JSONArray mSponsoredArray = obj.getJSONArray("places");

                    if ((mPeopleArray.length() == 0) && (mSponsoredArray.length() == 0)) {
                        unregisterLocationManager();
                    } else {
                        initializeLocationManager();
                    }

                } else if (mErrorCode == 1) {
                    //mid doesn't exist

                    Toast.makeText(getBaseContext(), "Please log in again", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 10) {

                    Toast.makeText(getBaseContext(), "The server is down!", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 11) {
                     Toast.makeText(getBaseContext(), "Server error!", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 13) {

                    Toast.makeText(getBaseContext(), "Incorrect key", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 100) {

                    Toast.makeText(getBaseContext(), "Oops... Error!", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(getBaseContext(), "Unknown error!", Toast.LENGTH_LONG).show();

                }

            } catch (JSONException j) {
                 Toast.makeText(getBaseContext(), j.toString(), Toast.LENGTH_LONG).show();

            } finally {
                //swipeLayout.setRefreshing(false);
            }
        }
    }

}