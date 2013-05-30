package com.example.board.view;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.board.R;
import com.example.board.lib.UrlJsonAsyncTask;
import com.example.board.model.NetworkInfo;

public class PostCreateActivity extends SherlockActivity {

	ImageView[] targetImage = new ImageView[5];
	String[] imageData = new String[5];

	private SharedPreferences mPreferences;
	private String mPostTitle;
	private String mPostDescription;
	private String mPostCategory;

	boolean changeImageFlag = false;

	private Spinner category;

	int mImageWidth = 0;
	int mImageHeight = 0;
	int newImageWidth = 400;
	int newImageHeight = 400;
	int targetImage_num;
	int imageNum = 0;
	float scaleWidth = 0;
	float scaleHeight = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_create);

		addItemsOnSpinner();

		Button addImageBtn = (Button) findViewById(R.id.addImageBtn);

		addImageBtn.setOnClickListener(new addImageBtnListener());

		mPreferences = getSharedPreferences("AuthToken", MODE_PRIVATE);

		Toast.makeText(this, "사진은 5장까지 추가가 됩니다.", Toast.LENGTH_LONG).show();
	}

	private class addImageBtnListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// imageNum = 이미지 개수
			switch (imageNum) {
			case 0:
				targetImage[0] = (ImageView) findViewById(R.id.targetImage1);
				getImageFromGallery(0);
				imageNum++;
				break;
			case 1:
				targetImage[1] = (ImageView) findViewById(R.id.targetImage2);
				getImageFromGallery(1);
				imageNum++;
				break;
			case 2:
				targetImage[2] = (ImageView) findViewById(R.id.targetImage3);
				getImageFromGallery(2);
				imageNum++;
				break;
			case 3:
				targetImage[3] = (ImageView) findViewById(R.id.targetImage4);
				getImageFromGallery(3);
				imageNum++;
				break;
			case 4:
				targetImage[4] = (ImageView) findViewById(R.id.targetImage5);
				getImageFromGallery(4);
				imageNum++;
				break;
			case 5:
				Toast.makeText(v.getContext(), "사진은 5개까지 추가가 가능합니다.",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			Uri targetUri = data.getData();
			Bitmap bitmap;
			try {
				bitmap = BitmapFactory.decodeStream(getContentResolver()
						.openInputStream(targetUri));

				targetImage[targetImage_num].setImageResource(0);

				mImageWidth = bitmap.getWidth();
				mImageHeight = bitmap.getHeight();

				scaleWidth = ((float) newImageWidth) / mImageWidth;
				scaleHeight = ((float) newImageHeight) / mImageHeight;

				Matrix matrix = new Matrix();
				matrix.postScale(scaleWidth, scaleHeight);

				Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
						mImageWidth, mImageHeight, matrix, true);

				targetImage[targetImage_num].setImageBitmap(resizedBitmap);

				targetImage[targetImage_num].setVisibility(ImageView.VISIBLE);

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (changeImageFlag == false) {
				imageNum--;
			}
			changeImageFlag = false;
		}
	}

	public boolean imageListener(View image) {
		switch (image.getId()) {
		case R.id.targetImage1:
			targetImage[0] = (ImageView) findViewById(R.id.targetImage1);
			getImageFromGallery(0);
			changeImageFlag = true;
			break;
		case R.id.targetImage2:
			targetImage[1] = (ImageView) findViewById(R.id.targetImage2);
			getImageFromGallery(1);
			changeImageFlag = true;
			break;
		case R.id.targetImage3:
			targetImage[2] = (ImageView) findViewById(R.id.targetImage3);
			getImageFromGallery(2);
			changeImageFlag = true;
			break;
		case R.id.targetImage4:
			targetImage[3] = (ImageView) findViewById(R.id.targetImage4);
			getImageFromGallery(3);
			changeImageFlag = true;
			break;
		case R.id.targetImage5:
			targetImage[4] = (ImageView) findViewById(R.id.targetImage5);
			getImageFromGallery(4);
			changeImageFlag = true;
			break;
		}

		return true;
	}

	public void getImageFromGallery(int image_num) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		targetImage_num = image_num;
		intent.setType("image/*");
		startActivityForResult(intent, 0);
	}

	public void saveTask(View button) {
		EditText postTitlelField = (EditText) findViewById(R.id.postTitle);
		mPostTitle = postTitlelField.getText().toString();
		EditText postDescriptionField = (EditText) findViewById(R.id.postDescription);
		mPostDescription = postDescriptionField.getText().toString();
		// http://www.mkyong.com/android/android-spinner-drop-down-list-example/
		// ���ǳ� (select item)
		Spinner postCategoryField = (Spinner) findViewById(R.id.postCategory);
		mPostCategory = String.valueOf(postCategoryField.getSelectedItem());

		if (mPostTitle.length() == 0) {
			// input fields are empty
			Toast.makeText(this, "제목을 써주세요", Toast.LENGTH_LONG).show();
			return;
		} else {
			// everything is ok!
			CreateTaskTask createTask = new CreateTaskTask(
					PostCreateActivity.this);
			createTask.setMessageLoading("새 글을 등록 중 입니다(사진이 많으면 오래 걸릴 수도 있습니다)...");
			createTask.setAuthToken(mPreferences.getString("AuthToken", ""));
			createTask.execute(NetworkInfo.CREATE_TASK_ENDPOINT_URL);
		}
	}

	private class CreateTaskTask extends UrlJsonAsyncTask {
		public CreateTaskTask(Context context) {
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

			targetImage[0] = (ImageView) findViewById(R.id.targetImage1);
			targetImage[1] = (ImageView) findViewById(R.id.targetImage2);
			targetImage[2] = (ImageView) findViewById(R.id.targetImage3);
			targetImage[3] = (ImageView) findViewById(R.id.targetImage4);
			targetImage[4] = (ImageView) findViewById(R.id.targetImage5);

			for (int i = 0; i < imageNum; i++) {
				ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
				BitmapDrawable drawable = (BitmapDrawable) targetImage[i]
						.getDrawable();
				Bitmap imageBitmap = drawable.getBitmap();
				imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
						imageStream);
				byte[] data = imageStream.toByteArray();
				imageData[i] = new String(Base64.encode(data, 1));
				try {
					imageStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				try {
					json.put("success", false);
					json.put("info", "Something went wrong. Retry!");
					taskObj.put("title", mPostTitle);
					taskObj.put("category", mPostCategory);
					taskObj.put("description", mPostDescription);
					taskObj.put("image1", imageData[0]);
					taskObj.put("image2", imageData[1]);
					taskObj.put("image3", imageData[2]);
					taskObj.put("image4", imageData[3]);
					taskObj.put("image5", imageData[4]);
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
				if (json.getBoolean("success")) {
					Intent intent = new Intent(getApplicationContext(),
							HomeActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

	public void addItemsOnSpinner() {
		category = (Spinner) findViewById(R.id.postCategory);
		List<String> list = new ArrayList<String>();
		list.add(getIntent().getStringExtra("category"));

		ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		categoryAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		category.setAdapter(categoryAdapter);
	}
}