package com.fcbm.test.multifeedreader.provider;

import java.util.HashMap;
import java.util.Map;

import android.provider.BaseColumns;
import android.util.Log;

public abstract class BaseContract implements BaseColumns {

	private static final String TAG = "BaseContract";
	
	protected static final String COL_TYPE_INTEGER = " integer ";
	protected static final String COL_TYPE_PRIMARY_KEY = " primary key ";
	protected static final String COL_TYPE_AUTOINCREMENT = " autoincrement ";
	protected static final String COL_TYPE_TEXT = " text ";
	protected static final String COL_TYPE_NOT_NULL = " not null ";
	protected static final String COL_TYPE_UNIQUE = " unique ";
	
	public abstract String getTable();
	public abstract String[] getColumns();
	public abstract String[] getQualifiedColumns();
	public abstract Map<String,String> getProjectionMap();
	public abstract ColumnDefinition[] getColumnDefinitions();
	
	public final String dropStatement()
	{
		StringBuilder sb = new StringBuilder();
		String table = getTable(); 
				
		sb.append("DROP TABLE IF EXISTS ");
		sb.append(table);
		
		return sb.toString();
	}	
	
	public final String createStatement()
	{
		StringBuilder sb = new StringBuilder ();
		ColumnDefinition[] definitions = getColumnDefinitions();
		String table = getTable();
		
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(table);
		sb.append(" (");
		
		for (int i = 0; i < definitions.length; i++)
		{
			sb.append( definitions[i] );
			if (i == definitions.length - 1)
			{
				sb.append(")");
			}
			else
				sb.append( ", ");
		}
		
		return sb.toString();
	}
	
	public static final class ColumnDefinition 
	{
		private String mName;
		private String mType;
		
		ColumnDefinition(String name, String type)
		{
			mName = name;
			mType = type;
		}
		
		public String getName() { return mName; }
		public String getType() { return mType; }
		
		@Override
		public String toString()
		{
			return mName + mType;
		}
	}

	public final String addPrefix(String column)
	{
		return getTable() + "." + column;
	}	

	public static final String addPrefix(String table, String column)
	{
		return table + "." + column;
	}
	
	public static final void appendToProjectionMap(Map<String, String> projectionMap, String[] qualifiedCols, String[] cols, String optPrefix)
	{
		if (qualifiedCols.length != cols.length)
		{
			throw new IllegalArgumentException("Number of qualifiedCold must be equal to the number of cols");
		}
/*		
		for (int i = 0; i < qualifiedCols.length; i++)
		{
			if (optPrefix != null)
				projectionMap.put( qualifiedCols[i], qualifiedCols[i] + " as " + optPrefix + cols[i]);
			else
				projectionMap.put( qualifiedCols[i], qualifiedCols[i] + " as " + cols[i]);
		}
*/
		for (int i = 0; i < qualifiedCols.length; i++)
		{
			if (optPrefix != null)
				projectionMap.put( cols[i], qualifiedCols[i] + " as " + optPrefix + cols[i]);
			else
				projectionMap.put( cols[i], qualifiedCols[i] + " as " + cols[i]);
		}
		
	}
	
	protected static final void init(
			String inTable,
			ColumnDefinition[] inColumnDefinitions,
			Map<String, String> outProjectionMap,
			String[] outColumns, 
			String[] outQualifiedColumns)
	{
		Log.d(TAG, "Build for " + inTable + " which has cols " + inColumnDefinitions);
		
		//outColumns = new String[inColumnDefinitions.length];
		//outQualifiedColumns = new String[inColumnDefinitions.length];
		
		for (int i = 0; i < inColumnDefinitions.length; i++)
		{
			outColumns[i] = inColumnDefinitions[i].getName();
			outQualifiedColumns[i] = addPrefix(inTable, outColumns[i]);
		}
		
		Log.d(TAG, "outColumns " + outColumns);
		Log.d(TAG, "outQualifiedColumns " + outQualifiedColumns);
		//outProjectionMap = new HashMap<String, String>(inColumnDefinitions.length);
		appendToProjectionMap(outProjectionMap, outQualifiedColumns, outColumns, null);
		Log.d(TAG, "outProjectionMap " + outProjectionMap);
	}
	
}
