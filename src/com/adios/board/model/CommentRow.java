package com.adios.board.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adios.board.R;

public class CommentRow extends LinearLayout {

	private TextView comment_contents;
	private TextView comment_author;
	private TextView comment_updated_day;
	private TextView comment_updated_time;
	private Button commentDeleteBtn;

	public CommentRow(Context context, Comment mComment) {
		super(context);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.comment_row, this, true);

		comment_contents = (TextView) findViewById(R.id.comment_contents);
		comment_contents.setText(mComment.getContents());

		comment_author = (TextView) findViewById(R.id.comment_author);
		comment_author.setText("작성자 : " + mComment.getAuthor() + " ");

		comment_updated_day = (TextView) findViewById(R.id.comment_updated_day);
		comment_updated_day.setText(mComment.getUpdated_time().split(" ")[0]
				+ " ");

		comment_updated_time = (TextView) findViewById(R.id.comment_updated_time);
		comment_updated_time.setText(mComment.getUpdated_time().split(" ")[1]
				+ " " + mComment.getUpdated_time().split(" ")[2]);

		commentDeleteBtn = (Button) findViewById(R.id.commentDeleteBtn);
		if (mComment.getIsOwner()) {
			setDeleteBtn(true);
		} else {
			setDeleteBtn(false);
		}
		commentDeleteBtn.setId(mComment.getId());

	}

	public void setTitle(String data) {
		comment_contents.setText(data);
	}

	public void setCategory(String data) {
		comment_author.setText(data);
	}

	public void setUpdated_day(String data) {
		comment_updated_day.setText(data);
	}

	public void setUpdated_time(String data) {
		comment_updated_time.setText(data);
	}

	public void setAuthor(String data) {
		comment_author.setText(data);
	}

	public void setDeleteBtn(boolean isOwner) {
		if (isOwner) {
			commentDeleteBtn.setVisibility(Button.VISIBLE);
		} else {
			commentDeleteBtn.setVisibility(Button.INVISIBLE);
		}
	}
}
