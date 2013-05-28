package com.example.board;

import java.util.Iterator;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.example.board.controller.ServerUtilities;
import com.example.board.model.NetworkInfo;
import com.example.board.view.HomeActivity;
import com.google.android.gcm.GCMBaseIntentService;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
	private SharedPreferences mPreferences = null;

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
		// GCMSendIdToServer sendIdToServer = new GCMSendIdToServer(context,
		// registrationId);
		// sendIdToServer.setMessageLoading("GCM Registered...");
		// sendIdToServer.execute(NetworkInfo.GCM_URL);
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

		Iterator<String> iterator_message = b.keySet().iterator();
		generateNotification(context,
				(String) b.get(iterator_message.next().toString()));
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

	// private static void generateNotification(Context context, String content,
	// String[] images, String[] movies) {
	// int icon = R.drawable.ic_launcher;
	// long when = System.currentTimeMillis();
	// NotificationManager notificationManager = (NotificationManager) context
	// .getSystemService(Context.NOTIFICATION_SERVICE);
	//
	// Notification notification = new Notification(icon, content, when);
	//
	// String title = context.getString(R.string.app_name);
	// Intent notificationIntent = new Intent(context, MainActivity.class);
	//
	// notificationIntent.putExtra("content", content);
	// notificationIntent.putExtra("images", images);
	// notificationIntent.putExtra("movies", movies);
	//
	// // set intent so it does not start a new activity
	// notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
	// | Intent.FLAG_ACTIVITY_CLEAR_TOP
	// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	//
	// PendingIntent intent = PendingIntent.getActivity(context, 0,
	// notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	// notification.setLatestEventInfo(context, title, content, intent);
	// notification.flags |= Notification.FLAG_AUTO_CANCEL;
	// notificationManager.notify(0, notification);
	//
	// }

	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(icon, message, when);

		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, HomeActivity.class);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);

		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(0, notification);
	}
}
