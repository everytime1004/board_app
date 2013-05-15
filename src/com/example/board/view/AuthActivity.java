package com.example.board.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.board.R;
import com.example.board.lib.UrlJsonAsyncTask;
import com.example.board.model.NetworkInfo;

public class AuthActivity extends SherlockActivity {

	private SharedPreferences mPreferences;

	private final static String LOGIN_API_ENDPOINT_URL = "http://"
			+ NetworkInfo.IP + "/api/v1/sessions.json";
	private final static String REGISTER_API_ENDPOINT_URL = "http://"
			+ NetworkInfo.IP + "/api/v1/registrations";

	private String mUserEmail;
	private String mUserName;
	private String mUserPassword;
	private String mUserPasswordConfirmation;
	private String mPhone_first;
	private String mPhone_second;
	private String mPhone_third;

	private Spinner phoneSelect;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getIntent().getBooleanExtra("register", false)) {
			setContentView(R.layout.activity_register);
			addItemsOnSpinner();
		} else {
			setContentView(R.layout.activity_sign_in);
		}

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.auth, menu);

		if (getIntent().getBooleanExtra("register", false)) {
			menu.findItem(R.id.action_registeration).setVisible(false);
		} else {
			menu.findItem(R.id.action_registeration).setVisible(true);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_registeration:
			Intent registerIntent = new Intent(this, AuthActivity.class);
			registerIntent.putExtra("register", true);
			startActivity(registerIntent);
			finish();
			break;
		}

		return true;
	}

	public void addItemsOnSpinner() {
		phoneSelect = (Spinner) findViewById(R.id.phone_first);
		List<String> list = new ArrayList<String>();
		list.add("010");
		list.add("011");
		list.add("016");
		list.add("017");
		list.add("018");
		list.add("019");
		ArrayAdapter<String> phoneSelectAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, list);
		phoneSelectAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		phoneSelect.setAdapter(phoneSelectAdapter);
	}

	public void registerNewAccount(View v) {
		EditText userEmailField = (EditText) findViewById(R.id.userEmail);
		mUserEmail = userEmailField.getText().toString();
		EditText userNameField = (EditText) findViewById(R.id.userName);
		mUserName = userNameField.getText().toString();
		EditText userPasswordField = (EditText) findViewById(R.id.userPassword);
		mUserPassword = userPasswordField.getText().toString();
		EditText userPasswordConfirmationField = (EditText) findViewById(R.id.userPasswordConfirmation);
		mUserPasswordConfirmation = userPasswordConfirmationField.getText()
				.toString();
		Spinner userPhoneField = (Spinner) findViewById(R.id.phone_first);
		mPhone_first = String.valueOf(userPhoneField.getSelectedItem());
		EditText userPhoneSecondField = (EditText) findViewById(R.id.phone_second);
		mPhone_second = String.valueOf(userPhoneSecondField.getText()
				.toString());
		EditText userPhoneThirdField = (EditText) findViewById(R.id.phone_third);
		mPhone_third = String.valueOf(userPhoneThirdField.getText().toString());

		if (mUserEmail.length() == 0 || mUserName.length() == 0
				|| mUserPassword.length() == 0
				|| mUserPasswordConfirmation.length() == 0) {
			// input fields are empty
			Toast.makeText(this, "Please complete all the fields",
					Toast.LENGTH_LONG).show();
			return;
		} else {
			if (!mUserPassword.equals(mUserPasswordConfirmation)) {
				// password doesn't match confirmation
				Toast.makeText(
						this,
						"Your password doesn't match confirmation, check again",
						Toast.LENGTH_LONG).show();
				return;
			} else {
				// everything is ok!
				RegisterTask registerTask = new RegisterTask(AuthActivity.this);
				registerTask.setMessageLoading("Registering new account...");
				registerTask.execute(REGISTER_API_ENDPOINT_URL);
			}
		}
	}

	public void login(View v) {

		EditText userEmailField = (EditText) findViewById(R.id.userEmail);
		mUserName = userEmailField.getText().toString();
		EditText userPasswordField = (EditText) findViewById(R.id.userPassword);
		mUserPassword = userPasswordField.getText().toString();

		if (mUserName.length() == 0 || mUserPassword.length() == 0) {
			// input fields are empty
			Toast.makeText(this, "Please complete all the fields",
					Toast.LENGTH_LONG).show();
			return;
		} else {
			LoginTask loginTask = new LoginTask(AuthActivity.this);
			loginTask.setMessageLoading("Logging in...");
			loginTask.setAuthToken(mPreferences.getString("AuthToken", ""));
			loginTask.execute(LOGIN_API_ENDPOINT_URL);
		}

	}

	private class RegisterTask extends UrlJsonAsyncTask {
		public RegisterTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... urls) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urls[0]);
			JSONObject holder = new JSONObject();
			JSONObject userObj = new JSONObject();
			String response = null;
			JSONObject json = new JSONObject();

			try {
				try {
					// setup the returned values in case
					// something goes wrong
					json.put("success", false);
					json.put("info", "Something went wrong. Retry!");

					// add the users's info to the post params
					userObj.put("email", mUserEmail);
					userObj.put("name", mUserName);
					userObj.put("password", mUserPassword);
					userObj.put("password_confirmation",
							mUserPasswordConfirmation);
					userObj.put("phone_first", mPhone_first);
					userObj.put("phone_second", mPhone_second);
					userObj.put("phone_third", mPhone_third);
					userObj.put("phone", mPhone_first + mPhone_second
							+ mPhone_third);
					holder.put("user", userObj);
					StringEntity se = new StringEntity(holder.toString());
					post.setEntity(se);

					// setup the request headers
					post.setHeader("Accept", "application/json");
					post.setHeader("Content-Type", "application/json");

					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					response = client.execute(post, responseHandler);
					json = new JSONObject(response);

				} catch (HttpResponseException e) {
					e.printStackTrace();
					Log.e("ClientProtocol", "" + e);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("IO", "" + e);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e("JSON", "" + e);
			}

			return json;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if (json.getBoolean("success")) {
					// everything is ok
					SharedPreferences.Editor editor = mPreferences.edit();
					// save the returned auth_token into
					// the SharedPreferences
					editor.putString("AuthToken", json.getJSONObject("data")
							.getString("auth_token"));
					editor.commit();

					// launch the HomeActivity and close this one
					Intent intent = new Intent(getApplicationContext(),
							HomeActivity.class);
					startActivity(intent);
				}
				Toast.makeText(context, json.getString("info"),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// something went wrong: show a Toast
				// with the exception message
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			} finally {
				super.onPostExecute(json);
			}
		}
	}

	private class LoginTask extends UrlJsonAsyncTask {
		public LoginTask(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... urls) {
			// #doInBackground 의 리턴값은 onPostExecute함수의 인자로 보내진다.
			// doInBackgroud함수에서 어느때든 publishProgress()를 호출하여 UI Thread에서
			// onProgressUpdate() 함수가 실행되게 할 수 있다.
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(urls[0]);
			JSONObject holder = new JSONObject();
			JSONObject userObj = new JSONObject();
			String response = null;
			JSONObject json = new JSONObject();

			try {
				try {
					// setup the returned values in case
					// something goes wrong
					json.put("success", false);
					json.put("info", "Something went wrong. Retry!");
					// add the user email and password to
					// the params
					userObj.put("name", mUserName);
					userObj.put("password", mUserPassword);
					holder.put("user", userObj);
					// http://rootnode.tistory.com/entry/StringEntity 한글 꺠질 때
					StringEntity se = new StringEntity(holder.toString());
					post.setEntity(se);

					// setup the request headers
					post.setHeader("Accept", "application/json");
					post.setHeader("Content-Type", "application/json");

					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					response = client.execute(post, responseHandler);
					json = new JSONObject(response);

				} catch (HttpResponseException e) {
					e.printStackTrace();
					Log.e("ClientProtocol", "" + e);
					json.put("info",
							"UserName and/or password are invalid. Retry!");
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("IO", "" + e);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				Log.e("JSON", "" + e);
			}

			return json;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if (json.getBoolean("success")) {
					// everything is ok
					SharedPreferences.Editor editor = mPreferences.edit();
					// save the returned auth_token into
					// the SharedPreferences
					editor.putString("AuthToken", json.getJSONObject("data")
							.getString("auth_token"));
					editor.putString("UserName", mUserName);
					editor.commit();

					// launch the HomeActivity and close this one
					Intent intent = new Intent(getApplicationContext(),
							HomeActivity.class);
					startActivity(intent);
				}
				Toast.makeText(context, json.getString("info"),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				// something went wrong: show a Toast
				// with the exception message
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			} finally {
				super.onPostExecute(json);
				finish();
			}
		}
	}
}
