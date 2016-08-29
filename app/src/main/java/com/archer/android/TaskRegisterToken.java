package com.archer.android;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidemelianov on 2/15/16.
 */
public class TaskRegisterToken extends AsyncTask<String, Void, String> {

    String mMID;
    String mToken;

    public TaskRegisterToken(String mid, String token) {

        this.mMID = mid;
        this.mToken = token;
    }

    @Override
    protected String doInBackground(String... urls) {

        InputStream inputStream = null;
        String result = "";

        if ((mMID != null) && (mToken != null)) {

            try {

                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(urls[0]);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("mid", mMID));
                nameValuePairs.add(new BasicNameValuePair("device_token", mToken));
                nameValuePairs.add(new BasicNameValuePair("device_type", "android"));
                nameValuePairs.add(new BasicNameValuePair("k", "hdj36dHyFB47dnG7h5FrEEW6E8d99j00JpD3a1M"));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = client.execute(post);

                // receive response as inputStream
                inputStream = response.getEntity().getContent();

                // convert inputstream to string
                if (inputStream != null)
                    result = convertInputStreamToString(inputStream);
                else
                    result = "Did not work!";

            } catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
                //mCurrent.setText("Oops");
            }

        }

        return result;

    }
    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {
        // Toast.makeText(getBaseContext(), "Received!" + result, Toast.LENGTH_LONG).show();

        //do error handling here

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