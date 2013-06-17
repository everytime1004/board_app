package com.adios.board.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.actionbarsherlock.app.SherlockActivity;
import com.adios.board.R;
import com.adios.board.controller.ServerUtilities;

public class SettingActivity extends SherlockActivity {

	private SharedPreferences mPreferences;
	private CheckBox notyCb = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		notyCb = (CheckBox) findViewById(R.id.notyCb);

		if (mPreferences.getBoolean("noty", false)) {
			notyCb.setChecked(true);
		} else {
			notyCb.setChecked(false);
		}
	}

	public void saveSettings(View button) {
		if (notyCb.isChecked()) {
			SharedPreferences.Editor editor = mPreferences.edit();
			// save the returned auth_token into
			// the SharedPreferences
			editor.putBoolean("noty", true);
			editor.commit();
		} else {
			SharedPreferences.Editor editor = mPreferences.edit();
			// save the returned auth_token into
			// the SharedPreferences
			editor.putBoolean("noty", false);
			editor.commit();
		}

		ServerUtilities.register(getApplicationContext(),
				mPreferences.getString("regId", ""),
				mPreferences.getBoolean("noty", true),
				mPreferences.getString("userName", ""));

	}
}