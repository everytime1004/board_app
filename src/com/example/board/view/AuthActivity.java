package com.example.board.view;

import com.example.board.R;
import com.example.board.R.layout;
import com.example.board.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class AuthActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.auth, menu);
		return true;
	}

}
