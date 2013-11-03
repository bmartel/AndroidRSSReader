package com.kryonation.androidrssreader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.kryonation.androidrssreader.adapter.ArticleListAdapter;
import com.kryonation.androidrssreader.db.DbAdapter;
import com.kryonation.androidrssreader.rss.domain.Article;
import com.kryonation.androidrssreader.util.DateUtils;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ArticleDetailFragment extends Fragment implements OnSharedPreferenceChangeListener {
	public static final int TITLE_FONT_SIZE = 20;
	public static final int DATE_FONT_SIZE = 12;
	public static final int CONTENT_FONT_SIZE = 16;
	public static final String ARG_ITEM_ID = "item_id";
	
	private SharedPreferences settings;
	private TextView titleView;
	private TextView contentView;
	private TextView authorView;
	Article displayedArticle;
	DbAdapter db;

	public ArticleDetailFragment() {
		setHasOptionsMenu(true); // this enables action bar to be set from
									// fragment
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    settings.registerOnSharedPreferenceChangeListener(this);
		db = new DbAdapter(getActivity());
		if (getArguments().containsKey(Article.KEY)) {
			displayedArticle = (Article) getArguments().getSerializable(
					Article.KEY);
		} else if (getArguments().containsKey(ARG_ITEM_ID)) {
			Log.d("RSS_Reader1", "Article serializble found by ARG_ITEM_ID");
			displayedArticle = (Article) getArguments().getSerializable(
					ARG_ITEM_ID);

		} else {
			Log.d("RSS_Reader1", "Article serializble key could not be found");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_article_detail,
				container, false);
		if (displayedArticle != null) {

			//Get the article details from the article object
			String title = displayedArticle.getTitle();
			String pubDate = displayedArticle.getPubDate();
			String content = displayedArticle.getDescription() + " "
					+ displayedArticle.getContentLink();
			SimpleDateFormat df = new SimpleDateFormat(
					"EEE, dd MMM yyyy kk:mm:ss Z", Locale.ENGLISH);
			
			try {
				Date pDate = df.parse(pubDate);
				pubDate = "This article was published "
						+ DateUtils.getDateDifference(pDate);
			} catch (ParseException e) {
				Log.d("DATE PARSING", "Error parsing date..");
				pubDate = "published on ?"; // displayedArticle.getAuthor();
			}
			
			//Get the view components
			contentView = (TextView) rootView.findViewById(R.id.article_detail);
			titleView =(TextView) rootView.findViewById(R.id.article_title);
			authorView = (TextView) rootView.findViewById(R.id.article_author);
			

//			String themeName = prefs.getString("theme_color_preference", "theme_default");
//			
//			int id = 1;
//			
//			String[] theme = getResources().getStringArray(id);
			//Set the content to the article's details
			titleView.setText(title);	
			authorView.setText(pubDate);
			contentView.setText(Html.fromHtml(content));
			contentView.setMovementMethod(LinkMovementMethod.getInstance());
			

			
		} else {
			Log.d("RSS_Reader1", "Article detail was empty");
		}
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.detailmenu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Log.d("item ID : ", "onOptionsItemSelected Item ID " + id);
		if (id == R.id.actionbar_saveoffline) {
			db.openToWrite();
			boolean saved = db.saveForOffline(displayedArticle.getGuid(),
					displayedArticle.getPubDate(),
					displayedArticle.getTitle(),
					displayedArticle.getDescription(),
					displayedArticle.getAuthor());
			db.close();
			if (saved) {
				Toast.makeText(getActivity().getApplicationContext(),
						"This article has been saved for offline reading.",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(
						getActivity().getApplicationContext(),
						"There was trouble saving the article for offline reading.",
						Toast.LENGTH_LONG).show();
			}

			return saved;
		} else if (id == R.id.actionbar_markunread) {
			db.openToWrite();
			db.markAsUnread(displayedArticle.getGuid());
			db.close();
			displayedArticle.setRead(false);
			ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getActivity()
					.getSupportFragmentManager().findFragmentById(
							R.id.article_list)).getListAdapter();
			adapter.notifyDataSetChanged();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		// Process application setting changes
		//Get the Font and Theme preferences
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());			
		if(key.equalsIgnoreCase("font_size_preference")){
			Double fontScale = Double.parseDouble(prefs.getString(key, "1.0"));
			
			Log.d("RSS_Reader1", "Setting font preferences");
			
			//Set the font preferences
			titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)(TITLE_FONT_SIZE * fontScale));
			authorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)(DATE_FONT_SIZE * fontScale));
			contentView.setTextSize(TypedValue.COMPLEX_UNIT_SP, (int)(CONTENT_FONT_SIZE * fontScale));
			
			Log.d("RSS_Reader1", "TitleSize: " + titleView.getTextSize());
			Log.d("RSS_Reader1", "DateSize: " + authorView.getTextSize());
			Log.d("RSS_Reader1", "ContentSize: " + contentView.getTextSize());
		}
	}
}