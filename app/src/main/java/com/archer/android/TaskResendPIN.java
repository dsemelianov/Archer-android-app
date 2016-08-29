package com.archer.android;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by davidemelianov on 2/15/16.
 */
public class TaskResendPIN extends AsyncTask<String, Void, String> {

    Context mContext;
    TextView mResend;

    public TaskResendPIN(Context context, TextView resend) {

        this.mContext = context;
        this.mResend = resend;
    }

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

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {

        try {

            JSONObject obj = new JSONObject(result);
            int mErrorCode = obj.getInt("error");

            if (mErrorCode == 0) {

                mResend.setText("Text message resent!");

            } else if (mErrorCode == 1) {

                mResend.setText("User ID error!");

            } else if (mErrorCode == 10) {

                mResend.setText("Server is down!");

            } else if (mErrorCode == 11) {

                mResend.setText("Server error!");

            } else if (mErrorCode == 12) {

                mResend.setText("Request error!");

            } else if (mErrorCode == 13) {

                mResend.setText("Unauthorized request");

            } else if (mErrorCode == 14) {

                mResend.setText("Invalid phone number...");

            } else if (mErrorCode == 100) {

                Toast.makeText(mContext, "Oops... Error!", Toast.LENGTH_LONG).show();

            } else {

                Toast.makeText(mContext, "Unknown error...", Toast.LENGTH_LONG).show();

            }

        } catch (JSONException j) {
            Toast.makeText(mContext, j.toString(), Toast.LENGTH_LONG).show();


        } catch (Exception e) {
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();

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