package com.kryonation.androidrssreader.rss;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.kryonation.androidrssreader.ArticleListFragment;
import com.kryonation.androidrssreader.adapter.ArticleListAdapter;
import com.kryonation.androidrssreader.db.DbAdapter;
import com.kryonation.androidrssreader.rss.domain.Article;
import com.kryonation.androidrssreader.rss.parser.RssHandler;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;



public class RssService extends AsyncTask<String, Void, List<Article>> {

	private ProgressDialog progress;
	private Context context;
	private ArticleListFragment articleListFrag;

	public RssService(ArticleListFragment articleListFragment) {
		context = articleListFragment.getActivity();
		articleListFrag = articleListFragment;
		progress = new ProgressDialog(context);
		progress.setMessage("Loading...");
	}


	protected void onPreExecute() {
		Log.d("ASYNC", "PRE EXECUTE");
		progress.show();
	}


	protected  void onPostExecute(final List<Article>  articles) {
		Log.d("ASYNC", "POST EXECUTE");
		articleListFrag.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if(articles == null){
					Log.d("RSS_Reader1", "articles are null");
				}
					
				for (Article a : articles){
					Log.d("DB", "Searching DB for GUID: " + a.getGuid());
					DbAdapter dba = new DbAdapter(articleListFrag.getActivity());
		            dba.openToRead();
		            Article fetchedArticle = dba.getBlogListing(a.getGuid());
		            dba.close();
					if (fetchedArticle == null){
						Log.d("DB", "Found entry for first time: " + a.getTitle());
						dba = new DbAdapter(articleListFrag.getActivity());
			            dba.openToWrite();
			            dba.insertBlogListing(a.getGuid());
			            dba.close();
					}else{
						a.setDbId(fetchedArticle.getDbId());
						a.setOffline(fetchedArticle.isOffline());
						a.setRead(fetchedArticle.isRead());
					}
				}
				ArticleListAdapter adapter = new ArticleListAdapter(articleListFrag.getActivity(), articles);
				articleListFrag.setListAdapter(adapter);
				adapter.notifyDataSetChanged();
				
			}
		});
		progress.dismiss();
	}


	@Override
	protected List<Article> doInBackground(String... urls) {
		String feed = urls[0];

		URL url = null;
		try {

			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();

			url = new URL(feed);
			RssHandler rh = new RssHandler();

			xr.setContentHandler(rh);
			xr.parse(new InputSource(url.openStream()));

			List<Article> articleList = rh.getArticleList();
			
			Log.d("ASYNC", "PARSING FINISHED");
			Log.d("ASYNC", "ARTICLE LIST" + articleList.size() );
			return articleList;

		} catch (IOException e) {
			Log.d("RSS Handler IO", e.getMessage() + " >> " + e.toString());
		} catch (SAXException e) {
			Log.d("RSS Handler SAX", e.toString());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			Log.d("RSS Handler Parser Config", e.toString());
		}
		
		Log.d("ASYNC", "NO ARTICLES FOUND");
		return null;

	}
}