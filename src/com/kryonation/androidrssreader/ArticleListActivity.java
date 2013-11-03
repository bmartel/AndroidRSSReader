package com.kryonation.androidrssreader;

import com.kryonation.androidrssreader.adapter.ArticleListAdapter;
import com.kryonation.androidrssreader.db.DbAdapter;
import com.kryonation.androidrssreader.rss.domain.Article;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;


public class ArticleListActivity extends FragmentActivity implements ArticleListFragment.Callbacks {

    private boolean mTwoPane;
    private DbAdapter dba;
    public static String PACKAGE_NAME;
    public ArticleListActivity(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = getApplicationContext().getPackageName();
        setContentView(R.layout.activity_article_list);
        
        dba = new DbAdapter(this);

        
        if (findViewById(R.id.article_detail_container) != null) {
            mTwoPane = true;
            
            ((ArticleListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.article_list))
                    .setActivateOnItemClick(true);
        }
        else{
        	Log.d("RSS_Reader1", "Style: 1 Panel" );
        }
    }


	@Override
    public void onItemSelected(String id) {
        Article selected = (Article) ((ArticleListFragment) getSupportFragmentManager().findFragmentById(R.id.article_list)).getListAdapter().getItem(Integer.parseInt(id));
        Log.d("RSS_Reader1", "Loaded selected article id: " + id);
        //mark article as read
        dba.openToWrite();
        dba.markAsRead(selected.getGuid());
        dba.close();
        selected.setRead(true);
        ArticleListAdapter adapter = (ArticleListAdapter) ((ArticleListFragment) getSupportFragmentManager().findFragmentById(R.id.article_list)).getListAdapter();
        adapter.notifyDataSetChanged();
        Log.d("CHANGE", "Changing to read: ");
        
        
        //load article details to main panel
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putSerializable (Article.KEY, selected);
            
            ArticleDetailFragment fragment = new ArticleDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.article_detail_container, fragment)
                    .commit();

        } else {
        	Log.d("RSS_Reader1", "Loading activity  article id: " + id);
        	Bundle bundle = new Bundle();  
        	bundle.putSerializable(ArticleDetailFragment.ARG_ITEM_ID, selected);
        	
            Intent detailIntent = new Intent(this, ArticleDetailActivity.class);
            detailIntent.putExtras(bundle);
//            detailIntent.putExtra(ArticleDetailFragment.ARG_ITEM_ID, id);

            startActivity(detailIntent);
        }
    }
}
