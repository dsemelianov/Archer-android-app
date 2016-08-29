package com.archer.android;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class ArrowAdapter extends BaseAdapter {

    private static final int ITEM_VIEW_TYPE_SEPARATOR = 0;
    private static final int ITEM_VIEW_TYPE_REQUEST = 1;
    private static final int ITEM_VIEW_TYPE_CURRENT = 2;
    private static final int ITEM_VIEW_TYPE_SPONSORED = 3;
    private static final int ITEM_VIEW_TYPE_PROMPT = 4;
    private static final int ITEM_VIEW_TYPE_PENDING = 5;

    private static final int ITEM_VIEW_TYPE_COUNT = 6;

    ArrayList<Arrow> mList;
    Context mContext;

    String mMID;

    Location mMyLocation;

    final String YOUR_PROJECT_TOKEN = "4864ac47bd7beb9dce3cbe039d7814fe";
    MixpanelAPI mixpanel;

    ArrowManager mArrowManager = ArrowManager.getInstance();

    public ArrowAdapter(ArrayList<Arrow> list, Context context, String mid, Location location) {

        this.mList = list;
        this.mContext = context;

        this.mMID = mid;
        this.mMyLocation = location;

        String projectToken = YOUR_PROJECT_TOKEN; // e.g.: "1ef7e30d2a58d27f4b90c42e31d6d7ad"
        mixpanel = MixpanelAPI.getInstance(mContext, projectToken);

    }

    @Override
    public int getCount() {
        //return OBJECTS.length;
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        //return OBJECTS[position];
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        if (!mList.isEmpty()) {
            //if (OBJECTS[position] instanceof ArrowListItem) {
            if (mList.get(position).getType().equals("ACTIVE")) {
                return ITEM_VIEW_TYPE_CURRENT;
            } else if (mList.get(position).getType().equals("REQUEST")) {
                return ITEM_VIEW_TYPE_REQUEST;
            } else if (mList.get(position).getType().equals("SPONSORED")) {
                return ITEM_VIEW_TYPE_SPONSORED;
            } else if (mList.get(position).getType().equals("PENDING")) {
                return ITEM_VIEW_TYPE_PENDING;
            } else if (mList.get(position).getType().equals("PROMPT")) {
                return ITEM_VIEW_TYPE_PROMPT;
            } else {
                return ITEM_VIEW_TYPE_SEPARATOR;
            }
        }
        else { return ITEM_VIEW_TYPE_CURRENT; }
    }

    @Override
    public boolean isEnabled(int position) {
        // A separator cannot be clicked !
        if ((getItemViewType(position) == ITEM_VIEW_TYPE_SEPARATOR) || (getItemViewType(position) == ITEM_VIEW_TYPE_REQUEST)) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final int type = getItemViewType(position);

        // First, let's create a new convertView if needed. You can also
        // create a ViewHolder to speed up changes if you want ;)
        if (convertView == null) {
            if (type == ITEM_VIEW_TYPE_REQUEST) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_request, parent, false);
            } else if (type == ITEM_VIEW_TYPE_CURRENT) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_arrow, parent, false);
            } else if (type == ITEM_VIEW_TYPE_SPONSORED) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_sponsored, parent, false);
            } else if (type == ITEM_VIEW_TYPE_PROMPT) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_send_arrow_prompt, parent, false);
            } else if (type == ITEM_VIEW_TYPE_PENDING) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_pending_arrow, parent, false);
            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_separator, parent, false);
            }
        }

        // We can now fill the list item view with the appropriate data.
        final Arrow arrow = (Arrow) getItem(position);
        if (type == ITEM_VIEW_TYPE_REQUEST) {

            ((TextView) convertView.findViewById(R.id.name)).setText(arrow.mName);

            ImageView mAccept = (ImageView) convertView.findViewById(R.id.approve);
            mAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String aid = arrow.getAID();
                    String mid = arrow.getMID();
                //    String name = arrow.getName();
                    SharedPreferences shared = mContext.getSharedPreferences("shared", mContext.MODE_PRIVATE);
                    String name = shared.getString("username", "null");

                    new TaskArrowAccept(mList, ArrowAdapter.this, mContext, aid, mid, name).execute(mContext.getString(R.string.api_url) + "arrow/accept" + mContext.getString(R.string.api_key));

                    try {
                        mixpanel.identify(mMID);
                        JSONObject props = new JSONObject();
                        props.put("Device Type", "Android");
                        mixpanel.track("ACCEPT", props);
                    } catch (JSONException e) {
                    }

                }
            });

            ImageView mDeny = (ImageView) convertView.findViewById(R.id.deny);
            mDeny.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String aid = arrow.getAID();
                    String mid = arrow.getMID();
                    //    String name = arrow.getName();
                    SharedPreferences shared = mContext.getSharedPreferences("shared", mContext.MODE_PRIVATE);
                    String name = shared.getString("username", "null");

                    new TaskArrowDeny(mList, ArrowAdapter.this, mContext, aid, mid, name).execute(mContext.getString(R.string.api_url) + "arrow/deny" + mContext.getString(R.string.api_key));

                    try {
                        mixpanel.identify(mMID);
                        JSONObject props = new JSONObject();
                        props.put("Device Type", "Android");
                        mixpanel.track("DENY", props);
                    } catch (JSONException e) {}
                }
            });

        } else if (type == ITEM_VIEW_TYPE_CURRENT) {

            ((TextView) convertView.findViewById(R.id.name)).setText(arrow.mName);

            ImageView mDelete = (ImageView) convertView.findViewById(R.id.delete);
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String aid = arrow.getAID();
                    new TaskDeleteArrow(mList, ArrowAdapter.this, mContext).execute(mContext.getString(R.string.api_url) + "arrow/" + aid + mContext.getString(R.string.api_key));

                    try {
                        mixpanel.identify(mMID);
                        JSONObject props = new JSONObject();
                        props.put("Device Type", "Android");
                        mixpanel.track("DELETE", props);
                    } catch (JSONException e) {}
                }
            });

            final TextView mTimeRemaining = (TextView) convertView.findViewById(R.id.time);
            long arrow_time = arrow.getExpirationTime().getTime();
            long current_time = new Date().getTime() + 18000000; //get current time and subtract 5 hours of offset time

         //   long current_time_system = System.currentTimeMillis();
          //  long difference_in_msec = arrow.getExpirationTime().getTime() - new Date().getTime();

            long difference_in_msec = arrow_time - current_time;
            long diffHours = difference_in_msec / (60 * 60 * 1000);
            if (diffHours < 0) {
                mTimeRemaining.setText("Arrow expired");
            } else if (diffHours > 24) {
                mTimeRemaining.setText("24+ hours");
            } else {
                mTimeRemaining.setText(diffHours + " hours");
            }

            TextView mDistanceBetween = (TextView) convertView.findViewById(R.id.distance);
            if (mMyLocation != null) {
                String mDistanceValue = arrow.getDistanceTo(mMyLocation);
                mDistanceBetween.setText(mDistanceValue + " m");
            } else {
                mDistanceBetween.setText("N/A");
            }

            RelativeLayout mBody = (RelativeLayout) convertView.findViewById(R.id.body);
            mBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTimeRemaining.getText().toString().equals("Arrow expired")) {
                        Toast.makeText(mContext, "Arrow is expired!", Toast.LENGTH_LONG).show();

                    } else {

                        try {
                            mixpanel.identify(mMID);
                            JSONObject props = new JSONObject();
                            props.put("Type", "People");
                            props.put("Device Type", "Android");
                            mixpanel.track("OPEN", props);
                        } catch (JSONException e) {}

                        Intent intent = new Intent(mContext, ActivityArrowNavigation.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("arrow", arrow);
                        mContext.startActivity(intent);
                    }
                }
            });


        } else if (type == ITEM_VIEW_TYPE_SPONSORED) {
            ((TextView) convertView.findViewById(R.id.name)).setText(arrow.mName);

            RelativeLayout mBody = (RelativeLayout) convertView.findViewById(R.id.body);
            mBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        mixpanel.identify(mMID);
                        JSONObject props = new JSONObject();
                        props.put("Type", "Sponsored");
                        props.put("Device Type", "Android");
                        mixpanel.track("OPEN", props);
                    } catch (JSONException e) {}

                    mixpanel.getPeople().identify(mMID);
                    mixpanel.getPeople().increment("Suggested Arrows Opened", 1);

                    Intent intent = new Intent(mContext, ActivityArrowNavigation.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("arrow", arrow);
                    mContext.startActivity(intent);
                }
            });

        } else if (type == ITEM_VIEW_TYPE_PROMPT) {

            RelativeLayout mBody = (RelativeLayout) convertView.findViewById(R.id.body);
            mBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, SendArrowActivity.class);
                    mContext.startActivity(intent);
                }
            });

        } else if (type == ITEM_VIEW_TYPE_PENDING) {

            ((TextView) convertView.findViewById(R.id.name)).setText(arrow.mName);

            ImageView mDelete = (ImageView) convertView.findViewById(R.id.delete);
            mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String aid = arrow.getAID();
                    new TaskDeleteArrow(mList, ArrowAdapter.this, mContext).execute(mContext.getString(R.string.api_url) + "arrow/" + aid + mContext.getString(R.string.api_key));

                    try {
                        mixpanel.identify(mMID);
                        JSONObject props = new JSONObject();
                        props.put("Device Type", "Android");
                        mixpanel.track("DELETE", props);
                    } catch (JSONException e) {}
                }
            });

        } else {
            ((TextView) convertView.findViewById(R.id.title)).setText(arrow.mName);
        }

        return convertView;
    }

}