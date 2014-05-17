package com.fcbm.test.multifeedreader.provider;

import java.util.HashMap;
import java.util.Map;

//Retrieve cnt of feeds for each Page (no special cases)
//select pagestable.title, count(newstable.link) from pagestable left join newstable on (pagestable.link=newstable.site) group by pagestable.link;

//Retrieve cnt for special cases
//(select pagestable.title, cnt from (select count(newstable.link) as cnt from newstable), pagestable where pagestable.title='All Feeds');


public final class NewsContract extends BaseContract {

	public static final String TABLE = "newstable";
	
	public static final String COL_ID = BaseContract._ID;
	public static final String COL_SITE = "site";
	public static final String COL_TITLE = "title";
	public static final String COL_DATE ="date";
	public static final String COL_AUTHOR = "author";
	public static final String COL_CATEGORY = "category";
	public static final String COL_DESCRIPTION = "description";
	public static final String COL_LINK = "link";
	public static final String COL_IMGLINK ="imglink";
	public static final String COL_STARRED = "starred";
	public static final String COL_UNREAD = "unread";
	public static final String COL_DATA = "_data";
	
	private static String[] COLUMNS = null;  
	private static String[] QUALIFIED_COLUMNS = null;
	private static Map<String, String> PROJECTION_MAP = null;
	
	public static final ColumnDefinition[] COLUMN_DEFINITIONS =  
		{
			new ColumnDefinition(COL_ID, COL_TYPE_INTEGER + COL_TYPE_PRIMARY_KEY + COL_TYPE_AUTOINCREMENT),
			new ColumnDefinition(COL_SITE, COL_TYPE_TEXT + COL_TYPE_NOT_NULL),
			new ColumnDefinition(COL_TITLE, COL_TYPE_TEXT),
			new ColumnDefinition(COL_DATE, COL_TYPE_INTEGER),
			new ColumnDefinition(COL_AUTHOR, COL_TYPE_TEXT),
			new ColumnDefinition(COL_CATEGORY, COL_TYPE_TEXT),
			new ColumnDefinition(COL_DESCRIPTION, COL_TYPE_TEXT),
			new ColumnDefinition(COL_LINK, COL_TYPE_TEXT + COL_TYPE_NOT_NULL + COL_TYPE_UNIQUE),
			new ColumnDefinition(COL_IMGLINK, COL_TYPE_TEXT),
			new ColumnDefinition(COL_STARRED, COL_TYPE_INTEGER ),
			new ColumnDefinition(COL_UNREAD, COL_TYPE_INTEGER ),
			new ColumnDefinition(COL_DATA, COL_TYPE_INTEGER),
		};
	
	static
	{
		COLUMNS = new String[COLUMN_DEFINITIONS.length];
		QUALIFIED_COLUMNS = new String[COLUMN_DEFINITIONS.length];
		PROJECTION_MAP = new HashMap<String, String>(COLUMN_DEFINITIONS.length);
		
		init(TABLE, COLUMN_DEFINITIONS, PROJECTION_MAP, COLUMNS, QUALIFIED_COLUMNS);
	}
	
	@Override
	public String getTable() {
		return TABLE;
	}

	@Override
	public ColumnDefinition[] getColumnDefinitions() {
		return COLUMN_DEFINITIONS;
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
	public Map<String, String> getProjectionMap() {
		return PROJECTION_MAP;
	}
}
