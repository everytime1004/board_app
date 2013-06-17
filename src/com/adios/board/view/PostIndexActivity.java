package com.adios.board.view;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.adios.board.R;
import com.adios.board.lib.UrlJsonAsyncTask;
import com.adios.board.model.NetworkInfo;
import com.adios.board.model.Post;
import com.adios.board.model.PostAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PostIndexActivity extends SherlockActivity {

	private SharedPreferences mPreferences;

	private String mCategory = null;

	private PullToRefreshListView mPullRefreshListView;

	private ArrayList<Post> tasksArray = new ArrayList<Post>();

	private PostAdapter postAdapter;

	private Button moreTaskBtn = null;

	private int offset_id = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_index);

		mCategory = getIntent().getStringExtra("category");

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.post_index_list);
		moreTaskBtn = (Button) findViewById(R.id.moreTaskBtn);

		mPullRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						String label = DateUtils.formatDateTime(
								getApplicationContext(),
								System.currentTimeMillis(),
								DateUtils.FORMAT_SHOW_TIME
										| DateUtils.FORMAT_SHOW_DATE
										| DateUtils.FORMAT_ABBREV_ALL);

						// Update the LastUpdatedLabel
						refreshView.getLoadingLayoutProxy()
								.setLastUpdatedLabel(label);

						tasksArray.clear();
						postAdapter.notifyDataSetChanged();

						// Do work to refresh the list here.
						loadPostFromServer(NetworkInfo.TASKS_URL);
					}
				});

		mPullRefreshListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				moreTaskBtn.setVisibility(Button.INVISIBLE);
			}
		});

		// Add an end-of-list listener
		mPullRefreshListView
				.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

					@Override
					public void onLastItemVisible() {
						Button moreTaskBtn = (Button) findViewById(R.id.moreTaskBtn);
						moreTaskBtn.setVisibility(Button.VISIBLE);
					}
				});

		setTitle(getIntent().getStringExtra("setCategory"));

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		if (mPreferences.contains("AuthToken")) {
			loadPostFromServer(NetworkInfo.TASKS_URL);
		} else {
			Toast.makeText(this, "로그인을 먼저 해주세요", Toast.LENGTH_LONG).show();
			finish();
		}

		if (this.getIntent().getBooleanExtra("isPushIntent", false)) {
			Intent intent = new Intent(this, PostShowActivity.class);
			intent.putExtra("title", this.getIntent().getStringExtra("title"));
			intent.putExtra("description",
					this.getIntent().getStringExtra("description"));
			intent.putExtra("category",
					this.getIntent().getStringExtra("category"));
			intent.putExtra("post_id",
					this.getIntent().getIntExtra("post_id", 0));
			startActivity(intent);
		}
	}

	public void moreTaskListener(View v) {
		GetMorePostsTask getMorePostsTask = new GetMorePostsTask(
				PostIndexActivity.this);
		getMorePostsTask.setMessageLoading("글들을 불러오는 중입니다...");
		getMorePostsTask.setAuthToken(mPreferences.getString("AuthToken", ""));
		getMorePostsTask.execute(NetworkInfo.TASKS_URL + "?category="
				+ mCategory + "&" + "offset_id=" + offset_id);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		if (postAdapter != null) {
			tasksArray.clear();
			postAdapter.notifyDataSetChanged();

			loadPostFromServer(NetworkInfo.TASKS_URL);
		}
		super.onResume();
	}

	private void loadPostFromServer(String url) {
		GetPostsTask getPostsTask = new GetPostsTask(PostIndexActivity.this);
		getPostsTask.setMessageLoading("글들을 불러오는 중입니다...");
		getPostsTask.setAuthToken(mPreferences.getString("AuthToken", ""));
		getPostsTask.execute(url + "?category=" + mCategory);
	}

	// Post 받아 오는 AsyncTask
	private class GetPostsTask extends UrlJsonAsyncTask {

		public GetPostsTask(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				JSONArray jsonTasks = json.getJSONObject("data").getJSONArray(
						"posts");
				JSONObject jsonTask = new JSONObject();
				int length = jsonTasks.length();

				for (int i = 0; i < length; i++) {
					jsonTask = jsonTasks.getJSONObject(i);

					String author = jsonTask.getString("author");

					String updated_time = jsonTask.getString("updated_at");
					// 2013-06-03T06:39:00Z

					String[] updated_time_split = updated_time.split("T");

					updated_time = updated_time_split[0] + " "
							+ updated_time_split[1].split(":")[0] + "시 "
							+ updated_time_split[1].split(":")[1] + "분";

					tasksArray.add(new Post(jsonTask.getInt("id"), jsonTask
							.getString("title"),
							jsonTask.getString("category"), jsonTask
									.getString("description"), updated_time,
							author));

					if (i == length - 1) {
						offset_id = jsonTask.getInt("id") - 1;
					}

				}

				PullToRefreshListView tasksListView = mPullRefreshListView;
				if (tasksListView != null) {
					postAdapter = new PostAdapter(PostIndexActivity.this,
							tasksArray);
					tasksListView.setAdapter(postAdapter);
				}
				tasksListView.setOnItemClickListener(new TasklistListener());
			} catch (Exception e) {
				Toast.makeText(context, "게시물이 더 없습니다.", Toast.LENGTH_LONG)
						.show();
			} finally {
				mPullRefreshListView.onRefreshComplete();

				super.onPostExecute(json);
			}
		}
	}

	// Post 더 받아 오는 AsyncTask
	private class GetMorePostsTask extends UrlJsonAsyncTask {

		public GetMorePostsTask(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				JSONArray jsonTasks = json.getJSONObject("data").getJSONArray(
						"posts");
				JSONObject jsonTask = new JSONObject();
				int length = jsonTasks.length();

				for (int i = 0; i < length; i++) {
					jsonTask = jsonTasks.getJSONObject(i);

					String author = jsonTask.getString("author");

					String updated_time = jsonTask.getString("updated_at");
					// 2013-06-03T06:39:00Z

					String[] updated_time_split = updated_time.split("T");

					updated_time = updated_time_split[0] + " "
							+ updated_time_split[1].split(":")[0] + "시 "
							+ updated_time_split[1].split(":")[1] + "분";

					tasksArray.add(new Post(jsonTask.getInt("id"), jsonTask
							.getString("title"),
							jsonTask.getString("category"), jsonTask
									.getString("description"), updated_time,
							author));

					if (i == length - 1) {
						offset_id = jsonTask.getInt("id") - 1;
					}

				}

				PullToRefreshListView tasksListView = mPullRefreshListView;
				if (tasksListView != null) {
					tasksListView.setAdapter(new PostAdapter(
							PostIndexActivity.this, tasksArray));
				}
				tasksListView.setOnItemClickListener(new TasklistListener());
			} catch (Exception e) {
				Toast.makeText(context, "게시물이 없습니다.", Toast.LENGTH_LONG).show();
			} finally {
				mPullRefreshListView.onRefreshComplete();

				super.onPostExecute(json);
			}
		}
	}

	private class TasklistListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parentView, View v,
				int position, long id) {
			Post post = (Post) parentView.getItemAtPosition(position);

			Intent intent = new Intent(parentView.getContext(),
					PostShowActivity.class);
			intent.putExtra("title", post.getTitle());
			intent.putExtra("description", post.getDescription());
			intent.putExtra("post_id", post.getId());
			intent.putExtra("category", post.getCategory());
			intent.putExtra("author", post.getAuthor());
			startActivity(intent);

		}
	}

	private class GetPostsTaskBySearching extends UrlJsonAsyncTask {
		String titleOrAuthor;

		public GetPostsTaskBySearching(Context context, String titleOrAuthor) {
			super(context);
			this.titleOrAuthor = titleOrAuthor;
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
					taskObj.put("category", mCategory);
					taskObj.put("searching", titleOrAuthor);
					holder.put("post", taskObj);
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
				tasksArray.clear();
				postAdapter.notifyDataSetChanged();

				JSONArray jsonTasks = json.getJSONObject("data").getJSONArray(
						"posts");
				JSONObject jsonTask = new JSONObject();
				int length = jsonTasks.length();

				for (int i = 0; i < length; i++) {
					jsonTask = jsonTasks.getJSONObject(i);

					String author = jsonTask.getString("author");

					String updated_time = jsonTask.getString("updated_at");
					// 2013-06-03T06:39:00Z

					String[] updated_time_split = updated_time.split("T");

					updated_time = updated_time_split[0] + " "
							+ updated_time_split[1].split(":")[0] + "시 "
							+ updated_time_split[1].split(":")[1] + "분";

					tasksArray.add(new Post(jsonTask.getInt("id"), jsonTask
							.getString("title"),
							jsonTask.getString("category"), jsonTask
									.getString("description"), updated_time,
							author));

				}

				PullToRefreshListView tasksListView = mPullRefreshListView;

				if (tasksListView != null) {
					postAdapter = new PostAdapter(PostIndexActivity.this,
							tasksArray);
					tasksListView.setAdapter(postAdapter);
				}
				tasksListView.setOnItemClickListener(new TasklistListener());
			} catch (Exception e) {
				Toast.makeText(context, "게시물이 더 없습니다.", Toast.LENGTH_LONG)
						.show();
			} finally {
				mPullRefreshListView.onRefreshComplete();

				super.onPostExecute(json);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.show_index, menu);

		SearchView searchView = (SearchView) menu.findItem(
				R.id.action_search_task).getActionView();

		SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
			public boolean onQueryTextChange(String newText) {
				return true;
			}

			public boolean onQueryTextSubmit(String query) {
				// this is your adapter that will be filtered
				GetPostsTaskBySearching getPostsSearchingTask = new GetPostsTaskBySearching(
						PostIndexActivity.this, query);
				getPostsSearchingTask.setMessageLoading("글들을 불러오는 중입니다...");
				getPostsSearchingTask.setAuthToken(mPreferences.getString(
						"AuthToken", ""));
				getPostsSearchingTask.execute(NetworkInfo.TASKS_SEARCH_URL);
				return true;
			}
		};
		searchView.setOnQueryTextListener(queryTextListener);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_create_task:
			Intent createTaskIntent = new Intent(this, PostCreateActivity.class);
			createTaskIntent.putExtra("category", mCategory);
			startActivity(createTaskIntent);
			break;
		}

		return true;
	}
}
