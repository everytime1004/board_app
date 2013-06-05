package com.example.board.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.example.board.R;
import com.example.board.lib.UrlJsonAsyncTask;
import com.example.board.model.NetworkInfo;
import com.example.board.model.Post;
import com.example.board.model.PostAdapter;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PostIndexActivity extends SherlockActivity {

	private SharedPreferences mPreferences;

	private String mCategory = null;

	private PullToRefreshListView mPullRefreshListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_index);

		mCategory = getIntent().getStringExtra("category");

		mPullRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);

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

						// Do work to refresh the list here.
						loadPostFromServer(NetworkInfo.TASKS_URL, mCategory);
					}
				});

		setTitle(mCategory);

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		if (mPreferences.contains("AuthToken")) {
			loadPostFromServer(NetworkInfo.TASKS_URL, mCategory);
		} else {
			Toast.makeText(this, "로그인을 먼저 해주세요", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void loadPostFromServer(String url, String category) {
		GetPostsTask getPostsTask = new GetPostsTask(PostIndexActivity.this,
				category);
		getPostsTask.setMessageLoading("글들을 불러오는 중입니다...");
		getPostsTask.setAuthToken(mPreferences.getString("AuthToken", ""));
		getPostsTask.execute(url);
	}

	// Post 받아 오는 AsyncTask
	private class GetPostsTask extends UrlJsonAsyncTask {
		private String category = null;

		public GetPostsTask(Context context, String category) {
			super(context);
			this.category = category;
		}

		@Override
		protected void onPostExecute(JSONObject json) {
			try {
				JSONArray jsonTasks = json.getJSONObject("data").getJSONArray(
						"posts");
				JSONObject jsonTask = new JSONObject();
				int length = jsonTasks.length();
				final ArrayList<Post> tasksArray = new ArrayList<Post>(length);

				for (int i = 0; i < length; i++) {
					jsonTask = jsonTasks.getJSONObject(i);

					if (category.equals(jsonTask.getString("category"))
							|| jsonTask.getString("category").equals("공지사항")) {
						
						String updated_time = jsonTask.getString("updated_at");
						// 2013-06-03T06:39:00Z

						String[] updated_time_split = updated_time.split("T");

						updated_time = updated_time_split[0] + " "
								+ updated_time_split[1].split(":")[0] + "시 "
								+ updated_time_split[1].split(":")[1] + "분";

						tasksArray.add(new Post(jsonTask.getInt("id"), jsonTask
								.getString("title"), jsonTask
								.getString("category"), jsonTask
								.getString("description"), updated_time));
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
			startActivity(intent);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.show, menu);

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
