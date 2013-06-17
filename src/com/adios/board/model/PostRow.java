package com.adios.board.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adios.board.R;

public class PostRow extends LinearLayout {

	private TextView task_title;
	private TextView task_category;
	private TextView task_updated_day;
	private TextView task_updated_time;
	private TextView task_author;

	public PostRow(Context context, Post mPost) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.post_row, this, true);

		task_title = (TextView) findViewById(R.id.task_title);

		if (mPost.getCategory().equals("sellComplete")) {
			task_title.setText(mPost.getTitle() + "(판매완료)");
		} else {
			task_title.setText(mPost.getTitle());
		}

		task_updated_day = (TextView) findViewById(R.id.task_updated_day);
		task_updated_day.setText(mPost.getUpdated_time().split(" ")[0]);

		task_updated_time = (TextView) findViewById(R.id.task_updated_time);
		task_updated_time.setText(mPost.getUpdated_time().split(" ")[1] + " "
				+ mPost.getUpdated_time().split(" ")[2]);

		task_author = (TextView) findViewById(R.id.task_author);
		task_author.setText("작성자 : " + mPost.getAuthor());

	}

	public void setTitle(String data) {
		task_title.setText(data);
	}

	public void setCategory(String data) {
		task_category.setText(data);
	}

	public void setUpdated_day(String data) {
		task_updated_day.setText(data);
	}

	public void setUpdated_time(String data) {
		task_updated_time.setText(data);
	}

	public void setAuthor(String data) {
		task_author.setText(data);
	}
}
