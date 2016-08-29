package com.archer.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by davidemelianov on 2/15/16.
 */
public class TaskDeleteArrow extends AsyncTask<String, Void, String> {

    ArrayList<Arrow> mList;
    ArrowAdapter listAdapter;
    Context mContext;

    ArrowManager mArrowManager = ArrowManager.getInstance();

    public TaskDeleteArrow(ArrayList<Arrow> list, ArrowAdapter list_adapter, Context context) {

        this.mList = list;
        this.listAdapter = list_adapter;
        this.mContext = context;
    }

    @Override
    protected String doInBackground(String... urls) {

        //GET task
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpDelete(urls[0]));

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

                        //remove the arrow
                        mList.remove(i);
                        mArrowManager.decrementNUMBER_OF_ACTIVE();

                        //add a prompt row if empty
                        if (mArrowManager.getNUMBER_OF_ACTIVE() == 0) {

                            Arrow mPromptArrow = new Arrow("PROMPT");
                            mList.add(i, mPromptArrow);

                        }

                        listAdapter.notifyDataSetChanged();

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