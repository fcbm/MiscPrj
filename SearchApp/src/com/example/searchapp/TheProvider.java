package com.example.searchapp;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
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
			Log.d(TAG, "MoviesOpenHelper()");
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
	
	static
	{
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
		
		return retVal;
	}

	@Override
	public String getType(Uri uri) {
		Log.d(TAG, "getType(" + uri.toString() + ")");
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(TAG, "insert(" + uri.toString() + "," + values.toString() + ")");
		return null;
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
		Cursor c = db.query(TABLE_MOVIES, projection, selection, selectionArgs, null, null, sortOrder);
		Log.d(TAG, "Cursor size " + c.getCount() + " colCount" + c.getColumnCount());
		
		c.setNotificationUri( getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.d(TAG, "update(" + uri.toString() + "," + selection + ")");
		return 0;
	}

}
