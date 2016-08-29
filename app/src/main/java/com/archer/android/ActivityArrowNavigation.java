package com.archer.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityArrowNavigation extends AppCompatActivity implements SensorEventListener {
    //   , SharedPreferences.OnSharedPreferenceChangeListener

    private TextView mErrorBar;

    private String mMID;

    private TextView mName;
    private TextView mDistance;
    private ImageView mArrowImage;
    private MapView mMapView;
    private RelativeLayout mSwitchModeButton;
    private ImageView mModeIcon;

    private TextView mLoading;

    // record the current degree that the arrow image is turned
    private float currentDegree = 0f;
    private SensorManager mSensorManager;

    Arrow mArrowObject;
    boolean isSponsored = false;
    private Location mSponsoredLocation = new Location("sponsored");

    private Location mUserLocation = new Location("cache");

    //handlers to provide location updates
//    private LocalLocationHandler mLocalLocationHandler;
    private FetchLocationHandler mFetchLocationHandler;

    GoogleMap mMap;
    boolean mapIsSetup = false;
    boolean showMapMode = false;

    float mFinalAngle;

    final String YOUR_PROJECT_TOKEN = "4864ac47bd7beb9dce3cbe039d7814fe";
    MixpanelAPI mixpanel;

    public SharedPreferences shared;

    public Marker mUserMarker;
    public Marker mDestinationMarker;

    public LatLng mUserLatLng;
    public LatLng mDestinationLatLng;

    public LocationHandler mLocationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrow_navigation);

        String projectToken = YOUR_PROJECT_TOKEN;
        mixpanel = MixpanelAPI.getInstance(this, projectToken);

        mLocationHandler = LocationHandler.getInstance(ActivityArrowNavigation.this);

        shared = getSharedPreferences("shared", MODE_PRIVATE);
        mMID = shared.getString("mid", "null");
        //    mUserLocation.setLatitude(mCache.getLatitude());
        //   mUserLocation.setLongitude(mCache.getLongitude());

        mLoading = (TextView) findViewById(R.id.loading_overlay);

        mErrorBar = (TextView) findViewById(R.id.error_bar);

        Intent intent = getIntent();
        mArrowObject = intent.getParcelableExtra("arrow");

        if (mArrowObject.getType().equals("SPONSORED")) {
            isSponsored = true;
            mSponsoredLocation.setLatitude(mArrowObject.getLatitude());
            mSponsoredLocation.setLongitude(mArrowObject.getLongitude());
        }

        mName = (TextView) findViewById(R.id.name_label);
        mName.setText(mArrowObject.getName());

        mDistance = (TextView) findViewById(R.id.distance);

        //SET UP MODE SWITCHING
        mSwitchModeButton = (RelativeLayout) findViewById(R.id.mode_button);
        mModeIcon = (ImageView) findViewById(R.id.mode_icon);
        mSwitchModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((!showMapMode) && (mapIsSetup)) {
                    if (Build.VERSION.SDK_INT < 23) {
                        mSwitchModeButton.setBackgroundColor(getResources().getColor(R.color.seafoam_green));
                    } else {
                        mSwitchModeButton.setBackgroundColor(getColor(R.color.seafoam_green));
                    }
                    mModeIcon.setImageDrawable(getResources().getDrawable(R.drawable.arrow_icon));
                    mMapView.setVisibility(View.VISIBLE);

                    mArrowImage.setVisibility(View.INVISIBLE);

                    //     LatLng mCurrentLatLng = new LatLng(mLocalLocationHandler.getLatitude(), mLocalLocationHandler.getLongitude());
                   /* LatLng mCurrentLatLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());

                    // construct a CameraPosition focusing on our current position and pointing towards the tower
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(mCurrentLatLng)      // Sets the center of the map to current location
                            .zoom(19)                   // Sets the zoom
                            .bearing(currentDegree)        // Sets the orientation of the camera to destination @ north
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/

                    // setMapView();

                    try {
                        mixpanel.identify(mMID);
                        JSONObject props = new JSONObject();
                        props.put("Device Type", "Android");
                        props.put("Mode", "Map");
                        mixpanel.track("SWITCH MODE", props);
                    } catch (JSONException e) {

                    }

                    //THIS PROPERLY SETS THE CAMERA VIEW
                    /*LatLngBounds.Builder latlng_builder = new LatLngBounds.Builder();
                    latlng_builder.include(mDestinationLatLng);
                    latlng_builder.include(mUserLatLng);
                    LatLngBounds bounds = latlng_builder.build();

                    LatLng mCenterPoint = bounds.getCenter();
                    LatLng mNorthEast = bounds.northeast;
                    double mLongitude1 = mNorthEast.longitude;
                    LatLng mSouthWest = bounds.southwest;
                    double mLongitude2 = mSouthWest.longitude;
                    double angle = mLongitude1 - mLongitude2;
                    if (angle < 0) {
                        angle += 360;
                    }
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int globe_width = 256;
                    int pixel_width = size.x;

                    long zoom = Math.round(Math.log(pixel_width * 360 / angle / globe_width) / Math.log(2));

                    CameraPosition.Builder camera_builder = new CameraPosition.Builder();
                    camera_builder
                            .target(mCenterPoint)
                            .zoom(zoom);*/

                  //  mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera_builder.build()));

                    /*Thread setMapView = new Thread(new Runnable() {
                        public void run() {
                            setMapView();
                        }
                    }, "AudioRecorder Thread");

                    setMapView.start();*/

                    setMapView();

                } else if (showMapMode) {
                    if (Build.VERSION.SDK_INT < 23) {
                        mSwitchModeButton.setBackgroundColor(getResources().getColor(R.color.white));
                    } else {
                        mSwitchModeButton.setBackgroundColor(getColor(R.color.white));
                    }
                    mModeIcon.setImageDrawable(getResources().getDrawable(R.drawable.map_icon));
                    mMapView.setVisibility(View.INVISIBLE);
                    mArrowImage.setVisibility(View.VISIBLE);

                    try {
                        mixpanel.identify(mMID);
                        JSONObject props = new JSONObject();
                        props.put("Device Type", "Android");
                        props.put("Mode", "Arrow");
                        mixpanel.track("SWITCH MODE", props);
                    } catch (JSONException e) {
                    }

                }

                showMapMode = !showMapMode;

            }
        });

        //INITIALIZE ARROW MODE
        mArrowImage = (ImageView) findViewById(R.id.arrow);
        // mLocalLocationHandler = LocalLocationHandler.getInstance(ActivityArrowNavigation.this);
        if (!isSponsored) {
            mFetchLocationHandler = new FetchLocationHandler(mArrowObject.getMID(), mErrorBar, ActivityArrowNavigation.this);
        }
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //INITIALIZE MAP MODE
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mapIsSetup = true;
        mMap = mMapView.getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //  mMap.setMyLocationEnabled(true);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(ActivityArrowNavigation.this);
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Google maps error!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // resume map
        if (mMapView != null) mMapView.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

        //   getSharedPreferences("shared", MODE_PRIVATE)
        //          .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);

        //     getSharedPreferences("shared", MODE_PRIVATE)
        //            .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) mMapView.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        //if the phone is rotated, get the latest available location coordinates
        //  Location user_location = mLocalLocationHandler.getLocation(ActivityArrowNavigation.this);
        mUserLocation = mLocationHandler.getLocation();

        Location destination_location;
        if (!isSponsored) {
            destination_location = mFetchLocationHandler.getLocation();
        } else {
            destination_location = mSponsoredLocation;
        }

        //if location coordinates are not null...
        if ((mUserLocation.getLongitude() != 0f) && (mUserLocation.getLatitude() != 0f) && (destination_location != null)) {

            mLoading.setVisibility(View.GONE);

            // Toast.makeText(ActivityArrowNavigation.this, "your location: " + String.valueOf(user_location.getLongitude()) + ", " + String.valueOf(user_location.getLatitude()), Toast.LENGTH_LONG).show();
            // Toast.makeText(ActivityArrowNavigation.this, "target location: " + String.valueOf(destination_location.getLongitude()) + ", " + String.valueOf(destination_location.getLatitude()), Toast.LENGTH_LONG).show();

            //UPDATE THE DISTANCE
            Integer mDistanceValueInt = Math.round(mUserLocation.distanceTo(destination_location));
            String mStringDistance;
            if (mDistanceValueInt > 1000) {
                mStringDistance = "1000+";
            } else  {
                mStringDistance = mDistanceValueInt.toString();
            }
            mDistance.setText(mStringDistance + " m");

            //DO ARROW PROCESS
            rotateArrow(event.values[0], (float) Math.toDegrees(Math.atan2(
                    destination_location.getLongitude() - mUserLocation.getLongitude(),
                    destination_location.getLatitude() - mUserLocation.getLatitude())));

            //DROP PINS ONTO MAP
            //if map is not null, let's draw some markers
            if (mMap != null) {

                mDestinationLatLng = new LatLng(destination_location.getLatitude(), destination_location.getLongitude());

                //if the marker is null, create a new marker
                if (mDestinationMarker == null) {
                    mDestinationMarker = mMap.addMarker(new MarkerOptions()
                            .position(mDestinationLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
                            .draggable(false)
                            .title(mArrowObject.getName()));
                    //else move the marker
                } else {
                    mDestinationMarker.setPosition(mDestinationLatLng);
                }


                mUserLatLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());

                //if the marker is null, create a new marker
                if (mUserMarker == null) {
                    mUserMarker = mMap.addMarker(new MarkerOptions()
                            .position(mUserLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_you))
                            .draggable(false)
                            .title("You"));
                    //else move the marker
                } else  {
                    mUserMarker.setPosition(mUserLatLng);
                }

            }


        }

    }

    public void setMapView() {

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        if ((mDestinationLatLng == null) || (mUserLatLng == null)) {

            LatLng mCurrentLatLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());

            // construct a CameraPosition focusing on our current position and pointing towards the tower
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mCurrentLatLng)      // Sets the center of the map to current location
                    .zoom(19)                   // Sets the zoom
                    .bearing(currentDegree)        // Sets the orientation of the camera to destination @ north
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(mDestinationLatLng);
            builder.include(mUserLatLng);
            LatLngBounds bounds = builder.build();
            int padding = 300; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

            mMap.moveCamera(cu);

            /*try {
                mMap.moveCamera(cu);
            } catch (Exception e) {

                Toast.makeText(ActivityArrowNavigation.this, "Map not initialized, dingus!", Toast.LENGTH_SHORT);
            }*/

        }

    }

    private void rotateArrow(float value, float v) {
        //get the bearing between the two locations
        float mBearing = v;

        if (mBearing < 0) {
            mBearing += 360;
        }

        // get the angle around the z-axis rotated
        float mOrientationToNorth = Math.round(value);

        //get final angle needed for compass
        mFinalAngle = mBearing - mOrientationToNorth;
        if (mFinalAngle < 0) {
            mFinalAngle += 360;
        } else if (mFinalAngle > 360) {
            mFinalAngle -= 360;
        }
        //   tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

 //       if (((mFinalAngle-currentDegree) > 1) || ((mFinalAngle-currentDegree) < 1)){

            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    mFinalAngle,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);

            // how long the animation will take place
            ra.setDuration(210);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            mArrowImage.startAnimation(ra);

            currentDegree = mFinalAngle;
   //     }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}