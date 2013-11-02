package com.kryonation.androidrssreader.adapter;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.kryonation.androidrssreader.R;
import com.kryonation.androidrssreader.rss.domain.Article;
import com.kryonation.androidrssreader.util.DateUtils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;



public class ArticleListAdapter extends ArrayAdapter<Article> {

	public ArticleListAdapter(Activity activity, List<Article> articles) {
		super(activity, 0, articles);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Activity activity = (Activity) getContext();
		LayoutInflater inflater = activity.getLayoutInflater();

		View rowView = inflater.inflate(R.layout.fragment_article_list, null);
		Article article = getItem(position);
		

		TextView textView = (TextView) rowView.findViewById(R.id.article_title_text);
		textView.setText(article.getTitle());
		
		TextView dateView = (TextView) rowView.findViewById(R.id.article_listing_smallprint);
		String pubDate = article.getPubDate();
		SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss Z", Locale.ENGLISH);
		Date pDate;
		try {
			pDate = df.parse(pubDate);
			pubDate = "published " + DateUtils.getDateDifference(pDate);
		} catch (ParseException e) {
			Log.d("DATE PARSING", "Error parsing date..");
			pubDate = "Link to article: " + article.getAuthor();
		}
		dateView.setText(pubDate);

		
		if (!article.isRead()){
			LinearLayout row = (LinearLayout) rowView.findViewById(R.id.article_row_layout);
			row.setBackgroundColor(Color.WHITE);
			textView.setTypeface(Typeface.DEFAULT_BOLD);
		}
		return rowView;

	} 
}