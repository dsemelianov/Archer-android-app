package com.archer.android;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class FetchLocationHandler {

    public Context mContext;

    String mMID;

    public Float latitude = 0f;
    public Float longitude = 0f;

    public Timer mTimer = new Timer();
    public TimerTask mTimerTask = new myTimerTask();

    public Location mLocation;

    public TextView mErrorBar;

    public FetchLocationHandler(String mid, TextView e, Context context) {

        mErrorBar = e;

        mMID = mid;

        mContext = context;

        mTimer.schedule(mTimerTask, 1, 10000);

    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public Location getLocation() {
        return mLocation;
    }

    public class myTimerTask extends TimerTask {
        @Override
        public void run() {
            new getLocationTask().execute(mContext.getString(R.string.api_url) + "user/getLocation/" + mMID + mContext.getString(R.string.api_key));
        }
    }

    private class getLocationTask extends AsyncTask<String, Void, String> {

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
                if (inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }

            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject obj = new JSONObject(result);
                int mErrorCode = obj.getInt("error");

                if (mErrorCode == 0) {

                    latitude = (float) obj.getDouble("latitude");
                    longitude = (float) obj.getDouble("longitude");

                    mLocation = new Location("server");
                    mLocation.setLatitude(latitude);
                    mLocation.setLongitude(longitude);

                    mErrorBar.setVisibility(View.GONE);

                } else if (mErrorCode == 1) {

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("User doesn't exist!");

               //     Toast.makeText(mContext, "User doesn't exist!", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 4) {

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Waiting for user location...");

                //    Toast.makeText(mContext, "Location unknown...", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 10) {

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Server is down!");

                //    Toast.makeText(mContext, "Server is down!", Toast.LENGTH_LONG).show();


                } else if (mErrorCode == 11) {

               //     Toast.makeText(mContext, "Server error!", Toast.LENGTH_LONG).show();

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Server error!");

                } else if (mErrorCode == 12) {

               //     Toast.makeText(mContext, "Request error...", Toast.LENGTH_LONG).show();

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Request error.");

                } else if (mErrorCode == 13) {

               //     Toast.makeText(mContext, "Unauthorized request!", Toast.LENGTH_LONG).show();

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Unauthorized request!!");

                } else if (mErrorCode == 100) {

                //    Toast.makeText(mContext, "Oops... Error!", Toast.LENGTH_LONG).show();

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Oops... Error.!");

                } else {

               //     Toast.makeText(mContext, "Unknown error!", Toast.LENGTH_LONG).show();

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Unknown error!");
                }

            } catch (JSONException j) {

                //     Toast.makeText(getBaseContext(), j.toString(), Toast.LENGTH_LONG).show();
            //    Toast.makeText(mContext, "Experiencing server issues...", Toast.LENGTH_LONG).show();

                mErrorBar.setVisibility(View.VISIBLE);
                mErrorBar.setText("Experiencing server issues...");

            }
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


}