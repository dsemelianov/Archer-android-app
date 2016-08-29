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

public class LoginActivity extends Activity {

	protected EditText mPhoneNumberField;
	protected static String mPhoneNumber;

	protected EditText mPasswordField;
	protected static String mPassword;

	protected Button mContinueButton;
	protected TextView mSignUpOption;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);

		//getActionBar().hide();

		mPhoneNumberField = (EditText) findViewById(R.id.username_field);
		mPasswordField = (EditText) findViewById(R.id.password_field);

		mContinueButton = (Button) findViewById(R.id.login_button);
		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				mPhoneNumber = mPhoneNumberField.getText().toString();
				mPhoneNumber = mPhoneNumber.trim();
				mPhoneNumber = mPhoneNumber.replaceAll("[^\\d]", "");

				mPassword = mPasswordField.getText().toString();
				mPassword = mPassword.trim();

				mContinueButton.setText("Loading!");
				mContinueButton.setBackgroundColor(Color.parseColor("#50D2C2"));

				new UserAuthenticateTask().execute(getString(R.string.api_url) + "user/auth.json/");

			}
		});

		mSignUpOption = (TextView) findViewById(R.id.signup_option);
		mSignUpOption.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(LoginActivity.this,
						SignUpActivity.class);
				startActivity(intent);

			}
		});

	}

	private class UserAuthenticateTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {

			InputStream inputStream = null;
			String result = "";
			try {

				HttpClient client = new DefaultHttpClient();
				HttpPost post= new HttpPost(urls[0]);

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("phone", mPhoneNumber));
				nameValuePairs.add(new BasicNameValuePair("password", mPassword));
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
			// Toast.makeText(getBaseContext(), "Received!" + result, Toast.LENGTH_LONG).show();

			try {
				JSONObject obj = new JSONObject(result);
				int mErrorCode = obj.getInt("error");

				if (mErrorCode == 0) {

                    mContinueButton.setText("Logged in!");
                    mContinueButton.setBackgroundColor(Color.parseColor("#50D2C2"));

                    String mMID = obj.getString("mid");
                    String mConfirmed= obj.getString("confirmed");
					String mUserName = obj.getString("full_name");

                    SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("mid", mMID);
                    editor.putString("confirmed", mConfirmed);
					editor.putString("phone", mPhoneNumber);
					editor.putString("username", mUserName);

                    if (!mConfirmed.equals("t")) {

                        Integer mPIN = obj.getInt("pin");
                        editor.putInt("pin", mPIN);
                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this,
                                ActivityConfirmPhoneNumber.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {

                        editor.apply();

                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

				} else if (mErrorCode == 3) {

                    mContinueButton.setText("Invalid phone number!");
                    mContinueButton.setBackgroundColor(Color.RED);

                } else if (mErrorCode == 4) {

                    mContinueButton.setText("Incorrect password!");
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
