package com.example.board.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.board.R;

public class PostRow extends LinearLayout {

	private TextView task_title;
	private TextView task_category;

	public PostRow(Context context, Post mPost) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.post_row, this, true);

		task_title = (TextView) findViewById(R.id.task_title);
		task_title.setText(mPost.getTitle());

		task_category = (TextView) findViewById(R.id.task_category);
		task_category.setText(mPost.getCategory());

	}

	public void setTitle(String data) {
		task_title.setText(data);
	}

	public void setCategory(String data) {
		task_category.setText(data);
	}
}
