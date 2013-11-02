package com.kryonation.androidrssreader.db;

import java.util.ArrayList;
import java.util.List;

import com.kryonation.androidrssreader.rss.domain.Article;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;


public class DbAdapter{
	
	public static final String KEY_ROWID = BaseColumns._ID;
	public static final String KEY_GUID = "guid";
	public static final String KEY_DATE = "pub_date";
	public static final String KEY_TITLE = "title";
	public static final String KEY_CONTENT = "description";
	public static final String KEY_LINK = "link";
	public static final String KEY_READ = "read";
	public static final String KEY_OFFLINE = "offline";    
	
	private static final String DATABASE_NAME = "blogposts";
	private static final String DATABASE_TABLE1 = "blogpostlist";
	private static final int DATABASE_VERSION = 3;
	
	private static final String DATABASE_CREATE_LIST_TABLE = "create table " + DATABASE_TABLE1 + " (" + 
																KEY_ROWID +" integer primary key autoincrement, "+ 
																KEY_GUID + " text not null, " +
																KEY_DATE + " text null, " +
																KEY_TITLE + " text null, " +
																KEY_CONTENT + " text null, " +
																KEY_LINK + " text null, " +
																KEY_READ + " boolean not null, " + 
																KEY_OFFLINE + " boolean not null);";


	private SQLiteHelper sqLiteHelper;
	private SQLiteDatabase sqLiteDatabase;
	private Context context;

	
	public DbAdapter(Context c){
		context = c;
		
	}

	public DbAdapter openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getReadableDatabase();
		return this; 
	}

	public DbAdapter openToWrite() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getWritableDatabase();
		return this; 
	}

	public void close(){
		sqLiteHelper.close();
	}

	public class SQLiteHelper extends SQLiteOpenHelper {
		public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE_LIST_TABLE);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE1 );
            onCreate(db);
		}
	}

    public long insertBlogListing(String guid) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_GUID, guid);
        initialValues.put(KEY_READ, false);
        initialValues.put(KEY_OFFLINE, false);
        return sqLiteDatabase.insert(DATABASE_TABLE1, null, initialValues);
    }
    
    public Article getBlogListing(String guid) throws SQLException {
        Cursor mCursor =
        		sqLiteDatabase.query(true, DATABASE_TABLE1, new String[] {
                		KEY_ROWID,
                		KEY_GUID, 
                		KEY_TITLE, 
                		KEY_CONTENT, 
                		KEY_LINK, 
                		KEY_READ,
                		KEY_OFFLINE
                		}, 
                		KEY_GUID + "= '" + guid + "'", 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null && mCursor.getCount() > 0) {
        	mCursor.moveToFirst();
        	Article a = new Article();
   			a.setGuid(mCursor.getString(mCursor.getColumnIndex(KEY_GUID)));
   			a.setRead(mCursor.getInt(mCursor.getColumnIndex(KEY_READ)) > 0);
   			a.setDbId(mCursor.getLong(mCursor.getColumnIndex(KEY_ROWID)));
   			a.setOffline(mCursor.getInt(mCursor.getColumnIndex(KEY_OFFLINE)) > 0);
   			return a;
        }
        return null;
    }
    
    public List<Article> getOfflineArticles() throws SQLException {
    	List<Article> articleList = new ArrayList<Article>();
        Cursor mCursor =
        		sqLiteDatabase.query(true, DATABASE_TABLE1, new String[] {
                		KEY_ROWID,
                		KEY_GUID, 
                		KEY_DATE, 
                		KEY_TITLE, 
                		KEY_CONTENT, 
                		KEY_LINK, 
                		KEY_READ,
                		KEY_OFFLINE
                		}, 
                		KEY_OFFLINE + "> " + 0, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        int rowCount = mCursor.getCount();
        Log.d("RSS_Reader1", "Querying the DB for offline articles");
        if (mCursor != null && rowCount > 0) {
        	Log.d("RSS_Reader1", "DB Cursor retrieved " +rowCount+" rows");
        	if (mCursor.moveToFirst()) {
        		Log.d("RSS_Reader1", "Processing DB record");
                while (mCursor.isAfterLast() == false) {
                	Log.d("RSS_Reader1", "Creating a new Article from DB record");
                	Article a = new Article();
           			a.setGuid(mCursor.getString(mCursor.getColumnIndex(KEY_GUID)));
           			a.setPubDate(mCursor.getString(mCursor.getColumnIndex(KEY_DATE)));
           			a.setTitle(mCursor.getString(mCursor.getColumnIndex(KEY_TITLE)));
           			a.setDescription(mCursor.getString(mCursor.getColumnIndex(KEY_CONTENT)));
           			a.setAuthor(mCursor.getString(mCursor.getColumnIndex(KEY_LINK)));
           			a.setRead(mCursor.getInt(mCursor.getColumnIndex(KEY_READ)) > 0);
           			a.setDbId(mCursor.getLong(mCursor.getColumnIndex(KEY_ROWID)));
           			a.setOffline(mCursor.getInt(mCursor.getColumnIndex(KEY_OFFLINE)) > 0);
           			articleList.add(a);
           			Log.d("RSS_Reader1", "Added article to the list");
                	mCursor.moveToNext();
                }
            }
        	Log.d("RSS_Reader1", "Returning the Article list");
   			return articleList;
        }
        Log.d("RSS_Reader1", "DB Cursor was null, or no rows were retrieved");
        return null;
    }
    public boolean markAsUnread(String guid) {
        ContentValues args = new ContentValues();
        args.put(KEY_READ, false);
        return sqLiteDatabase.update(DATABASE_TABLE1, args, KEY_GUID + "='" + guid+"'", null) > 0;
    }
    
    public boolean markAsRead(String guid) {
        ContentValues args = new ContentValues();
        args.put(KEY_READ, true);
        return sqLiteDatabase.update(DATABASE_TABLE1, args, KEY_GUID + "='" + guid+"'", null) > 0;
    }

    public boolean saveForOffline(String guid, String date, String title, String content, String link) {
        ContentValues args = new ContentValues();
        args.put(KEY_OFFLINE, true);
        args.put(KEY_DATE, date);
        args.put(KEY_TITLE, title);
        args.put(KEY_CONTENT, content);
        args.put(KEY_LINK, link);
        
        Log.d("RSS_Reader1", "Attempting to save article offline");
        boolean savedCorrectly = sqLiteDatabase.update(DATABASE_TABLE1, args, KEY_GUID + "='" + guid+"'", null) > 0;
        if(savedCorrectly)
        	Log.d("RSS_Reader1", "Saved Article to Database!");
        else
        	Log.d("RSS_Reader1", "Failed to save correctly");
        
        return savedCorrectly;
    }
    
    public boolean removeFromOffline(String guid) {
        ContentValues args = new ContentValues();
        args.put(KEY_OFFLINE, false);
        return sqLiteDatabase.update(DATABASE_TABLE1, args, KEY_GUID + "='" + guid+"'", null) > 0;
    }
    
    public boolean delete(String guid) {
        return sqLiteDatabase.delete(DATABASE_TABLE1, KEY_GUID + "='" + guid+"'", null) > 0;
    }
}