package com.fcbm.test.multifeedreader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class NewsProvider extends ContentProvider {

	private static final String TAG = "NewsProvider";
	
	private static final String NEWS_DATABASE = "newsdatabase.db";
	private static final int NEWS_DATABASE_VER = 13;
	
	private static final String NEWS_TABLE = "newstable";
	private static final String PAGES_TABLE = "pagestable";
	public static final String NEWS_COL_ID = "_id";
	public static final String NEWS_COL_SITE = "site";
	public static final String NEWS_COL_TITLE = "title";
	public static final String NEWS_COL_DATE = "date";
	public static final String NEWS_COL_AUTHOR = "author";
	public static final String NEWS_COL_CATEGORY = "category";
	public static final String NEWS_COL_DESCRIPTION = "description";
	public static final String NEWS_COL_LINK = "link";
	public static final String NEWS_COL_IMGLINK = "imglink";
	public static final String NEWS_COL_DATA = "_data";

	public static final String PAGES_COL_ID = NEWS_COL_ID;
	public static final String PAGES_COL_TITLE = NEWS_COL_TITLE;
	public static final String PAGES_COL_DATE = NEWS_COL_DATE;
	public static final String PAGES_COL_DESCRIPTION = NEWS_COL_DESCRIPTION;
	public static final String PAGES_COL_LINK = NEWS_COL_LINK;
	public static final String PAGES_COL_IMGLINK = NEWS_COL_IMGLINK;
	public static final String PAGES_COL_DATA = NEWS_COL_DATA;
	
	private static final UriMatcher mMatcher;
	private static final int CODE_ITEM = 100;
	private static final int CODE_DIR = 101;
	private static final int CODE_PAGES_ITEM = 102;
	private static final int CODE_PAGES_DIR = 103;
	
	public static final Uri authority = Uri.parse("content://com.fcbm.test.multifeedreader/" + NEWS_TABLE);
	public static final Uri authorityPages = Uri.parse("content://com.fcbm.test.multifeedreader/" + PAGES_TABLE);
	
	static
	{
		mMatcher = new UriMatcher( UriMatcher.NO_MATCH );
		mMatcher.addURI(authority.getAuthority(), NEWS_TABLE + "/", CODE_DIR);
		mMatcher.addURI(authority.getAuthority(), NEWS_TABLE + "/#", CODE_ITEM);
		mMatcher.addURI(authorityPages.getAuthority(), PAGES_TABLE + "/", CODE_PAGES_DIR);
		mMatcher.addURI(authorityPages.getAuthority(), PAGES_TABLE + "/#", CODE_PAGES_ITEM);

	}
	
	private NewsDbHelper mDbHelper;
	private PagesDbHelper mDbPagesHelper;
	
	private final class NewsDbHelper extends SQLiteOpenHelper {
		
		public NewsDbHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		private static final String CREATE_NEWS_TABLE = "CREATE TABLE IF NOT EXISTS " + NEWS_TABLE +" ("
				+NEWS_COL_ID + " integer primary key autoincrement,"
				+NEWS_COL_SITE + " text not null,"
				+NEWS_COL_TITLE + " text,"
				+NEWS_COL_DATE + " long,"
				+NEWS_COL_AUTHOR + " text,"
				+NEWS_COL_CATEGORY + " text,"
				+NEWS_COL_DESCRIPTION + " text,"
				+NEWS_COL_LINK + " text not null UNIQUE,"
				+NEWS_COL_IMGLINK + " text,"
				+NEWS_COL_DATA + " integer)";
		private static final String DROP_NEWS_TABLE = "DROP TABLE IF EXISTS " + NEWS_TABLE; 
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("TAG", "UpgradeDB");
			dropFiles(NewsProvider.authority, db, null, null);
			db.execSQL(DROP_NEWS_TABLE);//TODO:delete files
			onCreate(db);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL( CREATE_NEWS_TABLE );
		}
	};
	
	private final class PagesDbHelper extends SQLiteOpenHelper {
		
		public PagesDbHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		private static final String CREATE_PAGES_TABLE = "CREATE TABLE IF NOT EXISTS " + PAGES_TABLE +" ("
				+PAGES_COL_ID + " integer primary key autoincrement,"
				+PAGES_COL_TITLE + " text,"
				+PAGES_COL_DATE + " long,"
				+PAGES_COL_DESCRIPTION + " text,"
				+PAGES_COL_LINK + " text not null UNIQUE,"
				+PAGES_COL_IMGLINK + " text,"
				+PAGES_COL_DATA + " integer)";
		private static final String DROP_PAGES_TABLE = "DROP TABLE IF EXISTS " + PAGES_TABLE; 
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d("TAG", "UpgradeDB");
			dropFiles(NewsProvider.authorityPages, db, null, null);
			db.execSQL(DROP_PAGES_TABLE);//TODO:delete files
			onCreate(db);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL( CREATE_PAGES_TABLE );
		}
	};	
	
	@Override
	public boolean onCreate() 
	{
		CursorFactory factory = null;
		mDbHelper = new NewsDbHelper(getContext() , NEWS_DATABASE, factory, NEWS_DATABASE_VER);
		mDbPagesHelper = new PagesDbHelper(getContext() , NEWS_DATABASE, factory, NEWS_DATABASE_VER);
		return mDbHelper != null && mDbPagesHelper != null;
	}
	
	@Override
	public String getType(Uri uri) 
	{
		switch (mMatcher.match(uri))
		{
		case CODE_DIR:
		case CODE_PAGES_DIR:
			return "vnd.android.cursor.dir/vnd.multifeedreader.dir";
		case CODE_ITEM:
		case CODE_PAGES_ITEM:
			return "vnd.android.cursor.item/vnd.multifeedreader.item";
		}
		return null;
	}	
	
	private String getTable(Uri uri)
	{
		switch (mMatcher.match(uri))
		{
		case CODE_DIR:
		case CODE_ITEM:
			return NEWS_TABLE;
		case CODE_PAGES_DIR:
		case CODE_PAGES_ITEM:
			return PAGES_TABLE;		
		}
		return "";
	}
	
	private SQLiteDatabase getSafeDatabase(Uri uri, boolean writable)
	{
		SQLiteDatabase db = null;
		try
		{
			SQLiteOpenHelper helper = null;
			if (getTable(uri) == NEWS_TABLE)
				helper = mDbHelper;
			else if (getTable(uri) == PAGES_TABLE)
				helper = mDbPagesHelper;
			
			if (writable)
				db = helper.getWritableDatabase();
			else
				db = helper.getReadableDatabase();
		} catch (SQLiteException e)
		{
			Log.e(TAG, "Failed to get writable database", e);
		}
		return db;
	}
	
	private String fillSelection(Uri uri, String selection)
	{
		switch (mMatcher.match(uri))
		{
		case CODE_ITEM:
		case CODE_PAGES_ITEM:
			String rowId = uri.getPathSegments().get(1);
			String selectionTail = (selection != null && !TextUtils.isEmpty(selection)) ? " AND (" + selection + ")" : ""; 
			selection = NEWS_COL_ID + "=" + rowId + selectionTail;
		}
		
		return selection;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		SQLiteDatabase db = getSafeDatabase(uri, true);
		if (db == null) { return 0; }

		selection = fillSelection(uri, selection);
		
		dropFiles(uri, null, selection, selectionArgs);
		int deletedItems = db.delete(getTable(uri), selection, selectionArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return deletedItems;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		SQLiteDatabase db = getSafeDatabase(uri, true);
		if (db == null) { return null; }
		
		String nullColumnHack = null;
		
		long newRowId = -1;
		
		try
		{
			newRowId = db.insertOrThrow(getTable(uri), nullColumnHack, values);
		} catch (SQLiteConstraintException e)
		{
			Log.e(TAG, "Duplicate link!");
		}
		
		
		Uri newUrl = null;
		
		if (newRowId > -1)
		{
			Log.i(TAG, "Insert " + newRowId);
			newUrl = ContentUris.withAppendedId(uri, newRowId);
			// TODO: check newUrl vs uri
			getContext().getContentResolver().notifyChange(newUrl, null);
		}
		
		return newUrl;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		SQLiteDatabase db = getSafeDatabase(uri, false);
		if (db == null) { return null; }
		
		selection = fillSelection(uri, selection);
		
		String groupBy = null;
		String having = null;
		
		Cursor newCursor = db.query(getTable(uri), projection, selection, selectionArgs, groupBy, having, sortOrder);
		
		if (newCursor != null)
		{
			newCursor.setNotificationUri(getContext().getContentResolver(), uri);
		}
		
		return newCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

		SQLiteDatabase db = getSafeDatabase(uri, true);
		if (db == null) { return 0; }
		
		selection = fillSelection(uri, selection);
		
		if (selection == null)
		{
			selection = "1";
		}
		
		int updatedRows = db.update(getTable(uri), values, selection, selectionArgs);
		
		if (updatedRows > 0)
		{
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return updatedRows;
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException
	{
		String rowId = uri.getPathSegments().get(1);
		String fName = rowId;
		
		File file = new File(getContext().getCacheDir(), fName);

		Log.d("fe1", "fileExists?");
		if (!file.exists())
		{
			Log.d("fe1", "fileExists?no");
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		int fMode = ParcelFileDescriptor.MODE_READ_ONLY;
		
		if (mode.contains("w") && mode.contains("r"))
		{
			fMode |= ParcelFileDescriptor.MODE_READ_WRITE;
		}
		if (mode.contains("w"))
		{
			fMode |= ParcelFileDescriptor.MODE_WRITE_ONLY;
		}
		else if (mode.contains("r"))
		{
			fMode |= ParcelFileDescriptor.MODE_READ_ONLY;
		}
		else if (mode.contains("a"))
		{
			fMode |= ParcelFileDescriptor.MODE_APPEND;
		}
	
		return ParcelFileDescriptor.open(file, fMode);
	}

	private void dropFiles(Uri uri, SQLiteDatabase db, String selection, String[] selectionArgs)
	{
		String[] projection = new String[] {NewsProvider.NEWS_COL_ID};
		Cursor c = null;
		if (db == null)
			c = query(uri, projection, selection, selectionArgs, null);
		else
			c = db.query(NEWS_TABLE, projection, selection, selectionArgs, null, null, null);
		
		if (c == null)
			return;
		
		for (c.moveToFirst() ; !c.isAfterLast() ; c.moveToNext())
		{
			File f = new File(getContext().getCacheDir(), c.getString( c.getColumnIndex( NEWS_COL_ID )));
			if (f.exists()) 
			{
				f.delete();
			}
		}
		c.close();
	}
}
