package com.adios.board;

import java.util.Iterator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.adios.board.controller.ServerUtilities;
import com.adios.board.model.NetworkInfo;
import com.adios.board.view.HomeActivity;
import com.google.android.gcm.GCMBaseIntentService;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
	public static SharedPreferences mPreferences = null;

	public GCMIntentService() {
		super(NetworkInfo.PROJECT_ID);
		Log.d("GCM Check", "check");
	}

	public GCMIntentService(String project_id) {
		super(project_id);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString("regId", registrationId);
		editor.putBoolean("noty", true);
		editor.commit();

		ServerUtilities.register(context, registrationId,
				mPreferences.getBoolean("noty", true),
				mPreferences.getString("userName", ""));
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		// ServerUtilities.unregister(context, registrationId);
	}

	/** 푸시로 받은 메시지 */
	@Override
	protected void onMessage(Context context, Intent intent) {
		Bundle b = intent.getExtras();
		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);
		// b = Bundle[{score=asdf, collapse_key=updated_score,
		// from=180594026587}]

		Iterator<String> iterator = b.keySet().iterator();
		// iterator = java.util.HashMap$KeyIterator@422c41a0 이렇게 넘어옴
		while (iterator.hasNext()) {
			String key = iterator.next();
			// 첫 번 째 key값은 score
			String value = b.get(key).toString();
			Log.d(TAG, "onMessage. " + key + " : " + value);
		}

		String message = b.get("message").toString();
		String title = b.get("title").toString();
		String category = b.get("category").toString();
		String description = b.get("description").toString();
		String post_id = b.get("post_id").toString();

		/**
		 * eXtra[0] : message eXtra[1] : title eXtra[2] : category eXtra[3] :
		 * description eXtra[4] : post_id
		 */
		String[] eXtra = { message, title, category, description, post_id };

		generateNotification(context, eXtra);
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
		// String message = getString(R.string.gcm_deleted, total);
		// notifies user
		// generateNotification(context, message);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * eXtra[0] : message eXtra[1] : title eXtra[2] : category eXtra[3] :
	 * description eXtra[4] : post_id
	 */
	private static void generateNotification(Context context, String[] eXtra) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(icon, eXtra[0], when);

		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, HomeActivity.class);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// 인텐트에 넣어도 getIntent해도 extra값 못 받아와서 preference이용.....
		notificationIntent.putExtra("isPushIntent", true);
		notificationIntent.putExtra("title", eXtra[1]);
		notificationIntent.putExtra("category", eXtra[2]);
		notificationIntent.putExtra("description", eXtra[3]);
		notificationIntent.putExtra("post_id", Integer.parseInt(eXtra[4]));

		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, title, eXtra[0], intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
	}
}