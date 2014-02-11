package com.fcbm.frenchquiz;

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
import android.text.TextUtils;
import android.util.Log;

public class LanguagesProvider extends ContentProvider {

	private static final String TAG = "LanguagesProvider";
	
	private static final String LANG_DATABASE = "LanguagesProvider.db";
	private static final int LANG_DATABASE_VER = 3;
	
	private static final String GLOSSARY_TABLE = "glossarytable";
	private static final String TRANSLATION_TABLE = "translationtable";
	private static final String RANDOM_TABLE = "randomtable";
	
	public static final String GLOSSARY_COL_ID = "_id";
	public static final String GLOSSARY_COL_WORD = "word";
	public static final String GLOSSARY_COL_WORD_TRANSLATION = "origword";
	public static final String GLOSSARY_COL_LANG = "lang";
	public static final String GLOSSARY_COL_DIFFICULTY = "difficulty";
	public static final String GLOSSARY_COL_COMMENT = "word_comment";
	public static final String GLOSSARY_COL_GRAMMATICAL_NOTE = "grammaticalnote";
	
	public static final String TRANS_COL_ID = "_id";
	// TODO: fix this
	public static final String TRANS_COL_ID_CLIENT = "idtrans";
	public static final String TRANS_COL_T1 = "t1";
	public static final String TRANS_COL_T2 = "t2";
	public static final String TRANS_COL_DIFFICULTY = "difficulty";
	public static final String TRANS_COL_COMMENT = "translation_comment";
	public static final String TRANS_COL_NUMBER_OF_ATTEMTS = "numberoftrial";
	public static final String TRANS_COL_NUMBER_OF_SUCCESS = "numberofsuccess";
	public static final String TRANS_COL_LAST_ATTEMPT = "lastattempt";

	public static final String TRANS_COL_ORIG_WORD = "orig_word";
	public static final String TRANS_COL_ORIG_LANG = "orig_lang";
	public static final String TRANS_COL_DEST_LANG = "dest_lang";
	public static final String TRANS_COL_DEST_WORDS = "dest_words";
	
	
	private static final UriMatcher mMatcher;
	private static final int CODE_GLOSSARY_ITEM = 100;
	private static final int CODE_GLOSSARY_DIR 	= 101;
	private static final int CODE_TRANSLATION_ITEM = 102;
	private static final int CODE_TRANSLATION_DIR 	= 103;
	private static final int CODE_RANDOM_ITEM = 104;
	private static final int CODE_RANDOM_DIR 	= 105;
	
	public static final Uri authorityGlossary = Uri.parse("content://com.fcbm.frenchquiz/" + GLOSSARY_TABLE);
	public static final Uri authorityTranslation = Uri.parse("content://com.fcbm.frenchquiz/" + TRANSLATION_TABLE);
	public static final Uri authorityRandomWord = Uri.parse("content://com.fcbm.frenchquiz/" + RANDOM_TABLE);
	
	static
	{
		mMatcher = new UriMatcher( UriMatcher.NO_MATCH );
		mMatcher.addURI(authorityGlossary.getAuthority(), GLOSSARY_TABLE + "/", CODE_GLOSSARY_DIR);
		mMatcher.addURI(authorityGlossary.getAuthority(), GLOSSARY_TABLE + "/#", CODE_GLOSSARY_ITEM);		
		mMatcher.addURI(authorityGlossary.getAuthority(), TRANSLATION_TABLE + "/", CODE_TRANSLATION_DIR);
		mMatcher.addURI(authorityGlossary.getAuthority(), TRANSLATION_TABLE + "/#", CODE_TRANSLATION_ITEM);		
		mMatcher.addURI(authorityGlossary.getAuthority(), RANDOM_TABLE + "/", CODE_RANDOM_DIR);
		mMatcher.addURI(authorityGlossary.getAuthority(), RANDOM_TABLE + "/#", CODE_RANDOM_ITEM);		

	}
	
	private LanguagesDbHelper mLanguagesDbHelper;
	
	private final class LanguagesDbHelper extends SQLiteOpenHelper {
		
		public LanguagesDbHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		//   select word, case when glossary._id=t1 then t2 else t1 end from glossary,trans where word='donna' and (glossary._id = t1 or glossary._id = t2);
		
		// sqlite> select _id origword, word from glossary, (select word as origword, 
		// (case when glossary._id=t1 then t2 else t1 end) as idtrans from glossary,trans where word='femme' and (glossary._id = t1 or glossary._id = t2)) 
		// where lang='en' and idtrans=glossary._id;
		// femme|wife
		// femme|woman
		
		private final String CREATE_GLOSSARY_TABLE = "CREATE TABLE IF NOT EXISTS " + GLOSSARY_TABLE +" ("
				+ GLOSSARY_COL_ID + " integer primary key autoincrement,"
				+ GLOSSARY_COL_WORD + " text not null,"
				+ GLOSSARY_COL_LANG + " text not null,"
				+ GLOSSARY_COL_DIFFICULTY + "integer,"
				+ GLOSSARY_COL_COMMENT + " text,"
				+ GLOSSARY_COL_GRAMMATICAL_NOTE + " text, "
				+ "CONSTRAINT uq UNIQUE(" + GLOSSARY_COL_WORD + ", " + GLOSSARY_COL_LANG + ")"
				+ ");";
		
		private final String CREATE_TRANSLATION_TABLE = "CREATE TABLE IF NOT EXISTS " + TRANSLATION_TABLE +" ("
				+ TRANS_COL_ID + " integer primary key autoincrement,"
				+ TRANS_COL_T1 + " integer not null,"
				+ TRANS_COL_T2 + " integer not null,"
				+ TRANS_COL_DIFFICULTY + " integer,"
				+ TRANS_COL_COMMENT + " text,"
				+ TRANS_COL_NUMBER_OF_ATTEMTS + " integer,"
				+ TRANS_COL_NUMBER_OF_SUCCESS + " integer,"
				+ TRANS_COL_LAST_ATTEMPT + " integer, "
				+ "UNIQUE(" + TRANS_COL_T1 + ", " + TRANS_COL_T2 + "), CHECK(" + TRANS_COL_T1 + " < " + TRANS_COL_T2 + ")" 
				+ ");";
		
		private final String DROP_GLOSSARY_TABLE = "DROP TABLE IF EXISTS " + GLOSSARY_TABLE;
		private final String DROP_TRANSLATION_TABLE = "DROP TABLE IF EXISTS " + TRANSLATION_TABLE; 
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
			Log.d("TAG", "UpgradeDB");

			// dropFiles(LanguagesProvider.authority, db, null, null);
			db.execSQL(DROP_GLOSSARY_TABLE);
			db.execSQL(DROP_TRANSLATION_TABLE);
			onCreate(db);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL( CREATE_GLOSSARY_TABLE );
			db.execSQL( CREATE_TRANSLATION_TABLE );
		}
	};
	
	@Override
	public boolean onCreate() 
	{
		CursorFactory factory = null;
		mLanguagesDbHelper = new LanguagesDbHelper(getContext() , LANG_DATABASE, factory, LANG_DATABASE_VER);
		return mLanguagesDbHelper != null;
	}
	
	@Override
	public String getType(Uri uri) 
	{
		switch (mMatcher.match(uri))
		{
		case CODE_TRANSLATION_DIR:
		case CODE_GLOSSARY_DIR:
		case CODE_RANDOM_DIR:
			return "vnd.android.cursor.dir/vnd.languagesprovider.dir";
		case CODE_TRANSLATION_ITEM:
		case CODE_GLOSSARY_ITEM:
		case CODE_RANDOM_ITEM:			
			return "vnd.android.cursor.item/vnd.languagesprovider.item";
		}
		return null;
	}	
	
	private String getTable(Uri uri)
	{
		switch (mMatcher.match(uri))
		{
		case CODE_GLOSSARY_ITEM:
		case CODE_GLOSSARY_DIR:
			return GLOSSARY_TABLE;
		case CODE_TRANSLATION_ITEM:
		case CODE_TRANSLATION_DIR:
			return TRANSLATION_TABLE;
		case CODE_RANDOM_DIR:
		case CODE_RANDOM_ITEM:
			return RANDOM_TABLE;
		}
		return "";
	}
	
	private SQLiteDatabase getSafeDatabase(Uri uri, boolean writable)
	{
		SQLiteDatabase db = null;
		try
		{
			if (writable)
				db = mLanguagesDbHelper.getWritableDatabase();
			else
				db = mLanguagesDbHelper.getReadableDatabase();
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
		case CODE_GLOSSARY_ITEM:
		case CODE_TRANSLATION_ITEM:
		case CODE_RANDOM_ITEM:
			String rowId = uri.getPathSegments().get(1);
			String selectionTail = (selection != null && !TextUtils.isEmpty(selection)) ? " AND (" + selection + ")" : ""; 
			selection = TRANS_COL_ID + "=" + rowId + selectionTail;
			break;
		default:
			break;
		}
		
		return selection;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		SQLiteDatabase db = getSafeDatabase(uri, true);
		if (db == null) { return 0; }

		selection = fillSelection(uri, selection);

		// TODO: do query/delete in TRANSLATION_TABLE
		int deletedItems = db.delete( getTable(uri), selection, selectionArgs);
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return deletedItems;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		SQLiteDatabase db = getSafeDatabase(uri, true);
		if (db == null) { return null; }
		
		String nullColumnHack = null;
		
		long newRowId = -1;
		long destinationIds[] = null;
			
		if (values != null && values.containsKey( TRANS_COL_DEST_LANG ) && values.containsKey( TRANS_COL_DEST_WORDS))
		{
			String[] translationWords = values.getAsString( TRANS_COL_DEST_WORDS ).split( ",");
			
			//translationValues = new ContentValues[translationWords.length];
			destinationIds = new long[translationWords.length];
			
			String destinationLang = values.getAsString( TRANS_COL_DEST_LANG );
			int i = 0;
			for (String word : translationWords)
			{
				ContentValues cv = new ContentValues();
				cv.put( GLOSSARY_COL_WORD, word);
				cv.put( GLOSSARY_COL_LANG, destinationLang);
				long tmpId = -1;
				try
				{
					tmpId = db.insertOrThrow( GLOSSARY_TABLE, nullColumnHack, cv);
				} catch (SQLiteConstraintException e)
				{
					Log.e(TAG, "Duplicate word : " + word + " lang: " + destinationLang);
					tmpId = getWordId(word, destinationLang);
				}
				if (tmpId > -1)
				{
					destinationIds[i] = tmpId;
					++i;
				}
			}
		}
		
		long origRowId = -1;
		try
		{
			ContentValues cv = new ContentValues();
			String word = values.getAsString( GLOSSARY_COL_WORD); 
			String lang = values.getAsString( GLOSSARY_COL_LANG);

			cv.put( GLOSSARY_COL_WORD, word);
			cv.put( GLOSSARY_COL_LANG, lang);
			
			newRowId = db.insertOrThrow( GLOSSARY_TABLE, nullColumnHack, cv);
			origRowId = newRowId;
		} catch (SQLiteConstraintException e)
		{
			if (values.containsKey( GLOSSARY_COL_WORD) && values.containsKey( GLOSSARY_COL_LANG ))
			{
				String word = values.getAsString( GLOSSARY_COL_WORD); 
				String lang = values.getAsString( GLOSSARY_COL_LANG);
				origRowId = getWordId(word, lang);
			}
			Log.e(TAG, "Duplicate link!");
		}
		
		if (origRowId > 0 && destinationIds != null)
		{
			for (int i = 0; i < destinationIds.length ; i++)
			{
				ContentValues cv = new ContentValues();
				cv.put( TRANS_COL_T1 , Math.min(origRowId, destinationIds[i]));
				cv.put( TRANS_COL_T2 , Math.max(origRowId, destinationIds[i]));
				try
				{
					db.insertOrThrow( TRANSLATION_TABLE, nullColumnHack, cv);
				} catch (SQLiteConstraintException e) { Log.e(TAG, "Duplicate translation!"); }
			}
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
	
	private long getWordId(String word, String lang)
	{
		long id = -1;
		Cursor c = query( 
				authorityGlossary, 
				new String[] {GLOSSARY_COL_ID}, 
				new String(GLOSSARY_COL_WORD + "='" + word + "' AND " + GLOSSARY_COL_LANG + " ='" + lang + "'"), 
				null, null);
		
		if (c != null)
		{
			c.moveToFirst();
			Log.d(TAG, "Cursor size " + c.getColumnCount() );
			id = c.getLong( c.getColumnIndex( GLOSSARY_COL_ID ));
			c.close();
		}
		return id;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) 
	{
		SQLiteDatabase db = getSafeDatabase(uri, false);
		if (db == null) { return null; }
		
		selection = fillSelection(uri, selection);
		Log.d(TAG, "Selection " + selection);
		Cursor newCursor = null;

		String groupBy = null;
		String having = null;

		if (getTable( uri) == GLOSSARY_TABLE)
		{
			Log.i(TAG, "Query on glossary");
			newCursor = db.query( GLOSSARY_TABLE, projection, selection, selectionArgs, groupBy, having, sortOrder);
		}
		else if (getTable( uri) == TRANSLATION_TABLE)
		{
			Log.i(TAG, "Query on translation");
			String selectTranslation =
				"SELECT " +
					GLOSSARY_COL_ID + ", " + 
					"origword, " + 
					GLOSSARY_COL_WORD + 
				" FROM " + 
					GLOSSARY_TABLE + ", (" + 
					"SELECT " + 
						GLOSSARY_COL_WORD + " AS origword, " + 
						"(CASE WHEN " + GLOSSARY_TABLE+"."+ GLOSSARY_COL_ID+"=" + TRANS_COL_T1 + " THEN " + TRANS_COL_T2 + " ELSE " + TRANS_COL_T1 + " END) AS idtrans " + 
					"FROM " + 
						GLOSSARY_TABLE + ", " + 
						TRANSLATION_TABLE + 
					" WHERE " + 
						GLOSSARY_COL_WORD + "='" + selectionArgs[0]+"' AND " + 
						GLOSSARY_COL_LANG + "='" + selectionArgs[1]+"' AND " +
						"(" + GLOSSARY_TABLE+"."+ GLOSSARY_COL_ID+" = " + TRANS_COL_T1 + " or " + GLOSSARY_TABLE+"."+ GLOSSARY_COL_ID+" = " + TRANS_COL_T2 + "))" +
				"WHERE " 
						//+ GLOSSARY_COL_LANG + "=? AND " 
						+ "idtrans=" + GLOSSARY_TABLE+"."+ GLOSSARY_COL_ID+";";
		
			newCursor = db.rawQuery(selectTranslation, null);
			Log.d(TAG, "NewCursor is null : " + (newCursor != null && newCursor.getCount() > 0));
			
		}
		else if (getTable( uri) == RANDOM_TABLE)
		{
			// TODO : handle randomness in code
			Log.i(TAG, "Query on random table");
			String selectTranslation =
				"SELECT * FROM (SELECT * FROM (SELECT " +
					GLOSSARY_TABLE + "." + GLOSSARY_COL_ID + ", " + 
					TRANS_COL_ID_CLIENT + ", " +
					TRANS_COL_NUMBER_OF_ATTEMTS + ", " +
					TRANS_COL_NUMBER_OF_SUCCESS + ", " +
					"origword, " + 
					GLOSSARY_COL_WORD + 
				" FROM " + 
					GLOSSARY_TABLE + ", (" + 
					"SELECT " + 
						TRANS_COL_NUMBER_OF_ATTEMTS + ", " +
						TRANS_COL_NUMBER_OF_SUCCESS + ", " +
						GLOSSARY_COL_WORD + " AS origword, " + 
						"(CASE WHEN " + GLOSSARY_TABLE+"."+ GLOSSARY_COL_ID+"=" + TRANS_COL_T1 + " THEN " + TRANS_COL_T2 + " ELSE " + TRANS_COL_T1 + " END) AS idtrans " + 
					"FROM " + 
						GLOSSARY_TABLE + ", " + 
						TRANSLATION_TABLE + 
					" WHERE " + 
					//	GLOSSARY_COL_WORD + "='" + selectionArgs[0]+"' AND " + 
						GLOSSARY_COL_LANG + "='" + selectionArgs[0] + "' AND " +
						"(" + GLOSSARY_TABLE+"."+ GLOSSARY_COL_ID+" = " + TRANS_COL_T1 + " or " + GLOSSARY_TABLE+"."+ GLOSSARY_COL_ID+" = " + TRANS_COL_T2 + ")" +
						" ORDER BY RANDOM())"+ 
				" WHERE " +
						GLOSSARY_COL_LANG + "='"+ selectionArgs[2]+"' AND " + 
						"idtrans=" + GLOSSARY_TABLE+"."+ GLOSSARY_COL_ID +
						 
						" ORDER BY RANDOM() LIMIT " + selectionArgs[1] + 
						") GROUP BY origword ) GROUP BY " +GLOSSARY_COL_WORD + ";" ;
		
			newCursor = db.rawQuery(selectTranslation, null);
			Log.d(TAG, "NewCursor is null : " + (newCursor != null && newCursor.getCount() > 0));
			// select name, surname from (select id, name from nm group by name) as a, (select id, surname from nm group by surname) as b where a.id = b.id;
		}		
		else
		{
			Log.i(TAG, "Query on something wrong");
		}
		
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
		String table = getTable(uri);
		int updatedRows = db.update( table, values, selection, selectionArgs);
		Log.d(TAG, "Update on " + table + " res: " + updatedRows );
		if (updatedRows > 0)
		{
			getContext().getContentResolver().notifyChange(uri, null);
		}
		
		return updatedRows;
	}
}
