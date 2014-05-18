package com.fcbm.test.multifeedreader.provider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class NewsProvider extends ContentProvider {

	private static final String TAG = "NewsProvider";
	
	private static final String DATABASE = "newsdatabase.db";
	private static final int DATABASE_VER = 21;

	private static final BaseContract pc = new PagesContract();
	private static final BaseContract nc = new NewsContract();
	private static final BaseContract pjnc = new PagesJoinNewsContract();
	
	
	//private static final String PAGES_AND_COUNTED_NEWS = 
	//		PagesContract.TABLE + " LEFT JOIN " + NewsContract.TABLE 
	//		+ "  ON ( " + pc.addPrefix(PagesContract.COL_LINK) + "=" + nc.addPrefix(NewsContract.COL_SITE) + ") ";
			
	//public static final String COL_CNT = "CNT";
	//public static final String COL_COUNT = "COUNT(*) AS " + COL_CNT;
	
	private static final UriMatcher mMatcher;
	private static final int CODE_PAGES_ITEM = 100;
	private static final int CODE_PAGES_DIR = 101;
	private static final int CODE_NEWS_ITEM = 102;
	private static final int CODE_NEWS_DIR = 103;
	//private static final int CODE_JOIN_ITEM = 104;
	//private static final int CODE_JOIN_DIR = 105;
	private static final int CODE_PAGES_AND_COUNTED_NEWS = 106;
	
	public static final Uri authorityNews = Uri.parse("content://com.fcbm.test.multifeedreader/" + NewsContract.TABLE);
	//public static final Uri authorityNewsJoinPages = Uri.parse("content://com.fcbm.test.multifeedreader/" + JOIN_TABLE);
	//public static final Uri authorityNewsJoinPagesGroupByPagesLink = Uri.parse("content://com.fcbm.test.multifeedreader/" + JOIN_TABLE + "/" + PagesContract.COL_LINK);
	public static final Uri authorityPagesAndCountedNews = Uri.parse("content://com.fcbm.test.multifeedreader/" + PagesJoinNewsContract.TABLE );
	public static final Uri authorityPages = Uri.parse("content://com.fcbm.test.multifeedreader/" + PagesContract.TABLE);
	
	private NewsDbHelper mDbHelper;
	
	static
	{
		mMatcher = new UriMatcher( UriMatcher.NO_MATCH );
		mMatcher.addURI(authorityNews.getAuthority(), NewsContract.TABLE + "/", CODE_NEWS_DIR);
		mMatcher.addURI(authorityNews.getAuthority(), NewsContract.TABLE + "/#", CODE_NEWS_ITEM);
		mMatcher.addURI(authorityPages.getAuthority(), PagesContract.TABLE + "/", CODE_PAGES_DIR);
		mMatcher.addURI(authorityPages.getAuthority(), PagesContract.TABLE + "/#", CODE_PAGES_ITEM);
		//mMatcher.addURI(authorityNewsJoinPages.getAuthority(), JOIN_TABLE + "/", CODE_JOIN_DIR);
		//mMatcher.addURI(authorityNewsJoinPages.getAuthority(), JOIN_TABLE + "/#", CODE_JOIN_ITEM);
		mMatcher.addURI(authorityPagesAndCountedNews.getAuthority(), PagesJoinNewsContract.TABLE, CODE_PAGES_AND_COUNTED_NEWS);
	}
	
	@Override
	public boolean onCreate() 
	{
		CursorFactory factory = null;
		mDbHelper = new NewsDbHelper(getContext() , DATABASE, factory, DATABASE_VER);
		return mDbHelper != null;
	}
	
	@Override
	public String getType(Uri uri) 
	{
		switch (mMatcher.match(uri))
		{
		case CODE_NEWS_DIR:
		case CODE_PAGES_DIR:
		//case CODE_JOIN_DIR:
		case CODE_PAGES_AND_COUNTED_NEWS:
			return "vnd.android.cursor.dir/vnd.multifeedreader.dir";
		case CODE_NEWS_ITEM:
		case CODE_PAGES_ITEM:
		//case CODE_JOIN_ITEM:
			return "vnd.android.cursor.item/vnd.multifeedreader.item";
		}
		return null;
	}	

	private String getTable(Uri uri)
	{
		
		switch (mMatcher.match(uri))
		{
		case CODE_NEWS_DIR:
		case CODE_NEWS_ITEM:
			return NewsContract.TABLE;
		case CODE_PAGES_DIR:
		case CODE_PAGES_ITEM:
		case CODE_PAGES_AND_COUNTED_NEWS:
			return PagesContract.TABLE;		
		}
		return "";
	}
	
	private String getTableForQuery(Uri uri)
	{
		
		switch (mMatcher.match(uri))
		{
		case CODE_NEWS_DIR:
		case CODE_NEWS_ITEM:
			return NewsContract.TABLE;
		case CODE_PAGES_DIR:
		case CODE_PAGES_ITEM:
			return PagesContract.TABLE;		
		//case CODE_JOIN_DIR:
		//case CODE_JOIN_ITEM:
		case CODE_PAGES_AND_COUNTED_NEWS:
			return PagesJoinNewsContract.TABLE;		
		}
		return "";
	}

	private Map<String,String> getProjectionMap(Uri uri)
	{
		
		switch (mMatcher.match(uri))
		{
		case CODE_NEWS_DIR:
		case CODE_NEWS_ITEM:
			return nc.getProjectionMap();
		case CODE_PAGES_DIR:
		case CODE_PAGES_ITEM:
			return pc.getProjectionMap();		
		//case CODE_JOIN_DIR:
		//case CODE_JOIN_ITEM:
		case CODE_PAGES_AND_COUNTED_NEWS:
			return pjnc.getProjectionMap();		
		}
		return null;
	}
	
	
	private String getGroupBy(Uri uri)
	{
		switch (mMatcher.match(uri))
		{
		case CODE_PAGES_AND_COUNTED_NEWS:
			return pc.addPrefix(PagesContract.COL_LINK);
		default :
			return "";
		}
	}
	
	private SQLiteDatabase getSafeDatabase(Uri uri, boolean writable)
	{
		SQLiteDatabase db = null;
		try
		{
			if (writable)
				db = mDbHelper.getWritableDatabase();
			else
				db = mDbHelper.getReadableDatabase();
		} catch (SQLiteException e)
		{
			Log.e(TAG, "Failed to get writable database", e);
		}
		return db;
	}
	
	private String fillSelection(Uri uri, String selection)
	{
		String selectionTail = (selection != null && !TextUtils.isEmpty(selection)) ? " AND (" + selection + ")" : "";
		String rowId = null;
		
		if (uri.getPathSegments().size() > 1)
			rowId = uri.getPathSegments().get(1);

		String qualifiedColName = null;
		switch (mMatcher.match(uri))
		{
		case CODE_NEWS_ITEM:
			qualifiedColName = nc.addPrefix( NewsContract.COL_ID );
			break;
		case CODE_PAGES_ITEM:
			qualifiedColName = pc.addPrefix( PagesContract.COL_ID );
			break;
		default:
			break;
		}
		
		if (qualifiedColName != null && rowId != null)
		{
			selection = qualifiedColName + "=" + rowId + selectionTail;
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
			Log.e(TAG, "Duplicate link! " + newRowId);
		}
		
		
		Uri newUrl = null;
		
		if (newRowId > -1)
		{
			Log.i(TAG, "Insert " + newRowId);
			newUrl = ContentUris.withAppendedId(uri, newRowId);
			// TODO: check newUrl vs uri
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return newUrl;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		SQLiteDatabase db = getSafeDatabase(uri, false);
		if (db == null) { return null; }
		
		selection = fillSelection(uri, selection);
		
		SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
		
		String groupBy = getGroupBy(uri);
		String having = null;
		String table = getTableForQuery(uri);
		//String limit = null;
		Map<String, String> projectionMap = getProjectionMap( uri );
		
		Log.d(TAG, "selection " + selection);
		Log.d(TAG, "table " + table);
		Cursor newCursor = null;
		
		sqb.setTables(table);
		Log.d(TAG, "setting projection " + projectionMap);
		sqb.setProjectionMap( projectionMap );
		
		/*if (table == PagesJoinNewsContract.TABLE)
		{
			//String[] queries = new String[2];
			
			// buildQueryString (boolean distinct, String tables, String[] columns, String where, String groupBy, String having, String orderBy, String limit) 
			// A1 : buildQuery  (String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit)
			Log.d(TAG, "setting projection");
			sqb.setProjectionMap( pjnc.getProjectionMap() );
			
			//queries[0] = SQLiteQueryBuilder.buildQueryString(false, TABLE, projection, null, null, null, null, null);
			//sqb.setTables(table);
			//queries[1] = sqb.buildQuery(new String[] { COL_COUNT }, selection, selectionArgs, groupBy, having, sortOrder, limit);
			//queries[1] = sqb.buildQuery(new String[] { COL_COUNT }, selection, selectionArgs, null, having, sortOrder, limit);
			//String query = queries[0] + ", (" + queries[1] + ")"; 
			//Log.d(TAG, "Query : " + query);
			//newCursor = db.rawQuery(query, null);
		}
		else
		{
			//Cursor newCursor = db.query(table, projection, selection, selectionArgs, groupBy, having, sortOrder);
		}*/
		
		Log.d(TAG, "try to read a query");
		String queryStr = SQLiteQueryBuilder.buildQueryString(false, table, projection, selection, groupBy, having, sortOrder, null);
		Log.d(TAG, "query: " + queryStr);
		newCursor = sqb.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
		
		if (newCursor != null)
		{
			newCursor.setNotificationUri(getContext().getContentResolver(), uri);
			Log.d(TAG, "newCursorSize " + newCursor.getCount() + " table " + table);
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
		String table = getTable(uri);
		String[] projection = null;
		String colName = null;
		if (table == NewsContract.TABLE)
		{
			projection = new String[] {NewsContract.COL_ID};
			colName = NewsContract.COL_ID;
		}
		else if (table == PagesContract.TABLE)
		{
			projection = new String[] { PagesContract.COL_ID, PagesContract.COL_IMGLINK};
			colName = PagesContract.COL_IMGLINK;
		}
		
		Cursor c = null;
		
		
		if (db == null)
			c = query(uri, projection, selection, selectionArgs, null);
		else
			c = db.query(table, projection, selection, selectionArgs, null, null, null);
		
		if (c == null)
			return;
		
			
		for (c.moveToFirst() ; !c.isAfterLast() ; c.moveToNext())
		{
			String fname =  c.getString( c.getColumnIndex( colName ));
			if (fname == null)
				continue;
			Log.d(TAG, "Delete fname " + fname + " table " + table + " colName " + colName);
			File f = new File(getContext().getCacheDir(), fname);
			if (f.exists()) 
			{
				f.delete();
			}
		}
		c.close();
	}
}
