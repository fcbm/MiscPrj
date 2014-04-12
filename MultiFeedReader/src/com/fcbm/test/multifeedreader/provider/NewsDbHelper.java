package com.fcbm.test.multifeedreader.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public final class NewsDbHelper extends SQLiteOpenHelper {
	
	private Context mContext;
	
	public NewsDbHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		NewsContract nc = new NewsContract();
		PagesContract pc = new PagesContract();
			
		//mContext.getContentResolver().delete(NewsProvider.authorityNews, null, null);
		db.execSQL(nc.dropStatement());

		//mContext.getContentResolver().delete(NewsProvider.authorityPages, null, null);
		db.execSQL(pc.dropStatement());
			
		db.execSQL( nc.createStatement() );
		db.execSQL( pc.createStatement() );
	}
		
	@Override
	public void onCreate(SQLiteDatabase db) {
		NewsContract nc = new NewsContract();
		PagesContract pc = new PagesContract();

		db.execSQL( nc.createStatement() );
		db.execSQL( pc.createStatement() );
	}
}
