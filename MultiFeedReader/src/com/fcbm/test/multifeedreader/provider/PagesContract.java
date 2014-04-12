package com.fcbm.test.multifeedreader.provider;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class PagesContract extends BaseContract {

	private static final String TAG = "PagesContract";
	
	public static final String TABLE = "pagestable";
	
	// TODO: find the way to read these 3 values from resources
	public static final String COL_TITLE_ALL = "All Feeds";
	public static final String COL_TITLE_STARRED = "Starred";
	public static final String COL_TITLE_UNREAD = "Unread";
	
	public static final String COL_ID = BaseContract._ID;
	public static final String COL_TITLE = "title";
	public static final String COL_DATE = "date";
	public static final String COL_DESCRIPTION = "description";
	public static final String COL_LINK = "link";
	public static final String COL_IMGLINK = "imglink";
	public static final String COL_DATA = "_data";	

	private static String[] COLUMNS = null;  
	private static String[] QUALIFIED_COLUMNS = null;
	private static Map<String, String> PROJECTION_MAP = null;
	
	private static final ColumnDefinition[] COLUMN_DEFINITIONS = 
	{
		new ColumnDefinition(COL_ID, COL_TYPE_INTEGER + COL_TYPE_PRIMARY_KEY + COL_TYPE_AUTOINCREMENT),
		new ColumnDefinition(COL_TITLE, COL_TYPE_TEXT),
		new ColumnDefinition(COL_DATE, COL_TYPE_INTEGER),
		new ColumnDefinition(COL_DESCRIPTION, COL_TYPE_TEXT),
		new ColumnDefinition(COL_LINK, COL_TYPE_TEXT + COL_TYPE_NOT_NULL + COL_TYPE_UNIQUE),
		new ColumnDefinition(COL_IMGLINK, COL_TYPE_TEXT),
		new ColumnDefinition(COL_DATA, COL_TYPE_INTEGER),
	};
	
	static
	{
		Log.d(TAG, "init pages");
		COLUMNS = new String[COLUMN_DEFINITIONS.length];
		QUALIFIED_COLUMNS = new String[COLUMN_DEFINITIONS.length];
		PROJECTION_MAP = new HashMap<String, String>(COLUMN_DEFINITIONS.length);
		
		init(TABLE, COLUMN_DEFINITIONS, PROJECTION_MAP, COLUMNS, QUALIFIED_COLUMNS);
		Log.d(TAG, "pages columns : " + COLUMNS.length);
	}
	
	@Override
	public String getTable() {
		return TABLE;
	}

	@Override
	public String[] getColumns() {
		return COLUMNS;
	}

	@Override
	public String[] getQualifiedColumns() {
		return QUALIFIED_COLUMNS;
	}

	@Override
	public ColumnDefinition[] getColumnDefinitions() {
		return COLUMN_DEFINITIONS;
	}

	@Override
	public Map<String, String> getProjectionMap() {
		return PROJECTION_MAP;
	}

}
