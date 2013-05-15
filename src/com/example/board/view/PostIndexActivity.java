package com.example.board.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.board.R;
import com.example.board.lib.UrlJsonAsyncTask;
import com.example.board.model.NetworkInfo;
import com.example.board.model.Post;
import com.example.board.model.PostAdapter;

public class PostIndexActivity extends SherlockActivity {

	private static final String TASKS_URL = "http://" + NetworkInfo.IP
			+ "/api/v1/posts.json";

	private SharedPreferences mPreferences;

	private String mCategory = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_index);

		mCategory = getIntent().getStringExtra("category");

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		if (mPreferences.contains("AuthToken")) {
			loadPostFromServer(TASKS_URL, mCategory);
		} else {
			Toast.makeText(this, "로그인을 먼저 해주세요", 2000).show();
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
		getPostsTask.setMessageLoading("Loading tasks...");
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

					if (category.equals(jsonTask.getString("category")) || jsonTask.getString("category").equals("공지사항")) {

						tasksArray.add(new Post(jsonTask.getInt("id"), jsonTask
								.getString("title"), jsonTask
								.getString("category"), jsonTask
								.getString("description")));
					}
				}

				ListView tasksListView = (ListView) findViewById(R.id.postLv);
				if (tasksListView != null) {
					tasksListView.setAdapter(new PostAdapter(
							PostIndexActivity.this, tasksArray));
				}
				tasksListView.setOnItemClickListener(new TasklistListener());
			} catch (Exception e) {
				Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
						.show();
			} finally {
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
}
