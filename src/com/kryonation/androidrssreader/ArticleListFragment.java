package com.kryonation.androidrssreader;
import android.preference.PreferenceManager;
import com.kryonation.androidrssreader.rss.RssService;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;


public class ArticleListFragment extends ListFragment implements OnSharedPreferenceChangeListener {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    private static final String BLOG_URL = "http://www.nhl.com/rss/news.xml"; //String[] NHL_FEEDS = getResources().getStringArray(R.array.nhl_feeds); 
    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private RssService rssService;
    private SharedPreferences settings;

    public interface Callbacks {
        public void onItemSelected(String id);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    public ArticleListFragment() {
    	setHasOptionsMenu(true);	//this enables us to set actionbar from fragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    settings.registerOnSharedPreferenceChangeListener(this);
	    
        refreshList(settings.getString("rss_feed_preference",BLOG_URL));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(String.valueOf(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }
    
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refreshmenu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
     // Handle item selection
        switch (item.getItemId()) {
            case R.id.actionbar_refresh:
            	refreshList(settings.getString("rss_feed_preference", BLOG_URL));
                return true;
            case R.id.actionbar_settings:
            	Log.d("RSS_Reader1", "Settings action fired");

                Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
//        return false;
    }
    
    //Refresh the list adapter using Async operator RssService
    private void refreshList(String feed){
	
    	// Get the selected feed from preferences, or the default
    	rssService = new RssService(this);
        rssService.execute(feed);
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Update the list fragment if associated preferences change
		if(key.equalsIgnoreCase("rss_feed_preference")){
			String feed_url = sharedPreferences.getString(key, BLOG_URL);
			refreshList(feed_url);
		}
		
	}
    
}
