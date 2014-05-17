package com.fcbm.test.multifeedreader.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.fcbm.test.multifeedreader.bom.FeedItem;
import com.fcbm.test.multifeedreader.bom.PageInfo;
import com.fcbm.test.multifeedreader.provider.NewsContract;
import com.fcbm.test.multifeedreader.provider.NewsProvider;
import com.fcbm.test.multifeedreader.provider.PagesContract;

public class NewsProviderUtils {

	private static final String TAG = NewsProviderUtils.class.getName(); 
	
	public static String getFilenameOfNewsImgLink(Context ctx, String url)
	{
		String[] projection = new String[] {NewsContract.COL_ID, NewsContract.COL_DATA};
		String selection = NewsContract.COL_IMGLINK + "=\'" + url + "\'";
		
		Log.d(TAG, "Get Filename for ImgLink: " + url);
		Cursor c = ctx.getContentResolver().query(NewsProvider.authorityNews, projection, selection, null, null);
		
		String fname = null;
		int rowId = -1;
		
		if (c == null)
		{
			return null;
		}

		if (c.getCount() > 0)
		{
			c.moveToFirst();
			rowId = c.getInt( c.getColumnIndex( NewsContract.COL_ID));

			if( rowId > -1)
			{
				fname = ""+rowId;
			}
		}
		c.close();
		
		return fname;
	}
	
	public static void storeFileIndexedByProvider(Context ctx, Uri auth, int id, byte[] fileContent)
	{
		ParcelFileDescriptor fd = null;
		FileOutputStream outFile = null;
		
		try {
			
			fd = ctx.getContentResolver().openFileDescriptor(ContentUris.withAppendedId(auth, id) , "w");
			outFile = new FileOutputStream(fd.getFileDescriptor());
			outFile.write( fileContent, 0, fileContent.length);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (outFile != null)
		{
			try {
				outFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (fd != null)
		{
			try {
				fd.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public static int storeNewsInfo(Context ctx, List<FeedItem> items, String pageInfoUrl)
	{
		int retVal = -1;
		
		ContentValues[] values = new ContentValues[items.size()];
		
		for (int i = 0; i < values.length; i++)
		{
			ContentValues cv = new ContentValues();
			FeedItem item = items.get(i);
			
			cv.put(NewsContract.COL_TITLE, item.getTitle());
			cv.put(NewsContract.COL_SITE, pageInfoUrl);
			cv.put(NewsContract.COL_DATE, item.getLongDate());
			cv.put(NewsContract.COL_LINK, item.getLink());
			cv.put(NewsContract.COL_IMGLINK, item.getImageLink());
			cv.put(NewsContract.COL_CATEGORY, item.getCategory());
			cv.put(NewsContract.COL_AUTHOR, item.getAuthor());
			cv.put(NewsContract.COL_DESCRIPTION, item.getDescription());
			values[i] = cv;
		}
		
		retVal = ctx.getContentResolver().bulkInsert(NewsProvider.authorityNews, values);
		return retVal;
	}

	public static int updatePageInfo(Context ctx, PageInfo pageInfo)
	{
		ContentValues pageValues = null;
		int pagesUpdated = 0;
		
		Log.i(TAG, "PageUrl " 			+ pageInfo.getUrl());
		Log.i(TAG, "PageFaviconUrl " 	+ pageInfo.getFaviconUrl());
		Log.i(TAG, "PageDescription " 	+ pageInfo.getDescription());
		
		pageValues = new ContentValues();
		pageValues.put( PagesContract.COL_IMGLINK, pageInfo.getFaviconUrl());
		pageValues.put( PagesContract.COL_DESCRIPTION, pageInfo.getDescription());
		String where = PagesContract.COL_LINK+ "=\'" + pageInfo.getUrl()+"\'";
		Log.i(TAG, "where " + where);

		pagesUpdated = ctx.getContentResolver().update( NewsProvider.authorityPages, pageValues, where, null);

		Log.i(TAG, "items size : " + pageInfo.getItems().size() + " pagesUpdated " + pagesUpdated);		
		return pagesUpdated;
	}

	public static int storePageInfo(Context ctx, List<PageInfo> pageInfo)
	{
		ContentValues[] listOfValues = new ContentValues[pageInfo.size()];
		for(int i = 0; i < pageInfo.size(); i++)
		{
			PageInfo info = pageInfo.get(i);
			ContentValues cv = new ContentValues();
			cv.put(PagesContract.COL_LINK, info.getUrl());
			cv.put(PagesContract.COL_TITLE, info.getTitle());
			cv.put(PagesContract.COL_DESCRIPTION, info.getDescription());
			Log.d(TAG, "inserting Items " + info.getUrl());
			listOfValues[i] = cv ;
		}
		int inserted = ctx.getContentResolver().bulkInsert( NewsProvider.authorityPagesAndCountedNews, listOfValues);
		Log.d(TAG, "inserted Items " + inserted);
		return inserted;
	}
	
}
