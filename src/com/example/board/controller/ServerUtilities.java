/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.board.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.board.model.NetworkInfo;
import com.google.android.gcm.GCMRegistrar;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {
	public static final String TAG = "Board";

	private static final int MAX_ATTEMPTS = 5;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();
	private static AsyncTask<String, Void, JSONObject> gcmRegisterTask;

	/**
	 * Register this account/device pair within the server.
	 * 
	 */
	public static void register(final Context context, final String regId,
			final boolean noty) {
		// YSLIM : LOG
		Log.i(TAG, "registering device (regId = " + regId + ")");

		// String serverUrl = CONST.SERVER_IP + "/register";
		// Map<String, String> params = new HashMap<String, String>();
		// params.put("regId", regId);

		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

		// Once GCM returns a registration id, we need to register it in the
		// demo server. As the server might be down, we will retry it a couple
		// times.

		// YSLIM : GCM이 registration id를 리턴한 뒤에는, demo server에 그것을 등록할 필요가 있다.
		// server down 때문에, 그것을 여러번 재시도할 것이다.
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			// YSLIM : LOG
			Log.d(TAG, "Attempt #" + i + " to register");

			try {

				// YSLIM : Server post
				sendAppToServer(context, regId, noty);

				// Google API : Sets whether the device was successfully
				// registered in the server side
				// YSLIM : 성공적으로 등록이 되었을경우 GCM 서버에 등록확인
				GCMRegistrar.setRegisteredOnServer(context, true);

			} catch (IOException e) {
				// Here we are simplifying and retrying on any error; in a real
				// application, it should retry only on unrecoverable errors
				// (like HTTP error code 503).
				Log.e(TAG, "Failed to register on attempt " + i + ":" + e);
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Log.d(TAG, "Thread interrupted: abort remaining retries!");
					Thread.currentThread().interrupt();
					return;
				}
				// increase backoff exponentially
				backoff *= 2;
			}
		}

	}

	/**
	 * Unregister this account/device pair within the server.
	 */
	static void unregister(final Context context, final String regId) {
		Log.i(TAG, "unregistering device (regId = " + regId + ")");
		String serverUrl = NetworkInfo.IP + "/unregister";
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		try {
			post(serverUrl, params);
			GCMRegistrar.setRegisteredOnServer(context, false);
		} catch (IOException e) {
			// At this point the device is unregistered from GCM, but still
			// registered in the server.
			// We could try to unregister again, but it is not necessary:
			// if the server tries to send a message to the device, it will get
			// a "NotRegistered" error message and should unregister the device.
		}
	}

	/**
	 * Issue a POST request to the server.
	 * 
	 * @param endpoint
	 *            POST address.
	 * @param params
	 *            request parameters.
	 * 
	 * @throws IOException
	 *             propagated from POST.
	 */
	private static void post(String endpoint, Map<String, String> params)
			throws IOException {
		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		Log.v(TAG, "Posting '" + body + "' to " + url);
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("Post failed with error code " + status);
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * Issue a GET request to the server.
	 * 
	 * @param context
	 *            context
	 * @param regId
	 *            register ID
	 * @param noty
	 *            noty(받을지 안 받을지 환경설정 true false 값)
	 * 
	 * @throws IOException
	 *             propagated from POST.
	 */

	private static void sendAppToServer(final Context context,
			final String regId, final boolean noty) throws IOException {
		gcmRegisterTask = new AsyncTask<String, Void, JSONObject>() {

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
						taskObj.put("reg_id", regId);
						taskObj.put("noty", noty);

						holder.put("gcm", taskObj);
						StringEntity se = new StringEntity(holder.toString(),
								"utf-8");
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
//						Toast.makeText(context, json.getString("info"),
//								Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
							.show();
				} finally {
					super.onPostExecute(json);
				}
			}

		};
		gcmRegisterTask.execute(NetworkInfo.GCM_URL, null, null);
		// DefaultHttpClient client = new DefaultHttpClient();
		// HttpPost post = new HttpPost(NetworkInfo.GCM_URL);
		// JSONObject holder = new JSONObject();
		// JSONObject taskObj = new JSONObject();
		// String response = null;
		// JSONObject json = new JSONObject();
		//
		// try {
		// try {
		// json.put("success", false);
		// json.put("info", "Something went wrong. Retry!");
		// taskObj.put("reg_id", regId);
		// taskObj.put("noty", noty);
		//
		// holder.put("gcm", taskObj);
		// StringEntity se = new StringEntity(holder.toString(), "utf-8");
		// post.setEntity(se);
		// post.setHeader("Accept", "application/json");
		// post.setHeader("Content-Type", "application/json");
		//
		// ResponseHandler<String> responseHandler = new BasicResponseHandler();
		// response = client.execute(post, responseHandler);
		// json = new JSONObject(response);
		//
		// } catch (HttpResponseException e) {
		// e.printStackTrace();
		// Log.e("ClientProtocol", "" + e);
		// } catch (IOException e) {
		// e.printStackTrace();
		// Log.e("IO", "" + e);
		// }
		// } catch (JSONException e) {
		// e.printStackTrace();
		// Log.e("JSON", "" + e);
		// }
	}

}
