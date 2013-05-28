package com.example.board.view;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.board.R;
import com.example.board.controller.ServerUtilities;
import com.example.board.model.NetworkInfo;
import com.google.android.gcm.GCMRegistrar;

public class HomeActivity extends SherlockActivity {

	private SharedPreferences mPreferences;

	private String regId = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			ActionBar actionBar = getActionBar();
			// actionbar setting
			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
					| ActionBar.NAVIGATION_MODE_STANDARD
					| ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_SHOW_HOME);

			mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);
		}

		registGCM();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.home, menu);

		if (mPreferences.contains("AuthToken")) {
			menu.findItem(R.id.action_auth).setTitle("로그 아웃");

		} else {
			menu.findItem(R.id.action_auth).setTitle("로그인");
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_auth:
			if (item.getTitle().equals("로그인")) {
				Intent loginIntent = new Intent(this, AuthActivity.class);
				startActivity(loginIntent);
				break;
			} else {
				SharedPreferences.Editor editor = mPreferences.edit();
				// save the returned auth_token into
				// the SharedPreferences
				editor.remove("AuthToken");
				editor.commit();

				Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_LONG).show();

				item.setTitle("로그인");
				break;
			}

		case R.id.action_setting:
			Intent settingIntent = new Intent(this, SettingActivity.class);
			startActivity(settingIntent);
			break;
		}

		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		// GCM Code
		if (gcmRegisterTask != null) {
			gcmRegisterTask.cancel(true);
		}

		/* GCM Local state */
		GCMRegistrar.onDestroy(getApplicationContext());
	}

	AsyncTask<Void, Void, Void> gcmRegisterTask;

	private void registGCM() {

		// Make sure the device has the proper dependencies.
		// GCMRegistrar.checkDevice(this);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(getApplicationContext(),
					NetworkInfo.PROJECT_ID);
		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(getApplicationContext())) {
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;

				gcmRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						ServerUtilities.register(context, regId, true,
								mPreferences.getString("userName", ""));
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						gcmRegisterTask = null;
					}

				};
				gcmRegisterTask.execute(null, null, null);
			}
		}
	}

	public void showBoard(View v) {

		Intent showIntent = new Intent(this, PostIndexActivity.class);

		switch (v.getId()) {
		case R.id.boardBuyBtn:
			showIntent.putExtra("category", "삽니다");
			startActivity(showIntent);
			break;
		case R.id.boardSellBtn:
			showIntent.putExtra("category", "팝니다");
			startActivity(showIntent);
			break;
		case R.id.boardInquiryBtn:
			showIntent.putExtra("category", "문의 및 견적의뢰");
			startActivity(showIntent);
			break;
		case R.id.boardSellCompleteBtn:
			showIntent.putExtra("category", "판매 완료");
			startActivity(showIntent);
			break;
		}
	}

}
