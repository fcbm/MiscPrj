package com.fcbm.test.multifeedreader.provider;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class PagesJoinNewsContract extends BaseContract {

	private static final String TAG = "PagesJoinNewsContract";
	
	private static final BaseContract pc = new PagesContract();
	private static final BaseContract nc = new NewsContract();

	/*
    Retrieve cnt of feeds for each Page and special cases
    
	SELECT pagestable.title, 
		(CASE // TODO: define this case into a projection map or column name
			WHEN (pagestable.title='All Feeds') 
				THEN (select count(newstable.link) from newstable) 
			WHEN (pagestable.title='Starred') 
				THEN (select count(newstable.link) from newstable where newstable.starred=true) 
			WHEN (pagestable.title='Unread') 
				THEN (select count(newstable.link) from newstable where newstable.unread=true) 
			ELSE 
				count(newstable.link) 
		END) as CNT 
	FROM pagestable LEFT JOIN newstable ON (pagestable.link=newstable.site) 
	GROUP BY pagestable.link; // Need to group by pagestable.link as it's the valua granted not to be not null			
*/	
	
	public static final String TABLE = 
			PagesContract.TABLE + " LEFT JOIN " + NewsContract.TABLE 
			+ "  ON ( " + pc.addPrefix(PagesContract.COL_LINK) + "=" + nc.addPrefix(NewsContract.COL_SITE) + ") ";

	private static final String CNT_LINKS = "COUNT( " + nc.addPrefix( NewsContract.COL_LINK) + ")";
	private static final String SELECT_CNT_ALL_NEWS_LINKS = "SELECT " + CNT_LINKS + " from " + NewsContract.TABLE;
	private static final String SELECT_CNT_STARRED_NEWS_LINKS = SELECT_CNT_ALL_NEWS_LINKS + " WHERE " + nc.addPrefix(NewsContract.COL_STARRED) + "=1";
	private static final String SELECT_CNT_UNREAD_NEWS_LINKS = SELECT_CNT_ALL_NEWS_LINKS + " WHERE " + nc.addPrefix(NewsContract.COL_UNREAD) + " is null";
	
	public static final String COL_COUNT_CNT = "cnt";
	private static final String COL_COUNT_NEWS_AS_CNT = 
		"(CASE "+ 
		"	WHEN (" + pc.addPrefix(PagesContract.COL_TITLE ) + "=\'" + PagesContract.COL_TITLE_ALL + "\')" + 
		"		THEN ("+ SELECT_CNT_ALL_NEWS_LINKS+ ")" + 
		"	WHEN (" + pc.addPrefix(PagesContract.COL_TITLE ) + "=\'" + PagesContract.COL_TITLE_STARRED + "\')" +
		"		THEN ("+ SELECT_CNT_STARRED_NEWS_LINKS+ ")" + 
		"	WHEN (" + pc.addPrefix(PagesContract.COL_TITLE ) + "=\'" + PagesContract.COL_TITLE_UNREAD + "\')" +
		"		THEN ("+ SELECT_CNT_UNREAD_NEWS_LINKS+ ")" + 
		"	ELSE " +
		"		" + CNT_LINKS +  
		"END) as cnt ";
	
	private static Map<String, String> PROJECTION_MAP = null;
	
	
	static
	{
		PROJECTION_MAP = new HashMap<String, String>(pc.getColumns().length + nc.getColumns().length);
		appendToProjectionMap(PROJECTION_MAP, pc.getQualifiedColumns(), pc.getColumns(), null);
		//appendToProjectionMap(PROJECTION_MAP, nc.getQualifiedColumns(), nc.getColumns(), "_");
		PROJECTION_MAP.put(COL_COUNT_CNT, COL_COUNT_NEWS_AS_CNT);

		Log.d(TAG, "join PROJECTION_MAP " + PROJECTION_MAP);
	}
	
	@Override
	public String getTable() {
		return TABLE;
	}

	@Override
	public String[] getColumns() {
		return null;
	}

	@Override
	public String[] getQualifiedColumns() {
		return (String[]) PROJECTION_MAP.keySet().toArray();
	}

	@Override
	public ColumnDefinition[] getColumnDefinitions() {
		return null;
	}

	@Override
	public Map<String, String> getProjectionMap() {
		return PROJECTION_MAP;
	}

}
