package com.drocode.android.orrsyreader;

import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.methods.HttpGet;
import org.xml.sax.InputSource;

import com.drocode.android.orrsyreader.adapter.LazyAdapter;
import com.drocode.android.orrsyreader.handler.RSSHandler;
import com.drocode.android.orrsyreader.handler.RSSHandler.NewsItem;
import com.drocode.android.orrsyreader.prefs.Preferences;
import com.drocode.android.orrsyreader.utils.RestTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

public class OrssyReader extends Activity {

	private static final String FEED_ACTION = "com.drocode.android.rest.FEED";
	private static final int SHOW_PREFERENCES = 1;

	public static final String KEY_ID = "id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_LINK = "link";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_THUMB_URL = "thumb_url";

	private Intent changeListenerIntent = null;
	private ListView list;
	private LazyAdapter adapter;
	private ArrayList<HashMap<String, String>> articleList;

	boolean wifionly = false;
	private String rssFeedUrl = "http://drocode.com/feed/";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		// Set content
		setContentView(R.layout.main);

		// Set the titlebar layout
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title_bar);

		articleList = new ArrayList<HashMap<String, String>>();
		list = (ListView) this.findViewById(R.id.rssreaderListView);
		adapter = new LazyAdapter(this, articleList);
		list.setAdapter(adapter);

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long id) {
				HashMap<String, String> map = articleList.get(pos);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(map.get(KEY_LINK)));
				startActivity(intent);
			}
		});
		updateFromPreferences();
	}

	public void btnHeaderClick(View target) {
		switch (target.getId()) {
		case R.id.header_refresh_btn:
			updateArticles();
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (changeListenerIntent == null)
			changeListenerIntent = registerReceiver(receiver, new IntentFilter(
					FEED_ACTION));
		updateFromPreferences();
		updateArticles();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (changeListenerIntent != null) {
			unregisterReceiver(receiver);
			changeListenerIntent = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.setting:
			Intent i = new Intent(this, Preferences.class);
			startActivityForResult(i, SHOW_PREFERENCES);
			return true;

		case R.id.helpabout:
			updateArticles();
			return true;

		case R.id.exit:
			exitOptionsDialog();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SHOW_PREFERENCES) {
			if (resultCode == Activity.RESULT_OK) {
				updateFromPreferences();
				updateArticles();
			}
		}
	}

	private void updateArticles() {
		if (wifionly) {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifiNetworkInfo = cm
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifiNetworkInfo != null) {
				if (!wifiNetworkInfo.isConnected()) {
					new AlertDialog.Builder(this)
							.setMessage(
									"You have the preference set to only update articles on WiFi.")
							.setCancelable(false)
							.setNeutralButton("OK",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									}).show();
					return;
				}
			}
		}
		registerReceiver(receiver, new IntentFilter(FEED_ACTION));

		try {
			HttpGet feedRequest = new HttpGet(new URI(rssFeedUrl));
			RestTask task = new RestTask(this, FEED_ACTION);
			task.execute(feedRequest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateFromPreferences() {
		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		wifionly = prefs.getBoolean(Preferences.PREF_WIFI_ONLY, false);
		rssFeedUrl = prefs.getString(Preferences.PREF_RSS_URL,
				"http://drocode.com/feed");
	}

	private void exitOptionsDialog() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.menu_exit)
				.setMessage(R.string.app_exit_message)
				.setNegativeButton(R.string.str_no,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						})
				.setPositiveButton(R.string.str_ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						}).show();
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);

			try {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser p = factory.newSAXParser();
				RSSHandler parser = new RSSHandler();
				p.parse(new InputSource(new StringReader(response)), parser);

				adapter.imageLoader.clearCache();
				articleList.clear();
				int idVal = 1;

				for (NewsItem item : parser.getParsedItems()) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(KEY_ID, Integer.toString(idVal));
					map.put(KEY_TITLE, item.title);
					map.put(KEY_DESCRIPTION, item.pubDate);
					map.put(KEY_LINK, item.link);
					map.put(KEY_THUMB_URL, item.thumbUrl);

					articleList.add(map);
					idVal++;
				}
				adapter.notifyDataSetChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

}