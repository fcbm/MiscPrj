package com.fcbm.test.multifeedreader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fcbm.test.multifeedreader.bom.FeedItem;
import com.fcbm.test.multifeedreader.provider.NewsContract;
import com.fcbm.test.multifeedreader.provider.NewsProvider;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;


public class GenericDownloader<Token> extends HandlerThread {
	
	private static final String TAG = "GenericDownloader";
	private static final int DOWNLOAD_BITMAP = 100;
	public static final int DOWNLOADED_BITMAP = 101;
	public static final String  DOWNLOADED_BITMAP_FNAME = "downloaded_fname";
	
	
	private final Map<Token, String> mQueue = Collections.synchronizedMap(new HashMap<Token, String>());
	private Handler mHandler;
	private Handler mResponseHandler;
	private Context mApplicationContext;
	
	public GenericDownloader(Context applicationContext, Handler responseHandler)
	{
		super(TAG);
		mResponseHandler = responseHandler;
		mApplicationContext = applicationContext;
	}
	
	public void queueDownloadBitmap(Token token, String url)
	{
		mQueue.put(token, url);
		mHandler.obtainMessage(DOWNLOAD_BITMAP, token).sendToTarget();
	}
	
	@Override
	@SuppressLint("HandlerLeak")
	protected void onLooperPrepared()
	{
		super.onLooperPrepared();
		
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if (msg.what == DOWNLOAD_BITMAP)
				{
					String url = mQueue.get( msg.obj );
					
					if (url == null) 
						return;
					
					String fname = downloadBitmapFromCr(url);
					
					mQueue.remove( msg.obj );
					
					if (fname == null) 
						return;
					
					Message responseMsg = mResponseHandler.obtainMessage(DOWNLOADED_BITMAP, msg.obj);
					Bundle data = new Bundle();
					data.putString(DOWNLOADED_BITMAP_FNAME, fname);
					responseMsg.setData( data );
					responseMsg.sendToTarget();
				}
			}
		};
	}
	
	public void clearQueue()
	{
		mQueue.clear();
	}
	
	/*
	private String downloadBitmap(String url)
	{
		byte[] buffer = null;
		FileOutputStream outFile = null;
		String retStr = null;
		
		try {
			String fname = url;
			fname = fname.replaceAll("[^a-zA-Z0-9]", "_");

			File file = new File(mApplicationContext.getFilesDir(), fname);
			if (file.exists())
			{
				return fname;
			}
			
			buffer = FeedFetch.getUrlAsBytes(url);
			
			if (buffer == null) return null;
			
			outFile = mApplicationContext.openFileOutput(fname, Context.MODE_PRIVATE);
			outFile.write( buffer, 0, buffer.length);
			
			retStr = fname;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
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
		
		return retStr;
	}*/

	private String downloadBitmapFromCr(String url)
	{
		byte[] buffer = null;
		ParcelFileDescriptor fd = null;
		FileOutputStream outFile = null;
		String retStr = null;
		
		try {
			
			String[] projection = new String[] {NewsContract.COL_ID, NewsContract.COL_DATA};
			String selection = NewsContract.COL_IMGLINK + "=\'" + url + "\'";
			Cursor c = mApplicationContext.getContentResolver().query(NewsProvider.authorityNews, projection, selection, null, null);
			
			String fname = null;
			int rowId = -1;
			if (c != null && c.getCount() > 0)
			{
				c.moveToFirst();
				rowId = c.getInt( c.getColumnIndex( NewsContract.COL_ID));
				if( rowId > -1)
					fname = ""+rowId;
				//rowId = c.getInt( c.getColumnIndex( NewsProvider.NEWS_COL_ID ));
				c.close();
			}
			
			if (fname != null)
			{
			File file = new File(mApplicationContext.getCacheDir(), fname);
			Log.d("fe1", "look for " + file);
			Log.d("fe1", "look for imglink " + url);
			 if( file.exists())
			{
				Log.d("fe1", "fileExists");
				return fname;
			}
			 Log.d("fe1", "fileNotExists");
			}
			else 
				return null;
			
			buffer = FeedFetch.getUrlAsBytes(url);
			
			if (buffer == null) return null;
			
			fd = mApplicationContext.getContentResolver().openFileDescriptor(ContentUris.withAppendedId(NewsProvider.authorityNews, rowId) , "w");
			outFile = new FileOutputStream(fd.getFileDescriptor());
			outFile.write( buffer, 0, buffer.length);
			
			retStr = fname;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
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
		return retStr;
	}
	
	
	public static void clearOldFiles(Context ctx, List<FeedItem> items)
	{
		String[] fileList = ctx.fileList();
		int fListSizeBefore = fileList.length;
		int fListSizeAfter = fileList.length;
		
		
		//Date currentDate = new Date();
		//Date lastModDate = new Date();
		int failures = 0;
		int foundCnt = 0;
		for (String fname : fileList) {
			File f = new File(fname);
			boolean found = false;
			Log.d(TAG + "f", f.getName());
			for (FeedItem i : items) {
				if (i.getImageLink() != null) {
					String name = i.getImageLink().replaceAll("[^a-zA-Z0-9]", "_");
					if (name.equals(f.getName())) {
						found = true;
						foundCnt++;
					}
				}
			}

			if (!found) {
				if (!ctx.deleteFile(f.getName())) {
					failures++;
				}
			}

			/*lastModDate = new Date(f.lastModified());
			
			int diffInDays = (int)( (currentDate.getTime() - lastModDate.getTime()) / (1000 * 60 * 60 * 24) );
			Log.d(TAG+"m", lastModDate.toString());
			if (f.exists() && diffInDays >= 1)
			{
			}*/
			
		}

		fListSizeAfter = ctx.fileList().length;
		
		Toast.makeText(ctx, "FileListBefore " + fListSizeBefore + "FileListAfter " + fListSizeAfter
				+ "\nfailures " + failures
				+ "\nfoundCnt " + foundCnt, 
				Toast.LENGTH_LONG).show();
	}	
}
