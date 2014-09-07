package com.example.searchapp;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class TheProvider extends ContentProvider {

	private static final int URI_DIR = 1;
	private static final int URI_ITEM = 2;
	private static final int URI_SEARCH = 3;
	
	private static final String DB_MOVIES = "movies.db";
	public static final int DB_VERSION = 2;
	
	private static final String TABLE_MOVIES = "MOVIES";
	private static final String TAG = "TheProvider";
	
	public static final Uri AUTHORITY = Uri.parse("content://com.example.searchapp/" + TABLE_MOVIES);
	
	public static final String COL_ID = "_id";
	public static final String COL_TITLE = "COL_TITLE";
	public static final String COL_DIRECTOR = "COL_DIRECTOR";
	
	private static class MoviesOpenHelper extends SQLiteOpenHelper {
		
		public MoviesOpenHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
			Log.d(TAG, "MoviesOpenHelper() ");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "MoviesOpenHelper.onUpgrade()");
			db.execSQL( "DROP TABLE " + TABLE_MOVIES);
			onCreate(db);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "MoviesOpenHelper.onCreate()");
			db.execSQL( "CREATE TABLE " + TABLE_MOVIES + "("
					+ COL_ID + " integer primary key, " 
					+ COL_TITLE + " text not null, "
					+ COL_DIRECTOR + " text not null " + ")");
		}
	};
	
	
	private MoviesOpenHelper mHelper;
	private static UriMatcher mMatcher;
	
	private static HashMap<String, String> mProjectionMap = new HashMap<String,String>();
	
	static
	{
		mProjectionMap.put( COL_ID, COL_ID + " as " + COL_ID);
		mProjectionMap.put( SearchManager.SUGGEST_COLUMN_TEXT_1, COL_TITLE + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);
		mProjectionMap.put( SearchManager.SUGGEST_COLUMN_INTENT_DATA, COL_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA);
		
		mMatcher = new UriMatcher( UriMatcher.NO_MATCH );
		mMatcher.addURI("com.example.searchapp", TABLE_MOVIES, URI_DIR);
		mMatcher.addURI("com.example.searchapp", TABLE_MOVIES + "/#", URI_ITEM);
		
		mMatcher.addURI("com.example.searchapp", SearchManager.SUGGEST_URI_PATH_QUERY , URI_SEARCH);
		mMatcher.addURI("com.example.searchapp", SearchManager.SUGGEST_URI_PATH_QUERY  + "/*", URI_SEARCH);
		
		mMatcher.addURI("com.example.searchapp", SearchManager.SUGGEST_URI_PATH_SHORTCUT, URI_SEARCH);
		mMatcher.addURI("com.example.searchapp", SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", URI_SEARCH);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "delete(" + uri.toString() + "," + selection + ")");
		SQLiteDatabase db = mHelper.getWritableDatabase();

		int retVal = db.delete(TABLE_MOVIES, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return retVal;
	}

	@Override
	public String getType(Uri uri) {
		Log.d(TAG, "getType(" + uri.toString() + ")");
		return null;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values)
	{
		Log.d(TAG, "bulkInsert(" + uri.toString() + "," + values.toString() + ")");
		
		SQLiteDatabase db = mHelper.getWritableDatabase();
		int addedRows = 0;
		
		Log.d(TAG, "bulkInsert begin");
		db.beginTransaction();
		for (int i = 0; i < values.length; ++i)
		{
			Log.d(TAG, "bulkInsert inserting " + i);
			long id = db.insert(TABLE_MOVIES, null, values[i]);
			if (id > -1)
			{
				addedRows++;
			}
		}
		db.setTransactionSuccessful();
		Log.d(TAG, "bulkInsert complete"); 
		getContext().getContentResolver().notifyChange(uri, null);
		db.endTransaction();
		return addedRows;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert(" + uri.toString() + "," + values.toString() + ")");
		SQLiteDatabase db = mHelper.getWritableDatabase();
		long id = db.insert( TABLE_MOVIES, null, values);
		
		Uri newUri = null;
		
		if (id > -1)
		{
			newUri = ContentUris.withAppendedId(uri, id);
			getContext().getContentResolver().notifyChange(newUri, null);
		}
		
		return newUri;
	}

	@Override
	public boolean onCreate() {
		Log.d(TAG, "onCreate()");
		mHelper = new MoviesOpenHelper( getContext(), DB_MOVIES, null, DB_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query(" + uri.toString() + "," + selection + ")");
		
		SQLiteDatabase db = mHelper.getReadableDatabase();
		Cursor c = null;
		
		switch (mMatcher.match(uri))
		{
		case URI_SEARCH:
			Log.d(TAG, "Do search..");
			SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
			sqb.setTables( TABLE_MOVIES );
			sqb.setProjectionMap( mProjectionMap );
			selection = COL_TITLE + " LIKE \"%" + uri.getLastPathSegment() + "%\"";
			c = sqb.query( db, projection, selection, selectionArgs, null, null, sortOrder);
			//String tmp = sqb.buildQuery( projection, selection, selectionArgs, null, null, sortOrder, null);
			//Log.d(TAG, "SEARCH sqlite query: " + tmp);
			Log.d(TAG, "SEARCH Cursor size " + c.getCount() + " colCount" + c.getColumnCount());
			break;
		case URI_DIR:
			Log.d(TAG, "Want dir..");
			c = db.query(TABLE_MOVIES, projection, selection, selectionArgs, null, null, sortOrder);
			Log.d(TAG, "Cursor size " + c.getCount() + " colCount " + c.getColumnCount());
			break;
		case URI_ITEM:
			Log.d(TAG, "Want item..");
			break;
		}
		
		
		
		if (c != null)
		{
			c.setNotificationUri( getContext().getContentResolver(), uri);
		}
		
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.d(TAG, "update(" + uri.toString() + "," + selection + ")");
		return 0;
	}

}
