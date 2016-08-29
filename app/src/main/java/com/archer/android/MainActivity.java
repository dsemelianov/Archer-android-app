package com.archer.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeLayout;
    private ArrowAdapter listAdapter;
    ListView mListView;
    ArrayList<Arrow> mList = new ArrayList<>();

    String mMID;

    LocationHandler mLocationHandler;

    final String YOUR_PROJECT_TOKEN = "4864ac47bd7beb9dce3cbe039d7814fe";
    MixpanelAPI mixpanel;

    private TextView mErrorBar;

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered = false;

    ArrowManager mArrowManager = ArrowManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + "Archer" + "</font>"));

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (swipeLayout != null) {
                    new GetArrowsTask().execute(getString(R.string.api_url) + "arrows/" + mMID + getString(R.string.api_key));
                }
            }
        };
        registerReceiver();

        try {
            Intent intent = getIntent();
            Uri openUri = intent.getData();
            Toast.makeText(MainActivity.this, openUri.toString(), Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        String projectToken = YOUR_PROJECT_TOKEN; // e.g.: "1ef7e30d2a58d27f4b90c42e31d6d7ad"
        mixpanel = MixpanelAPI.getInstance(this, projectToken);

        mLocationHandler = LocationHandler.getInstance(MainActivity.this);

        SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
        if(!shared.contains("mid")) {

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        } else {

            String mConfirmed = shared.getString("confirmed", "null");
            if (!mConfirmed.equals("t")) {

                Intent intent = new Intent(this, ActivityConfirmPhoneNumber.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            } else {

                mMID = shared.getString("mid", "null");

                new TaskUpdateToken(mMID, MainActivity.this).execute(getString(R.string.api_url) + "user/store_token.json");

                MixpanelAPI.People people = mixpanel.getPeople();
                people.identify(mMID);
                //people.initPushHandling("865550989871");

                mListView = (ListView) findViewById(R.id.list);
                // VideoAdapter listAdapter = new VideoAdapter();
                // mListView.setAdapter(listAdapter);

                swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

                swipeLayout.setColorSchemeColors(
                        getResources().getColor(R.color.arrow_blue),
                        getResources().getColor(R.color.request_orange),
                        getResources().getColor(R.color.sponsored_purple)
                );

                swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                new GetArrowsTask().execute(getString(R.string.api_url) + "arrows/" + mMID + getString(R.string.api_key));
                            }
                        }, 5000);
                        //3119298bc948f180
                    }
                });

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                ColorStateList rippleColor = ContextCompat.getColorStateList(MainActivity.this, R.color.request_orange);
                fab.setBackgroundTintList(rippleColor);
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, SendArrowActivity.class);
                        startActivity(intent);
                    }
                });

                mErrorBar = (TextView) findViewById(R.id.error_bar);

                //   new GetArrowsTask().execute("http://pointme-hogueyy.c9users.io/api/arrows/" + mMID + ".json?k=hdj36dHyFB47dnG7h5FrEEW6E8d99j00JpD3a1M");

            }

        }

    }

    private boolean checkPlayServices() {

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000)
                        .show();
            } else {
                Log.i("PlayServicesTAG", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_logout) {

            SharedPreferences settings = getApplicationContext().getSharedPreferences("shared", Context.MODE_PRIVATE);
            settings.edit().remove("mid").remove("confirmed").apply();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            //  } else if (id == R.id.action_settings) {

        }
        return super.onOptionsItemSelected(item);
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

            swipeLayout.setRefreshing(true);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {

                mArrowManager.clearNumbers();

                mList.clear();

                JSONObject obj = new JSONObject(result);
                int mErrorCode = obj.getInt("error");

                if (mErrorCode == 0) {

                    //Add the Requests bar
                    Arrow mRequestsSeparator = new Arrow("SEPARATOR");
                    mRequestsSeparator.setName("REQUESTS");
                    mList.add(mRequestsSeparator);

                    //Add the Currently Running bar
                    Arrow mActiveSeparator = new Arrow("SEPARATOR");
                    mActiveSeparator.setName("CURRENTLY RUNNING");
                    mList.add(mActiveSeparator);

                    //Let's fill in the "requests" and "currently running" sections
                    JSONArray mPeopleArray = obj.getJSONArray("arrows");
                    for (int i = 0; i < mPeopleArray.length(); i++) {

                        JSONObject arrow_object = mPeopleArray.getJSONObject(i);

                        Arrow arrow;

                        String accepted = arrow_object.getString("accepted");

                        if (accepted.equals("t")) {

                            //if the arrow has been accepted, it's actively running
                            arrow = new Arrow("ACTIVE");

                        } else if (accepted.equals("f")) {

                            //if the arrow has been rejected, throw it out
                            continue;

                        } else {

                            //if the arrow hasn't been accepted or rejected, check who owns it
                            String sender_mid = arrow_object.getString("memberids");

                            if (sender_mid.equals(mMID)) {

                                //if you sent this arrow request, it's pending a response
                                arrow = new Arrow("PENDING");

                            } else {

                                //if this arrow request was sent to you, request a response
                                arrow = new Arrow("REQUEST");
                            }

                        }

                        //set the arrow parameters
                        setArrowParameters(arrow_object, arrow);

                        if (arrow.getType().equals("REQUEST")) {
                            //if it's a request, add it to the beginning of the list
                            mList.add(1, arrow);
                            mArrowManager.incrementNUMBER_OF_REQUESTS();
                        } else {
                            //if it's an active arrow, add it to the end of the list
                            mList.add(arrow);
                            mArrowManager.incrementNUMBER_OF_ACTIVE();
                        }

                    }

                    //if there are no requests, remove the "REQUESTS" section
                    if (mArrowManager.getNUMBER_OF_REQUESTS() == 0) {
                        mList.remove(0);
                    }

                    //if there are no active arrows, add a prompt
                    if (mArrowManager.getNUMBER_OF_ACTIVE() == 0) {

                        Arrow mPromptArrow = new Arrow("PROMPT");
                        mList.add(mPromptArrow);

                    }

                    Arrow mSponsoredSeparator = new Arrow("SEPARATOR");
                    mSponsoredSeparator.setName("SUGGESTED");
                    mList.add(mSponsoredSeparator);

                    JSONArray mPlacesArray = obj.getJSONArray("places");
                    if ((mPeopleArray.length() == 0) && (mPlacesArray.length() == 0)) {
                        mLocationHandler.unregisterLocationManager();
                    } else if (mLocationHandler.isActive) {
                        mLocationHandler.initializeLocationManager();
                    }

                    for (int i = 0; i < mPlacesArray.length(); i++) {

                        JSONObject arrow_object = mPlacesArray.getJSONObject(i);

                        Arrow arrow = new Arrow("SPONSORED");

                        String sponsor = arrow_object.getString("name");
                        arrow.setName(sponsor);

                        String pid = arrow_object.getString("pid");
                        arrow.setMID(pid);

                        String date = arrow_object.getString("deathtime");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            Date expiration = sdf.parse(date);
                            arrow.setExpirationTime(expiration);
                        } catch (ParseException e) {
                            Toast.makeText(getBaseContext(), "Parse Exception: " + e.toString(), Toast.LENGTH_LONG).show();
                        }

                        String coordinates = arrow_object.getString("location");
                        if (coordinates != null) {
                            if (!coordinates.isEmpty()) {

                                String latitude = coordinates.substring(coordinates.indexOf("(") + 1);
                                latitude = latitude.substring(0, latitude.indexOf(","));
                                double latitudeNum = Double.parseDouble(latitude);

                                String longitude = coordinates.substring(coordinates.indexOf(",") + 1);
                                longitude = longitude.substring(0, longitude.indexOf(")"));
                                double longitudeNum = Double.parseDouble(longitude);

                                arrow.setDestinationLocation(latitudeNum, longitudeNum);
                            }
                        }

                        mList.add(arrow);
                        mArrowManager.incrementNUMBER_OF_SPONSORED();

                    }

                    if (mArrowManager.getNUMBER_OF_SPONSORED() == 0) {
                        mList.remove(mList.size() - 1);
                    }

                    Location mUserLocation = mLocationHandler.getLocation();

                    listAdapter = new ArrowAdapter(mList, MainActivity.this, mMID, mUserLocation);
                    mListView.setAdapter(listAdapter);

                    mErrorBar.setVisibility(View.GONE);

                    //JSONObject res = obj.getJSONArray("results").getJSONObject(0);
                    //System.out.println(res.getString("formatted_address"));

                } else if (mErrorCode == 1) {
                    //mid doesn't exist

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Please log in again.");

                } else if (mErrorCode == 10) {

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("The server is down!");

                } else if (mErrorCode == 11) {

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Server error!");

                } else if (mErrorCode == 13) {

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Unauthorized request!");

                } else if (mErrorCode == 100) {

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Oops... Error!");

                } else {

                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Unknown error!");

                }

            } catch (JSONException j) {

                String error = j.toString();
                if (error.startsWith("org.json.JSONException: End of input at character 0")) {
                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("No connection!");
                } else {
                    mErrorBar.setVisibility(View.VISIBLE);
                    mErrorBar.setText("Experiencing server issues...");
                }

            } finally {
                swipeLayout.setRefreshing(false);
            }
        }

        private void setArrowParameters(JSONObject arrow_object, Arrow arrow) throws JSONException {
            String full_name = arrow_object.getString("full_name");
            arrow.setName(full_name);
            String mid = arrow_object.getString("mid");
            arrow.setMID(mid);
            String aid = arrow_object.getString("aid");
            arrow.setAID(aid);
            String date = arrow_object.getString("deathtime");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date expiration = sdf.parse(date);
                arrow.setExpirationTime(expiration);
            } catch (ParseException e) {
                Toast.makeText(getBaseContext(), "Parse Exception: " + e.toString(), Toast.LENGTH_LONG).show();
            }
            String coordinates = arrow_object.getString("location");
            if (coordinates != null) {
                if ((!coordinates.isEmpty()) && (!coordinates.equals("null"))) {

                    String latitude = coordinates.substring(coordinates.indexOf("(") + 1);
                    latitude = latitude.substring(0, latitude.indexOf(","));
                    double latitudeNum = Double.parseDouble(latitude);

                    String longitude = coordinates.substring(coordinates.indexOf(",") + 1);
                    longitude = longitude.substring(0, longitude.indexOf(")"));
                    double longitudeNum = Double.parseDouble(longitude);

                    arrow.setDestinationLocation(latitudeNum, longitudeNum);
                }
            }
        }
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(PreferencesGCM.NOTIFICATION_RECEIVED));
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        registerReceiver();

        mLocationHandler = LocationHandler.getInstance(MainActivity.this);
        mLocationHandler.initializeLocationManager();
        if (swipeLayout != null) {
            new GetArrowsTask().execute(getString(R.string.api_url) + "arrows/" + mMID + getString(R.string.api_key));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (swipeLayout != null) {
            new GetArrowsTask().execute(getString(R.string.api_url) + "arrows/" + mMID + getString(R.string.api_key));
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationHandler.unregisterLocationManager();

    }

}

