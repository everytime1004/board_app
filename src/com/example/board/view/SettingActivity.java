package com.example.board.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.board.R;
import com.example.board.controller.ServerUtilities;

public class SettingActivity extends SherlockActivity {

	private SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		mPreferences = getSharedPreferences("noty", MODE_PRIVATE);
	}

	public void saveSettings(View button) {
		CheckBox notyCb = (CheckBox) findViewById(R.id.notyCb);
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

		ServerUtilities.register(this, mPreferences.getString("regId", ""), mPreferences.getBoolean("noty", true));

	}
}