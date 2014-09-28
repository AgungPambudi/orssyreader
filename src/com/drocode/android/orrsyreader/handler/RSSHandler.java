package com.drocode.android.orrsyreader.handler;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RSSHandler extends DefaultHandler {
	private StringBuffer buf;
	private ArrayList<NewsItem> feedItems;
	private NewsItem item;

	private boolean inItem = false;

	public ArrayList<NewsItem> getParsedItems() {
		return feedItems;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if ("channel".equals(localName)) {
			feedItems = new ArrayList<RSSHandler.NewsItem>();
		} else if ("item".equals(localName)) {
			item = new NewsItem();
			inItem = true;
		} else if ("title".equals(localName) && inItem) {
			buf = new StringBuffer();
		} else if ("link".equals(localName) && inItem) {
			buf = new StringBuffer();
		} else if ("description".equals(localName) && inItem) {
			buf = new StringBuffer();
		} else if ("pubDate".equals(localName) && inItem) {
			buf = new StringBuffer();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if ("item".equals(localName)) {
			feedItems.add(item);
			inItem = false;
		} else if ("title".equals(localName) && inItem) {
			item.title = buf.toString();
		} else if ("link".equals(localName) && inItem) {
			item.link = buf.toString();
		} else if ("description".equals(localName) && inItem) {
			item.description = buf.toString();
			Document doc = Jsoup.parse(buf.toString());
			Element img = doc.select("img").first();
			String testUrl = img.attr("src");
			if (testUrl.length() > 0) {
				if (!testUrl.startsWith("http:")) {
					testUrl = "http:" + testUrl;
				}
				item.thumbUrl = testUrl;
			}
		} else if ("pubDate".equals(localName) && inItem) {
			item.pubDate = buf.toString();
		}

		buf = null;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (buf != null) {
			for (int i = start; i < start + length; i++) {
				buf.append(ch[i]);
			}
		}
	}

	public class NewsItem {
		public String title;
		public String link;
		public String description;
		public String pubDate;
		public String thumbUrl;

		@Override
		public String toString() {
			return title;
		}
	}

}
