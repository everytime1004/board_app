package com.example.board.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.board.R;
import com.example.board.controller.CacheManager;
import com.example.board.lib.UrlJsonAsyncTask;
import com.example.board.model.Comment;
import com.example.board.model.CommentAdapter;
import com.example.board.model.NetworkInfo;

public class PostShowActivity extends SherlockActivity {

	private static int mPostId = 0;

	private static String SHOW_TASK_ENDPOINT_URL;

	private static String SHOW_COMMENTS_ENDPOINT_URL;

	private SharedPreferences mPreferences;

	private String[] imageBitMapURL = new String[5];

	private Bitmap[] imageBitmap = new Bitmap[5];

	private ImageView[] showImage = new ImageView[5];

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

		SHOW_COMMENTS_ENDPOINT_URL = NetworkInfo.IP + "/api/v1/comments/"
				+ mPostId + ".json";

		showTaskTasks showTask = new showTaskTasks(PostShowActivity.this);
		showTask.setMessageLoading("글 불러오는 중...");
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

	private class showTaskTasks extends UrlJsonAsyncTask {
		public showTaskTasks(Context context) {
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

						if (image.contains("fallback/default.png")) {
							throw new Exception("사진이 없습니다");
						}
						/*
						 * 만약 글에 사진이 없으면 default 이미지가 넘어오는데 default를 지정해주지 않았으므로
						 * url이 없고 bitmap 다운로드할 때 에러가 발생해서 거기서 httl close 등 못하고
						 * 죽어버리므로 leak 발생해서 앱이 죽어버림
						 */

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
			String lastEncodeURL = encodeURL.split("%2F")[8];

			totalURL = splitURL[0] + "//" + splitURL[2] + "/" + splitURL[3]
					+ "/" + splitURL[4] + "/" + splitURL[5] + "/" + splitURL[6]
					+ "/" + splitURL[7] + "/" + "thumb_" + lastEncodeURL;
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

	/********************************************** 댓글 *********************************************/
	public void showComments(View v) {
		switch (v.getId()) {
		case R.id.showComments:
			TextView showComments = (TextView) findViewById(R.id.showComments);
			showComments.setVisibility(TextView.INVISIBLE);

			showCommentsTasks commentsTasks = new showCommentsTasks(
					PostShowActivity.this);
			commentsTasks.setMessageLoading("댓글 불러오는중...");
			commentsTasks.setAuthToken(mPreferences.getString("AuthToken", ""));
			commentsTasks.execute(SHOW_COMMENTS_ENDPOINT_URL);
			break;

		}
	}

	private class showCommentsTasks extends UrlJsonAsyncTask {
		public showCommentsTasks(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				JSONArray jsonTasks = json.getJSONObject("data").getJSONArray(
						"comments");
				JSONObject jsonTask = new JSONObject();
				int length = jsonTasks.length();
				final ArrayList<Comment> commentsArray = new ArrayList<Comment>(
						length);

				for (int i = 0; i < length; i++) {
					jsonTask = jsonTasks.getJSONObject(i);

					String updated_time = jsonTask.getString("updated_at");
					// 2013-06-03T06:39:00Z

					String[] updated_time_split = updated_time.split("T");

					updated_time = updated_time_split[0] + " "
							+ updated_time_split[1].split(":")[0] + "시 "
							+ updated_time_split[1].split(":")[1] + "분";

					commentsArray.add(new Comment(jsonTask.getString("author"),
							jsonTask.getString("contents"), updated_time));
				}

				ListView commentsShowListView = (ListView) findViewById(R.id.comments_show_list);

				if (commentsShowListView != null) {
					commentsShowListView.setAdapter(new CommentAdapter(
							PostShowActivity.this, commentsArray));
				}

			} catch (Exception e) {
				Toast.makeText(context, "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show();
			} finally {
				
				super.onPostExecute(json);
			}
		}
	}
}
