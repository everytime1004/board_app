package com.example.board.controller;

import java.io.IOException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.board.lib.UrlJsonAsyncTask;

public class GCMSendIdToServer extends UrlJsonAsyncTask {
	private SharedPreferences mPreferences;
	private String regId = null;

	public GCMSendIdToServer(Context context, String regId) {
		super(context);

		this.mPreferences = context.getSharedPreferences("CurrentUser",
				Context.MODE_PRIVATE);
		this.regId = regId;
	}

	@Override
	protected JSONObject doInBackground(String... urls) {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(urls[0]);
		JSONObject holder = new JSONObject();
		JSONObject taskObj = new JSONObject();
		String response = null;
		JSONObject json = new JSONObject();

		try {
			try {
				json.put("success", false);
				json.put("info", "Something went wrong. Retry!");
				taskObj.put("reg_id", mPreferences.getString("regid", regId));
				taskObj.put("noty", mPreferences.getBoolean("noty", true));
				taskObj.put("userName", mPreferences.getString("UserName", "X"));

				holder.put("gcm", taskObj);
				StringEntity se = new StringEntity(holder.toString(), "utf-8");
				post.setEntity(se);
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
				Toast.makeText(context, json.getString("info"),
						Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		} finally {
			super.onPostExecute(json);
		}
	}
}