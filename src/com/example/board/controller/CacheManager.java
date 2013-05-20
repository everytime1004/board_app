package com.example.board.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

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

		File file = new File(cacheDir, name);
		FileOutputStream os = new FileOutputStream(file);
		try {
			bitmap.compress(CompressFormat.JPEG, 100, os);
		} finally {
			os.flush();
			os.close();
		}
	}

	public static Bitmap retrieveData(Context context, String name)
			throws IOException {

		File cacheDir = context.getCacheDir();
		File file = new File(cacheDir, name);

		if (!file.exists()) {
			// Data doesn't exist
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