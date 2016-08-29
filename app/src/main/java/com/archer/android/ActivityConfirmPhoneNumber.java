package com.archer.android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityConfirmPhoneNumber extends Activity {

	protected EditText mPINField;

    protected TextView mResend;
    protected boolean canResend = true;

	protected ImageView mContinueButton;

    int mPIN;
    String mMID;

    final String YOUR_PROJECT_TOKEN = "4864ac47bd7beb9dce3cbe039d7814fe";
    MixpanelAPI mixpanel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_confirm);

        String projectToken = YOUR_PROJECT_TOKEN; // e.g.: "1ef7e30d2a58d27f4b90c42e31d6d7ad"
        mixpanel = MixpanelAPI.getInstance(this, projectToken);

        SharedPreferences shared = getSharedPreferences("shared", MODE_PRIVATE);
        mMID = shared.getString("mid", "null");
        mPIN = shared.getInt("pin", 0);

		//getActionBar().hide();

		mPINField = (EditText) findViewById(R.id.phone_number_field);

		mContinueButton = (ImageView) findViewById(R.id.continue_button);
		mContinueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

                String pin = mPINField.getText().toString().trim();
                if (pin.equals(String.valueOf(mPIN))) {

                  //  Toast.makeText(getBaseContext(), "confirming...", Toast.LENGTH_LONG).show();


                    new TaskConfirmPIN(ActivityConfirmPhoneNumber.this).execute(getString(R.string.api_url) + "user/confirm/" + mMID + getString(R.string.api_key));

                } else {

                    Toast.makeText(getBaseContext(), "Oops! Try again.", Toast.LENGTH_LONG).show();

                }

			}
		});

        mResend = (TextView) findViewById(R.id.resend);
        mResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (canResend) {
                    mResend.setText("Hold on...");
                    canResend = false;

                    new TaskResendPIN(ActivityConfirmPhoneNumber.this, mResend).execute(getString(R.string.api_url) + "user/resend/" + mMID + getString(R.string.api_key));

                    try {
                        mixpanel.identify(mMID);
                        JSONObject props = new JSONObject();
                        props.put("Device Type", "Android");
                        mixpanel.track("RESEND CODE", props);
                    } catch (JSONException e) {}
                }

            }
        });

	}
}
