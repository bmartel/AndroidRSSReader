package com.kryonation.androidrssreader;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

public class ArticleDetailActivity extends FragmentActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_article_detail);
        Log.d("RSS_Reader1", "View Set to ArticleActivityView");
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
        	Log.d("RSS_Reader1", "Saved Instance Not Found");
            Bundle arguments = new Bundle();
            
            Log.d("RSS_Reader1", "Grabbing Bundled arguments");
            
            arguments.putSerializable(ArticleDetailFragment.ARG_ITEM_ID, getIntent().getSerializableExtra(ArticleDetailFragment.ARG_ITEM_ID));

            ArticleDetailFragment fragment = new ArticleDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.article_detail_container, fragment)
                    .commit();
        }else{
        	Log.d("RSS_Reader1", "Saved Instance Found");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, ArticleListActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
