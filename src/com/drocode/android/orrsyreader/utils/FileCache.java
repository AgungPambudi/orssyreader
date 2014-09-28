package com.drocode.android.orrsyreader.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

class FileCache {
	private File cacheDir;

	FileCache(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(Environment.getExternalStorageDirectory(),
					"LazyList");
		} else {
			cacheDir = context.getCacheDir();
		}
		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	File getFile(String url) {
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);

		return f;
	}

	void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;

		for (File f : files)
			f.delete();
	}
}