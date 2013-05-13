package com.example.board.view;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.board.R;

public class HomeActivity extends SherlockActivity {

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
