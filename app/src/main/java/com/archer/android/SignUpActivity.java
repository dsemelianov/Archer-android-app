package com.archer.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class SignUpActivity extends Activity {

    protected EditText mUsernameField;
    protected static String mFullName;

    protected EditText mPasswordField;
    protected static String mPassword;

    protected EditText mPhoneNumberField;
    protected static String mPhoneNumber;

    protected EditText mEmailField;
    protected static String mEmail;

    protected Button mContinueButton;
    protected TextView mLoginOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_signup);

        //getActionBar().hide();

        mUsernameField = (EditText) findViewById(R.id.username_field);
        mPasswordField = (EditText) findViewById(R.id.password_field);
        mPhoneNumberField = (EditText) findViewById(R.id.phone_number_field);
        mEmailField = (EditText) findViewById(R.id.email_field);

        mContinueButton = (Button) findViewById(R.id.signup_button);
        mContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mFullName = mUsernameField.getText().toString();
                mFullName = mFullName.trim();

                mPassword = mPasswordField.getText().toString();
                mPassword = mPassword.trim();

                mPhoneNumber = mPhoneNumberField.getText().toString();
                mPhoneNumber = mPhoneNumber.trim();
                mPhoneNumber = mPhoneNumber.replaceAll( "[^\\d]", "" );

                mEmail = mEmailField.getText().toString();
                mEmail = mEmail.trim();
                if (mEmail == null) {
                    mEmail = "";
                } else if (mEmail.isEmpty()) {
                    mEmail = "";
                }

                if (mFullName.isEmpty()) {
                    mContinueButton.setText("Please enter a name!");
                    mContinueButton.setBackgroundColor(Color.RED);
                } else if (mPassword.isEmpty()) {
                    mContinueButton.setText("Please enter a password!");
                    mContinueButton.setBackgroundColor(Color.RED);
                } else if (mPhoneNumber.isEmpty()) {
                    mContinueButton.setText("Please enter a phone number!");
                    mContinueButton.setBackgroundColor(Color.RED);
                } else if ((mPhoneNumber.length() < 10) || (mPhoneNumber.length() > 11)) {
                    mContinueButton.setText("Invalid phone number!");
                    mContinueButton.setBackgroundColor(Color.RED);
                } else {
                    mContinueButton.setText("Loading...");
                    mContinueButton.setBackgroundColor(Color.parseColor("#50D2C2"));
                    new UserCreateTask().execute(getString(R.string.api_url) + "user.json");
                }

            }
        });

        mLoginOption = (TextView) findViewById(R.id.login_option);
        mLoginOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SignUpActivity.this,
                        LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            }
        });

    }

    private class UserCreateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return POST(urls[0]);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            // Toast.makeText(getBaseContext(), "Received!" + result, Toast.LENGTH_LONG).show();

            try {


                JSONObject obj = new JSONObject(result);
                int mErrorCode = obj.getInt("error");

                if (mErrorCode == 0) {

                    mContinueButton.setText("Signed up!");
                    mContinueButton.setBackgroundColor(Color.parseColor("#50D2C2"));

                    //  Toast.makeText(getBaseContext(), "yo yo", Toast.LENGTH_LONG).show();

                    String mMID = obj.getString("mid");

                    int mPin = obj.getInt("pin");

                    SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("mid", mMID);
                    editor.putString("confirmed", "false");
                    editor.putInt("pin", mPin);
                    editor.putString("phone", mPhoneNumber);
                    editor.putString("username", mFullName);
                    editor.apply();

                    Intent intent = new Intent(SignUpActivity.this,
                            ActivityConfirmPhoneNumber.class);
                    startActivity(intent);

                } else if (mErrorCode == 5) {

                    mContinueButton.setText("That number is taken!");
                    mContinueButton.setBackgroundColor(Color.RED);

                } else if (mErrorCode == 10) {

                    mContinueButton.setText("Server is down!");
                    mContinueButton.setBackgroundColor(Color.RED);

                } else if (mErrorCode == 11) {

                    mContinueButton.setText("Server error!");
                    mContinueButton.setBackgroundColor(Color.RED);

                } else if (mErrorCode == 12) {

                    mContinueButton.setText("Request error!");
                    mContinueButton.setBackgroundColor(Color.RED);

                } else if (mErrorCode == 13) {

                    mContinueButton.setText("Unauthorized request!");
                    mContinueButton.setBackgroundColor(Color.RED);

                } else if (mErrorCode == 14) {

                    Toast.makeText(getBaseContext(), "Invalid number!", Toast.LENGTH_LONG).show();
                    mContinueButton.setBackgroundColor(Color.RED);

                } else if (mErrorCode == 100) {

                    mContinueButton.setText("Oops... Error!");
                    mContinueButton.setBackgroundColor(Color.RED);

                } else {

                    mContinueButton.setText("Unknown error!");
                    mContinueButton.setBackgroundColor(Color.RED);

                }

            } catch (JSONException j) {

                String error = j.toString();
                if (error.startsWith("org.json.JSONException: End of input at character 0")) {
                    mContinueButton.setText("No connection!");
                    mContinueButton.setBackgroundColor(Color.RED);
                } else {
                    Toast.makeText(getBaseContext(), error, Toast.LENGTH_LONG).show();
                    mContinueButton.setText("JSON Error!");
                    mContinueButton.setBackgroundColor(Color.RED);
                }

            } catch (Exception e) {
                Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();

                mContinueButton.setText("Something went wrong...");
                mContinueButton.setBackgroundColor(Color.RED);
            }
        }
    }


    public static String POST(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient client = new DefaultHttpClient();
            HttpPost post= new HttpPost(url);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
            nameValuePairs.add(new BasicNameValuePair("full_name", mFullName));
            nameValuePairs.add(new BasicNameValuePair("phone", mPhoneNumber));
            nameValuePairs.add(new BasicNameValuePair("password", mPassword));
            nameValuePairs.add(new BasicNameValuePair("email", mEmail));
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
