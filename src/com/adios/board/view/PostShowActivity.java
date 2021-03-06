package com.adios.board.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.adios.board.R;
import com.adios.board.controller.CacheManager;
import com.adios.board.lib.UrlJsonAsyncTask;
import com.adios.board.model.Comment;
import com.adios.board.model.CommentAdapter;
import com.adios.board.model.NetworkInfo;

public class PostShowActivity extends SherlockActivity {

	private static int mPostId = 0;
	private static String mPostTitle = "";
	private static String mPostDescription = "";
	private static String mCategory = "";
	private static String mAuthor = "";

	private static String SHOW_TASK_ENDPOINT_URL;

	private static String SHOW_COMMENTS_ENDPOINT_URL;

	private static String DESTORY_TASK_ENDPOINT_URL;

	private SharedPreferences mPreferences;

	private String[] imageBitMapURL = new String[5];

	private Bitmap[] imageBitmap = new Bitmap[5];

	private ImageView[] showImage = new ImageView[5];

	private ListView commentsShowListView;
	EditText addCommentEt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_show);

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		TextView task_show_title = (TextView) findViewById(R.id.task_show_title);
		TextView task_show_description = (TextView) findViewById(R.id.task_show_description);
		TextView task_show_author = (TextView) findViewById(R.id.task_show_author);
		commentsShowListView = (ListView) findViewById(R.id.comments_show_list);

		Intent taskIntent = getIntent();
		mPostTitle = taskIntent.getStringExtra("title");
		mPostDescription = taskIntent.getStringExtra("description");
		mPostId = taskIntent.getIntExtra("post_id", 0);
		mCategory = taskIntent.getStringExtra("category");
		mAuthor = taskIntent.getStringExtra("author");

		task_show_title.setText(mPostTitle);
		task_show_description.setText(mPostDescription);
		task_show_author.setText(mAuthor);

		setTitle(mPostTitle);

		SHOW_TASK_ENDPOINT_URL = NetworkInfo.IP + "/api/v1/posts/" + mPostId
				+ ".json";

		SHOW_COMMENTS_ENDPOINT_URL = NetworkInfo.IP + "/api/v1/comments/"
				+ mPostId + ".json";

		DESTORY_TASK_ENDPOINT_URL = NetworkInfo.IP + "/api/v1/posts/" + mPostId
				+ ".json";

		showTaskTasks showTask = new showTaskTasks(PostShowActivity.this);
		showTask.setMessageLoading("글 불러오는 중...");
		showTask.setAuthToken(mPreferences.getString("AuthToken", ""));
		showTask.execute(SHOW_TASK_ENDPOINT_URL);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		showCommentsTasks showCommentsTasks = new showCommentsTasks(
				PostShowActivity.this);
		showCommentsTasks.setAuthToken(mPreferences.getString("AuthToken", ""));
		showCommentsTasks.execute(SHOW_COMMENTS_ENDPOINT_URL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.show, menu);

		if (mPreferences.getString("userName", "").equals(mAuthor)) {
			menu.findItem(R.id.action_create_delete).setVisible(true);
		} else {
			menu.findItem(R.id.action_create_delete).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create_delete:
			final Dialog dialog = new Dialog(PostShowActivity.this);
			dialog.setContentView(R.layout.task_remove_input);
			dialog.setTitle("삭제하시겠습니까?");

			// 취소 버튼
			((Button) dialog.findViewById(R.id.todayCancelBtn))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
			// 저장 버튼
			((Button) dialog.findViewById(R.id.todayInputBtn))
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Toast.makeText(getApplicationContext(), "삭제했습니다.",
									Toast.LENGTH_SHORT).show();

							DestroyTaskTasks destroyTaskTasks = new DestroyTaskTasks(
									PostShowActivity.this);
							destroyTaskTasks.setMessageLoading("글 삭제중...");
							// destroyTaskTasks.setAuthToken(mPreferences
							// .getString("AuthToken", ""));
							destroyTaskTasks.execute(DESTORY_TASK_ENDPOINT_URL);

							Intent deleteTaskIntent = new Intent(
									getApplicationContext(),
									PostIndexActivity.class);
							deleteTaskIntent.putExtra("category", mCategory);
							deleteTaskIntent
									.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							deleteTaskIntent
									.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
							startActivity(deleteTaskIntent);

							dialog.dismiss();
						}
					});
			dialog.show();
			break;
		}

		return true;
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

	private class DestroyTaskTasks extends UrlJsonAsyncTask {
		public DestroyTaskTasks(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... urls) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpDelete delete = new HttpDelete(urls[0]);
			String response = null;
			JSONObject json = new JSONObject();

			try {
				try {
					// setup the returned values in case
					// something goes wrong
					json.put("success", false);
					json.put("info", "인터넷 연결을 다시 확인해 주세요.");

					// add the users's info to the post params

					// "authenticity_token", mPreferences.getString("AuthToken",
					// "")

					// setup the request headers
					delete.setHeader("Authorization", "Token token=\""
							+ mPreferences.getString("AuthToken", "") + "\"");
					delete.setHeader("Accept", "application/json");
					delete.setHeader("Content-Type", "application/json");

					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					response = client.execute(delete, responseHandler);
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
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			} finally {
				super.onPostExecute(json);
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

	public void addComment(View v) {
		switch (v.getId()) {
		case R.id.showCommentEditTextBtn:
			Button showCommentEditTextBtn = (Button) findViewById(R.id.showCommentEditTextBtn);
			addCommentEt = (EditText) findViewById(R.id.addCommentEt);
			Button addCommentSubmitBtn = (Button) findViewById(R.id.addCommentSubmitBtn);

			addCommentEt.setVisibility(EditText.VISIBLE);
			addCommentSubmitBtn.setVisibility(Button.VISIBLE);
			showCommentEditTextBtn.setVisibility(Button.INVISIBLE);

			break;
		case R.id.addCommentSubmitBtn:
			createCommentTasks createCommentsTasks = new createCommentTasks(
					PostShowActivity.this);
			createCommentsTasks.setMessageLoading("댓글 추가 중...");
			createCommentsTasks.setAuthToken(mPreferences.getString(
					"AuthToken", ""));
			createCommentsTasks
					.execute(NetworkInfo.CREATE_COMMENT_ENDPOINT_URL);

			break;
		}
	}

	public void deleteComment(View v) {
		final Dialog dialog = new Dialog(PostShowActivity.this);
		dialog.setContentView(R.layout.task_remove_input);
		dialog.setTitle("삭제하시겠습니까?");

		final int mCommentId = v.getId();

		// 취소 버튼
		((Button) dialog.findViewById(R.id.todayCancelBtn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
		// 저장 버튼
		((Button) dialog.findViewById(R.id.todayInputBtn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(getApplicationContext(), "삭제했습니다.",
								Toast.LENGTH_SHORT).show();

						String DESTORY_COMMENT_ENDPOINT_URL = null;
						DESTORY_COMMENT_ENDPOINT_URL = NetworkInfo.IP
								+ "/api/v1/comments/" + mCommentId + ".json";

						DestroyCommentTasks destroyCommentTasks = new DestroyCommentTasks(
								PostShowActivity.this);
						destroyCommentTasks.setMessageLoading("댓글 삭제중...");
						destroyCommentTasks.setAuthToken(mPreferences
								.getString("AuthToken", ""));
						destroyCommentTasks
								.execute(DESTORY_COMMENT_ENDPOINT_URL);

						Intent intent = new Intent(PostShowActivity.this,
								PostShowActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
						intent.putExtra("title", mPostTitle);
						intent.putExtra("description", mPostDescription);
						intent.putExtra("post_id", mPostId);
						intent.putExtra("author", mAuthor);
						startActivity(intent);

						dialog.dismiss();
					}
				});
		dialog.show();
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

				if (length > 5) {
					final ScrollView post_show_scrollView = (ScrollView) findViewById(R.id.post_show_scrollView);
					commentsShowListView
							.setOnTouchListener(new OnTouchListener() {

								public boolean onTouch(View v, MotionEvent event) {

									post_show_scrollView
											.requestDisallowInterceptTouchEvent(true);

									return false;

								}

							});
				}
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

					if (mPreferences.getString("userName", "").equals(
							jsonTask.getString("author"))) {
						commentsArray.add(new Comment(jsonTask.getInt("id"),
								jsonTask.getString("author"), jsonTask
										.getString("contents"), updated_time,
								true));
					} else {
						commentsArray.add(new Comment(jsonTask.getInt("id"),
								jsonTask.getString("author"), jsonTask
										.getString("contents"), updated_time,
								false));
					}
				}

				if (commentsShowListView != null) {
					commentsShowListView.setAdapter(new CommentAdapter(
							PostShowActivity.this, commentsArray));
				}

			} catch (Exception e) {
				Toast.makeText(context, "댓글이 없습니다.", Toast.LENGTH_LONG).show();
			} finally {
				super.onPostExecute(json);
			}
		}
	}

	private class DestroyCommentTasks extends UrlJsonAsyncTask {
		public DestroyCommentTasks(Context context) {
			super(context);
		}

		@Override
		protected JSONObject doInBackground(String... urls) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpDelete delete = new HttpDelete(urls[0]);
			String response = null;
			JSONObject json = new JSONObject();

			try {
				try {
					// setup the returned values in case
					// something goes wrong
					json.put("success", false);
					json.put("info", "인터넷 연결을 다시 확인해 주세요.");

					// setup the request headers
					delete.setHeader("Authorization", "Token token=\""
							+ mPreferences.getString("AuthToken", "") + "\"");
					delete.setHeader("Accept", "application/json");
					delete.setHeader("Content-Type", "application/json");

					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					response = client.execute(delete, responseHandler);
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
					showCommentsTasks showCommentsTasks = new showCommentsTasks(
							PostShowActivity.this);
					showCommentsTasks.setMessageLoading("댓글 불러오는중...");
					showCommentsTasks.setAuthToken(mPreferences.getString(
							"AuthToken", ""));
					showCommentsTasks.execute(SHOW_COMMENTS_ENDPOINT_URL);

					Intent intent = new Intent(PostShowActivity.this,
							PostShowActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					intent.putExtra("title", mPostTitle);
					intent.putExtra("description", mPostDescription);
					intent.putExtra("post_id", mPostId);
					intent.putExtra("author", mAuthor);
					startActivity(intent);
				}
				Toast.makeText(context, json.getString("info"),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			} finally {
				super.onPostExecute(json);
				finish();
			}
		}
	}

	private class createCommentTasks extends UrlJsonAsyncTask {
		public createCommentTasks(Context context) {
			super(context);
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
					json.put("info", "인터넷 연결을 확인해 주세요.");
					taskObj.put("author",
							mPreferences.getString("userName", ""));
					taskObj.put("contents", addCommentEt.getText().toString());
					taskObj.put("post_id", mPostId);
					taskObj.put("user_id", mPreferences.getInt("userId", 0));
					holder.put("comment", taskObj);
					StringEntity se = new StringEntity(holder.toString(),
							"utf-8");
					post.setEntity(se);
					post.setHeader("Accept", "application/json");
					post.setHeader("Content-Type", "application/json");
					post.setHeader("Authorization", "Token token="
							+ mPreferences.getString("AuthToken", ""));

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
					showCommentsTasks showCommentsTasks = new showCommentsTasks(
							PostShowActivity.this);
					showCommentsTasks.setMessageLoading("댓글 불러오는중...");
					showCommentsTasks.setAuthToken(mPreferences.getString(
							"AuthToken", ""));
					showCommentsTasks.execute(SHOW_COMMENTS_ENDPOINT_URL);

					Intent intent = new Intent(PostShowActivity.this,
							PostShowActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					intent.putExtra("title", mPostTitle);
					intent.putExtra("description", mPostDescription);
					intent.putExtra("post_id", mPostId);
					intent.putExtra("author", mAuthor);
					startActivity(intent);
				}
				Toast.makeText(context, json.getString("info"),
						Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			} finally {
				super.onPostExecute(json);
				finish();
			}
		}
	}
}
