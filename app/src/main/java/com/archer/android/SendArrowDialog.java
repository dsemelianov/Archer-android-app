package com.archer.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidemelianov on 10/17/15.
 */
public class SendArrowDialog {

    private Context mContext;

    private String mMID;

    private String mUserNumber;

    private Dialog mDialog;

    private TextView mNameLabel;
    private String mOtherName;
    private String mOtherNumber;

    private TextView mContentLabel;

    private ImageView mAcceptButton;
    private ImageView mDenyButton;

    private ProgressBar mProgress;

    final String YOUR_PROJECT_TOKEN = "4864ac47bd7beb9dce3cbe039d7814fe";
    MixpanelAPI mixpanel;

    public SendArrowDialog(Context c, String user_number, String other_name, String other_number, String mid) {

        mContext = c;

        mMID = mid;

        String projectToken = YOUR_PROJECT_TOKEN; // e.g.: "1ef7e30d2a58d27f4b90c42e31d6d7ad"
        mixpanel = MixpanelAPI.getInstance(mContext, projectToken);

        MixpanelAPI.People people = mixpanel.getPeople();
        people.identify(mMID);

        mUserNumber = user_number;

        mOtherName = other_name;

        mOtherNumber = other_number;
        mOtherNumber = mOtherNumber.trim();
        mOtherNumber = mOtherNumber.replaceAll( "[^\\d]", "" );

        // Create custom dialog object
        mDialog = new Dialog(mContext);

        // Include dialog.xml file
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_confirm);

        mNameLabel = (TextView) mDialog.findViewById(R.id.name_label);
        mNameLabel.setText(mOtherName);

        mContentLabel = (TextView) mDialog.findViewById(R.id.description_label);

        mAcceptButton = (ImageView) mDialog.findViewById(R.id.approve);
        mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new SendArrowRequest().execute(mContext.getString(R.string.api_url) + "arrow" + mContext.getString(R.string.api_key));
                //new SendArrowRequest().execute("http://pointme-hogueyy.c9users.io/api/arrow.json?k=hdj36dHyFB47dnG7h5FrEEW6E8d99j00JpD3a1M");

                try {
                    mixpanel.identify(mMID);
                    JSONObject props = new JSONObject();
                    props.put("Device Type", "Android");
                    props.put("Type", "Person");
                    mixpanel.track("SEND", props);
                } catch (JSONException e) {}

                mixpanel.getPeople().identify(mMID);
                mixpanel.getPeople().increment("Arrows Sent", 1);
            }
        });

        mDenyButton = (ImageView) mDialog.findViewById(R.id.deny);
        mDenyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDialog.dismiss();

            }
        });

        mProgress = (ProgressBar) mDialog.findViewById(R.id.progress);

    }

    public void show() {
        mDialog.show();
    }

    private class SendArrowRequest extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            InputStream inputStream = null;
            String result = "";
            try {

                HttpClient client = new DefaultHttpClient();
                HttpPost post= new HttpPost(urls[0]);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("sender", mUserNumber));
                nameValuePairs.add(new BasicNameValuePair("receiver", mOtherNumber));
                nameValuePairs.add(new BasicNameValuePair("contact_name", mOtherName));
                nameValuePairs.add(new BasicNameValuePair("device_type", "android"));
                nameValuePairs.add(new BasicNameValuePair("k", "hdj36dHyFB47dnG7h5FrEEW6E8d99j00JpD3a1M"));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = client.execute(post);

                // receive response as inputStream
                inputStream = response.getEntity().getContent();

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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgress.setVisibility(View.VISIBLE);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // Toast.makeText(getBaseContext(), "Received!" + result, Toast.LENGTH_LONG).show();

            try {
                JSONObject obj = new JSONObject(result);
                int mErrorCode = obj.getInt("error");

                if (mErrorCode == 0) {

                    String mMessage = obj.getString("message");
                    if (!mMessage.isEmpty()) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(mOtherNumber, null,  mMessage, null, null);
                    }

             //       Toast.makeText(mContext, "Success", Toast.LENGTH_LONG).show();

                    ((Activity) mContext).finish();

                } else if (mErrorCode == 3) {

                    ////make this work

                } else if (mErrorCode == 6) {

                    Toast.makeText(mContext, "Arrow already exists!", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 10) {

                    Toast.makeText(mContext, "Server is down", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 11) {

                    Toast.makeText(mContext, "Server error!", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 12) {

                    Toast.makeText(mContext, "Request error.", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 13) {

                    Toast.makeText(mContext, "Unauthorized request", Toast.LENGTH_LONG).show();

                } else if (mErrorCode == 100) {

                    Toast.makeText(mContext, "Oops... Error!", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(mContext, "Unknown error.", Toast.LENGTH_LONG).show();

                }

            } catch (JSONException j) {
                Toast.makeText(mContext, "JSON Error!", Toast.LENGTH_LONG).show();
            }

            mProgress.setVisibility(View.GONE);
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