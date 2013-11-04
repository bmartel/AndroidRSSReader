package com.kryonation.androidrssreader.adapter;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.kryonation.androidrssreader.ArticleListActivity;
import com.kryonation.androidrssreader.R;
import com.kryonation.androidrssreader.rss.domain.Article;
import com.kryonation.androidrssreader.util.DateUtils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;



public class ArticleListAdapter extends ArrayAdapter<Article> implements OnSharedPreferenceChangeListener{

	private Activity currentActivity;
	private SharedPreferences settings;
	private String[] themePreference;
	public ArticleListAdapter(Activity activity, List<Article> articles) {
		super(activity, 0, articles);
		currentActivity = activity;
		settings = PreferenceManager.getDefaultSharedPreferences(currentActivity);
	    settings.registerOnSharedPreferenceChangeListener(this);
	    themePreference = getThemeSettings();
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = currentActivity.getLayoutInflater();

		View rowView = inflater.inflate(R.layout.fragment_article_list, null);
		Article article = getItem(position);
		TextView textView = (TextView) rowView.findViewById(R.id.article_title_text);
		TextView dateView = (TextView) rowView.findViewById(R.id.article_listing_smallprint);
		

		textView.setText(article.getTitle());
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

		LinearLayout row = (LinearLayout) rowView.findViewById(R.id.article_row_layout);
		if (article.isRead()){
			Log.d("RSS_Reader1","Setting article as read");
			row.setBackgroundColor(Color.parseColor(themePreference[4]));
			textView.setTypeface(Typeface.DEFAULT);
		}else{
			Log.d("RSS_Reader1","Setting article as unread");
			//Set theme color
			row.setBackgroundColor(Color.parseColor(themePreference[0]));	
			textView.setTypeface(Typeface.DEFAULT_BOLD);
		}
		
		Log.d("RSS_Reader1", "Applying theme to List View");
		textView.setTextColor(Color.parseColor(themePreference[1])); 
		dateView.setTextColor(Color.parseColor(themePreference[2])); 
		return rowView;

	} 

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equalsIgnoreCase("theme_color_preference")){
			themePreference = getThemeSettings();
		}
		
		
	}
	public String[] getThemeSettings(){
		Log.d("RSS_Reader1", "Getting theme preferences");
		int resourceId = currentActivity.getResources().getIdentifier(settings.getString("theme_color_preference", "theme_default"), "array", ArticleListActivity.PACKAGE_NAME);
		
		return currentActivity.getResources().getStringArray(resourceId);
	}
	
}