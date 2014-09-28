package com.drocode.android.orrsyreader.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import com.drocode.android.orrsyreader.OrssyReader;
import com.drocode.android.orrsyreader.R;
import com.drocode.android.orrsyreader.utils.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LazyAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;

	public LazyAdapter(Activity activity,
			ArrayList<HashMap<String, String>> data) {
		this.activity = activity;
		this.data = data;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext());
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int pos) {
		return pos;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_row, null);

		TextView title = (TextView) vi.findViewById(R.id.title);
		TextView description = (TextView) vi.findViewById(R.id.description);
		ImageView thumb_image = (ImageView) vi.findViewById(R.id.list_image);

		HashMap<String, String> article = new HashMap<String, String>();
		article = data.get(pos);

		title.setText(article.get(OrssyReader.KEY_TITLE));
		description.setText(article.get(OrssyReader.KEY_DESCRIPTION));
		imageLoader.DisplayImage(article.get(OrssyReader.KEY_THUMB_URL),
				thumb_image);

		return vi;
	}

}