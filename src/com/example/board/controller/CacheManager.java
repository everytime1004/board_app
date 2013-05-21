package com.example.board.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

public class CacheManager {

	private static final long MAX_SIZE = 5242880L; // 5MB

	private CacheManager() {

	}

	public static void cacheData(Context context, Bitmap bitmap, String name)
			throws IOException {

		File cacheDir = context.getCacheDir();
		long size = getDirSize(cacheDir);
		long newSize = bitmap.getByteCount() + size;

		if (newSize > MAX_SIZE) {
			cleanDir(cacheDir, newSize - MAX_SIZE);
		}
		
		String[] imageCheckSplit = new String[5];
		imageCheckSplit = name.split("_");
		
		String imageCheck = imageCheckSplit[2]+imageCheckSplit[3]+imageCheckSplit[4];
		
		File cacheImage = new File(cacheDir.getAbsolutePath(), imageCheck);
		OutputStream os = new FileOutputStream(cacheImage.getAbsolutePath());
		try {
			bitmap.compress(CompressFormat.JPEG, 100, os);
		} finally {
			try {
				os.flush();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static Bitmap retrieveData(Context context, String name)
			throws IOException {
		
		String[] imageCheckSplit = new String[5];
		imageCheckSplit = name.split("_");
		
		String imageCheck = imageCheckSplit[2]+imageCheckSplit[3]+imageCheckSplit[4];
		
		File cacheDir = context.getCacheDir();
		File file = new File(cacheDir, imageCheck);
	
		if (!file.exists()) {
			// Data doesn't exist
			Log.d("retrieveData","before return null");
			return null;
		}
	
		byte[] data = new byte[(int) file.length()];
		FileInputStream is = new FileInputStream(file);
		try {
			is.read(data);
		} finally {
			is.close();
		}
	
		Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		
		Log.d("retrieveData","before return bitmap");
		return bitmap;
	}

	private static void cleanDir(File dir, long bytes) {

		long bytesDeleted = 0;
		File[] files = dir.listFiles();

		for (File file : files) {
			bytesDeleted += file.length();
			file.delete();

			if (bytesDeleted >= bytes) {
				break;
			}
		}
	}

	private static long getDirSize(File dir) {

		long size = 0;
		File[] files = dir.listFiles();

		for (File file : files) {
			if (file.isFile()) {
				size += file.length();
			}
		}

		return size;
	}
}