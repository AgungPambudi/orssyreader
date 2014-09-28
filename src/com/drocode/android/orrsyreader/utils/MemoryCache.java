package com.drocode.android.orrsyreader.utils;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

class MemoryCache {
	private Map<String, SoftReference<Bitmap>> cache = Collections
			.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());

	Bitmap get(String id) {
		if (!cache.containsKey(id))
			return null;
		SoftReference<Bitmap> ref = cache.get(id);

		return ref.get();
	}

	void put(String id, Bitmap bitmap) {
		cache.put(id, new SoftReference<Bitmap>(bitmap));
	}

	void clear() {
		cache.clear();
	}
}
