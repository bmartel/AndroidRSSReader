package com.kryonation.androidrssreader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.kryonation.androidrssreader.adapter.ArticleListAdapter;
import com.kryonation.androidrssreader.db.DbAdapter;
import com.kryonation.androidrssreader.rss.domain.Article;
import com.kryonation.androidrssreader.util.DateUtils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ArticleDetailFragment extends Fragment {

	public static final String ARG_ITEM_ID = "item_id";

	Article displayedArticle;
	DbAdapter db;

	public ArticleDetailFragment() {
		setHasOptionsMenu(true); // this enables action bar to be set from
									// fragment
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			String title = displayedArticle.getTitle();
			String pubDate = displayedArticle.getPubDate();
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

			String content = displayedArticle.getDescription() + " "
					+ displayedArticle.getContentLink();
			((TextView) rootView.findViewById(R.id.article_title))
					.setText(title);
			((TextView) rootView.findViewById(R.id.article_author))
					.setText(pubDate);
			TextView contentView = (TextView) rootView
					.findViewById(R.id.article_detail);
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
}