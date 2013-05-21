package com.example.board.view;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.board.R;
import com.example.board.controller.CacheManager;
import com.example.board.lib.UrlJsonAsyncTask;
import com.example.board.model.NetworkInfo;

public class PostShowActivity extends SherlockActivity {

	private static int mPostId = 0;

	private static String SHOW_TASK_ENDPOINT_URL;

	private SharedPreferences mPreferences;

	private String[] imageBitMapURL = new String[5];

	private Bitmap[] imageBitmap = new Bitmap[5];

	private ImageView[] showImage = new ImageView[5];

	int mImageWidth = 0;
	int mImageHeight = 0;
	int newImageWidth = 400;
	int newImageHeight = 400;
	float scaleWidth = 0;
	float scaleHeight = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_show);

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		TextView task_show_title = (TextView) findViewById(R.id.task_show_title);
		TextView task_show_description = (TextView) findViewById(R.id.task_show_description);

		Intent taskIntent = getIntent();

		task_show_title.setText(taskIntent.getStringExtra("title"));
		task_show_description.setText(taskIntent.getStringExtra("description"));
		mPostId = taskIntent.getIntExtra("post_id", 0);

		SHOW_TASK_ENDPOINT_URL = NetworkInfo.IP + "/api/v1/posts/" + mPostId
				+ ".json";

		ShowTaskTask showTask = new ShowTaskTask(PostShowActivity.this);
		showTask.setMessageLoading("Loading task...");
		showTask.setAuthToken(mPreferences.getString("AuthToken", ""));
		showTask.execute(SHOW_TASK_ENDPOINT_URL);
	}

	class BitmapDownloaderTask extends AsyncTask<String, Void, Boolean> {

		public BitmapDownloaderTask() {
		}

		@Override
		// Actual download method, run in the task thread
		protected Boolean doInBackground(String... params) {
			// params comes from the execute() call: params[0] is the url.
			showImage[0] = (ImageView) findViewById(R.id.showImage1);
			showImage[1] = (ImageView) findViewById(R.id.showImage2);
			showImage[2] = (ImageView) findViewById(R.id.showImage3);
			showImage[3] = (ImageView) findViewById(R.id.showImage4);
			showImage[4] = (ImageView) findViewById(R.id.showImage5);

			// URL로부터 image 받아옴
			for (int i = 0; i < 5; i++) {
				if (imageBitMapURL[i] == null)
					continue;
				imageBitmap[i] = downloadBitmap(imageBitMapURL[i]);

			}

			return true;
		}

		@Override
		// Once the image is downloaded, associates it to the imageView
		protected void onPostExecute(Boolean bool) {
			if (isCancelled()) {
				bool = false;
			}

			// 비트맵이 널이 아니면 원래 image 초기화 시키고 새로운 이미지 넣고 보여지게 함
			for (int k = 0; k < 5; k++) {
				if (imageBitmap[k] != null) {
					showImage[k].setImageResource(0);
					showImage[k].setImageBitmap(imageBitmap[k]);
					showImage[k].setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private class ShowTaskTask extends UrlJsonAsyncTask {
		public ShowTaskTask(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				if (json.getBoolean("success")) {
					JSONArray jsonTasks = json.getJSONObject("data")
							.getJSONArray("image");
					int length = jsonTasks.length();
					for (int i = 0; i < length; i++) {
						String image = jsonTasks.getString(i);

						imageBitMapURL[i] = image;
					}
				}

				BitmapDownloaderTask task = new BitmapDownloaderTask();
				task.execute();

				// Toast.makeText(context, json.getString("info"),
				// Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			} finally {
				super.onPostExecute(json);
			}
		}
	}

	Bitmap downloadBitmap(String url) {
		final AndroidHttpClient client = AndroidHttpClient
				.newInstance("Android");
		String totalURL = null;
		Bitmap bitmap = null;

		try {
			String[] splitURL = null;
			splitURL = url.split("/");
			String encodeURL = URLEncoder.encode(url, "UTF-8");
			String lastEncodeURL = encodeURL.split("%2F")[5];

			totalURL = splitURL[0] + "//" + splitURL[2] + "/" + splitURL[3]
					+ "/" + splitURL[4] + "/" + lastEncodeURL;
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if (CacheManager.retrieveData(getApplicationContext(), totalURL) != null) {
				bitmap = CacheManager.retrieveData(getApplicationContext(),
						totalURL);
				Log.d("CacheMemory", "Image in Cache Memory");
				return bitmap;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		final HttpGet getRequest = new HttpGet(totalURL);

		try {
			 URL imageUrl = new URL(totalURL);
			 URLConnection connection = imageUrl.openConnection();
			 connection.setUseCaches(true);
			
			 InputStream inputStream = connection.getInputStream();
			
			 BitmapFactory.Options options = new BitmapFactory.Options();
			 options.inSampleSize = 1;
			 bitmap = BitmapFactory.decodeStream(inputStream);

			CacheManager.cacheData(getApplicationContext(), bitmap, totalURL);

			return bitmap;

			// HttpResponse response = client.execute(getRequest);
			// final int statusCode = response.getStatusLine().getStatusCode();
			// if (statusCode != HttpStatus.SC_OK) {
			// Log.w("ImageDownloader", "Error " + statusCode
			// + " while retrieving bitmap from " + url);
			// return null;
			// }
			//
			// final HttpEntity entity = response.getEntity();
			// if (entity != null) {
			// InputStream inputStream = null;
			// try {
			// inputStream = entity.getContent();
			// BitmapFactory.Options options = new BitmapFactory.Options();
			// options.inSampleSize = 2;
			// final Bitmap bitmap = BitmapFactory
			// .decodeStream(inputStream);
			// return bitmap;
			// } finally {
			// if (inputStream != null) {
			// inputStream.close();
			// }
			// entity.consumeContent();
			// }
			// }
		} catch (Exception e) {
			// Could provide a more explicit error message for IOException or
			// IllegalStateException
			getRequest.abort();
			Log.w("ImageDownloader", "Error while retrieving bitmap from "
					+ url + e.toString());
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}
}
