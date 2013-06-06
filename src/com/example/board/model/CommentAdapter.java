package com.example.board.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CommentAdapter extends BaseAdapter {

	private Context mContext;
	private List<Comment> mComments;

	public CommentAdapter(Context context, ArrayList<Comment> mComments) {
		this.mContext = context;
		this.mComments = mComments;
	}

	@Override
	public int getCount() {
		return mComments.size();
	}

	@Override
	public Object getItem(int position) {
		return mComments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CommentRow mCommentTextView;
		if (convertView == null) {
			mCommentTextView = new CommentRow(mContext, mComments.get(position));
		} else {
			mCommentTextView = (CommentRow) convertView;

			mCommentTextView.setTitle(mComments.get(position).getContents());
			mCommentTextView.setCategory("작성자 : "
					+ mComments.get(position).getAuthor() + " ");
			mCommentTextView.setUpdated_day(mComments.get(position).getUpdated_time()
					.split(" ")[0] + " ");
			mCommentTextView.setUpdated_time(mComments.get(position)
					.getUpdated_time().split(" ")[1]
					+ " "
					+ mComments.get(position).getUpdated_time().split(" ")[2]);
		}

		return mCommentTextView;
	}
}
