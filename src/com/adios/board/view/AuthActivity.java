package com.adios.board.view;

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
import com.adios.board.R;
import com.adios.board.lib.UrlJsonAsyncTask;
import com.adios.board.model.NetworkInfo;

public class AuthActivity extends SherlockActivity {

	private SharedPreferences mPreferences;

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

		setTitle("로그인");

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		if (mPreferences.contains("AuthToken")) {
			Intent mainIntent = new Intent(this, HomeActivity.class);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainIntent);
			finish();
		}

		if (getIntent().getBooleanExtra("register", false)) {
			setContentView(R.layout.activity_register);
			addItemsOnSpinner();
		} else {
			setContentView(R.layout.activity_sign_in);
		}

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
			Toast.makeText(this, "빈 값을 모두 입력해주세요.", Toast.LENGTH_LONG).show();
			return;
		} else if (mUserName.length() < 3 || mUserName.length() > 13) {
			Toast.makeText(this, "이름은 3자이상 13자 이하를 입력해주세요.", Toast.LENGTH_LONG)
					.show();
			return;
		} else {
			if (!mUserPassword.equals(mUserPasswordConfirmation)) {
				// password doesn't match confirmation
				Toast.makeText(this, "패스워드가 일치하지 않습니다. 다시 입력해주세요.",
						Toast.LENGTH_LONG).show();
				return;
			} else if (mUserPassword.length() < 4
					|| mUserPassword.length() > 20) {
				Toast.makeText(this, "패스워드는 4자이상 20자 이하를 입력해주세요.",
						Toast.LENGTH_LONG).show();
				return;
			} else {
				// everything is ok!
				RegisterTask registerTask = new RegisterTask(AuthActivity.this);
				registerTask.setMessageLoading("회원 가입 중...");
				registerTask.execute(NetworkInfo.REGISTER_API_ENDPOINT_URL);
			}
		}
	}

	public void login(View v) {

		EditText userEmailField = (EditText) findViewById(R.id.userEmail);
		mUserEmail = userEmailField.getText().toString();
		EditText userPasswordField = (EditText) findViewById(R.id.userPassword);
		mUserPassword = userPasswordField.getText().toString();

		if (mUserEmail.length() == 0 || mUserPassword.length() == 0) {
			// input fields are empty
			Toast.makeText(this, "빈 값을 입력해주세요", Toast.LENGTH_LONG).show();
			return;
		} else {
			LoginTask loginTask = new LoginTask(AuthActivity.this);
			loginTask.setMessageLoading("로그인 중...");
			loginTask.setAuthToken(mPreferences.getString("AuthToken", ""));
			loginTask.execute(NetworkInfo.LOGIN_API_ENDPOINT_URL);
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
					json.put("info", "인터넷 연결을 다시 확인해 주세요.");
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
					StringEntity se = new StringEntity(holder.toString(),
							"utf-8");
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
					editor.putString("userName", mUserName);
					editor.putInt("userId",
							json.getJSONObject("data").getInt("user_id"));
					editor.commit();

					// launch the HomeActivity and close this one
					Intent intent = new Intent(AuthActivity.this,
							HomeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();

					Toast.makeText(context, json.getString("info"),
							Toast.LENGTH_LONG).show();
				} else {
					String existEmail = "";
					String invalidName = "";
					if (json.getJSONObject("info").has("email")) {
						existEmail = "이메일이 이미 존재합니다.";
						if (json.getJSONObject("info").has("name")) {
							invalidName = "이름이 존재하거나 유효하지 않는 이름입니다.(특수문자를 빼주세요)";
						}
						Toast.makeText(context,
								existEmail + "\n" + invalidName,
								Toast.LENGTH_LONG).show();
					}

					Intent intent = new Intent(AuthActivity.this,
							AuthActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();

				}
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
					userObj.put("email", mUserEmail);
					userObj.put("password", mUserPassword);
					holder.put("user", userObj);
					// http://rootnode.tistory.com/entry/StringEntity 한글 꺠질 때
					StringEntity se = new StringEntity(holder.toString(),
							"utf-8");
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
					json.put("info", "이메일이나 패스워드가 일치하지 않습니다.");
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
					editor.putString("userName", json.getJSONObject("data")
							.getString("userName"));
					editor.putInt("userId",
							json.getJSONObject("data").getInt("user_id"));
					editor.commit();

					// launch the HomeActivity and close this one
					Intent intent = new Intent(getApplicationContext(),
							HomeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
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
}
