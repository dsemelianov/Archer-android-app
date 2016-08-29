package com.archer.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
 * Created by davidemelianov on 2/15/16.
 */
public class TaskArrowAccept extends AsyncTask<String, Void, String> {

    ArrayList<Arrow> mList;
    ArrowAdapter listAdapter;
    Context mContext;

    String mAID;
    String mMID;
    String mName;

    ArrowManager mArrowManager = ArrowManager.getInstance();

    public TaskArrowAccept(ArrayList<Arrow> list, ArrowAdapter list_adapter, Context context, String aid, String mid, String name) {

        this.mList = list;
        this.listAdapter = list_adapter;
        this.mContext = context;

        this.mAID = aid;
        this.mMID = mid;
        this.mName = name;

    }

    @Override
    protected String doInBackground(String... urls) {

        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient client = new DefaultHttpClient();
            HttpPost post= new HttpPost(urls[0]);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("aid", mAID));
            nameValuePairs.add(new BasicNameValuePair("receiver_name", mName));
            nameValuePairs.add(new BasicNameValuePair("sender_mid", mMID));
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

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {

        try {

            JSONObject obj = new JSONObject(result);
            int mErrorCode = obj.getInt("error");

            if (mErrorCode == 0) {

                String aid = obj.getString("aid");

                //search for the arrow that was just approved
                for (int i = 0; i < mList.size(); i++) {

                    if (mList.get(i).getAID().equals(aid)) {

                //        Toast.makeText(mContext, NUMBER_OF_ACTIVE + " " + NUMBER_OF_REQUESTS, Toast.LENGTH_LONG).show();

                        //change the approved arrow status to ACTIVE
                        Arrow approvedArrow = mList.get(i);
                        approvedArrow.changeType("ACTIVE");
                        mList.remove(i);
                        mArrowManager.decrementNUMBER_OF_REQUESTS();
                        //if there aren't any requests left, remove the whole section
                        if (mArrowManager.getNUMBER_OF_REQUESTS() == 0) {
                            mList.remove(0);
                        }

                        addToList(approvedArrow);

                        break;

                    }
                }

            } else if (mErrorCode == 7) {

                Toast.makeText(mContext, "Already approved!", Toast.LENGTH_LONG).show();

            } else if (mErrorCode == 8) {

                Toast.makeText(mContext, "Already denied!", Toast.LENGTH_LONG).show();

            } else if (mErrorCode == 9) {

                Toast.makeText(mContext, "Arrow deleted...", Toast.LENGTH_LONG).show();

            } else if (mErrorCode == 10) {

                Toast.makeText(mContext, "The server is down!", Toast.LENGTH_LONG).show();

            } else if (mErrorCode == 11) {

                Toast.makeText(mContext, "Server error!", Toast.LENGTH_LONG).show();

            } else if (mErrorCode == 13) {

                Toast.makeText(mContext, "Incorrect key", Toast.LENGTH_LONG).show();

            } else if (mErrorCode == 100) {

                Toast.makeText(mContext, "Oops... Error!", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(mContext, "Unknown error!", Toast.LENGTH_LONG).show();

            }

        } catch (JSONException j) {

            Toast.makeText(mContext, j.toString(), Toast.LENGTH_LONG).show();

        }

    }

    private void addToList(Arrow approvedArrow) {
        //THIS METHOD FUNCTIONS AS A GENERAL ADD FUNCTION FOR ALL TYPES OF ARROWS

        //this if statement is never used in this case, because we're always adding an Active arrow
        if (approvedArrow.getType().equals("REQUEST")) {

            //if it's the first request, add the REQUESTS banner
            if (mArrowManager.getNUMBER_OF_REQUESTS() == 0) {
                Arrow mRequestsSeparator = new Arrow("SEPARATOR");
                mRequestsSeparator.setName("REQUESTS");
                mList.add(0, mRequestsSeparator);
            }
            mList.add(1, approvedArrow);
            mArrowManager.incrementNUMBER_OF_REQUESTS();

        } else if (approvedArrow.getType().equals("ACTIVE")) {

            //if it's the first approved arrow, remove the dummy prompt row
            if (mArrowManager.getNUMBER_OF_ACTIVE() == 0) {

                if (mArrowManager.getNUMBER_OF_REQUESTS() == 0) {
                    mList.remove(1);
                } else {
                    mList.remove(mArrowManager.getNUMBER_OF_REQUESTS() + 2);
                }

            }

            if (mArrowManager.getNUMBER_OF_REQUESTS() == 0) {

                //if there are no requests, throw the active arrow on the top, beneath the first banner
                mList.add(1, approvedArrow);

            } else {

                //if there are requests, put the active arrow after the REQUEST banner, the requests, and the ACTIVE banner
                mList.add(mArrowManager.getNUMBER_OF_REQUESTS()+2, approvedArrow);
            }

            mArrowManager.incrementNUMBER_OF_ACTIVE();

            //this if statement is never used in this case, because we're always adding an Active arrow
        } else if (approvedArrow.getType().equals("SPONSORED")) {

            //if it's the first request, add the SPONSORED banner
            if (mArrowManager.getNUMBER_OF_SPONSORED() == 0) {
                Arrow mSponsoredSeparator = new Arrow("SEPARATOR");
                mSponsoredSeparator.setName("SPONSORED");
                mList.add(mSponsoredSeparator);
            }
            mList.add(approvedArrow);

            mArrowManager.incrementNUMBER_OF_SPONSORED();

        }

        listAdapter.notifyDataSetChanged();
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