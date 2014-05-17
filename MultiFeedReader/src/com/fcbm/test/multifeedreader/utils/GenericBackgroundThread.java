package com.fcbm.test.multifeedreader.utils;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fcbm.test.multifeedreader.bom.FeedItem;
import com.fcbm.test.multifeedreader.provider.NewsProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


public class GenericBackgroundThread<Token> extends HandlerThread {
	
	private static final String TAG = "GenericBackgroundThread";
	
	public static final int DOWNLOAD_BITMAP = 100;
	public static final int DOWNLOADED_BITMAP = 101;
	public static final int CLEAR_FILES = 100;
	
	public static final String  DOWNLOADED_BITMAP_FNAME = "downloaded_fname";
	
	
	private final Map<Token, String> mQueue = Collections.synchronizedMap(new HashMap<Token, String>());
	
	private Handler mHandler;
	private Handler mResponseHandler;
	private Context mApplicationContext;
	
	public GenericBackgroundThread(Context applicationContext, Handler responseHandler)
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
	
	public void queueDeleteFiles(String[] fileNames)
	{
		mHandler.obtainMessage(CLEAR_FILES, fileNames);
	}
	
	public void clearQueue()
	{
		mQueue.clear();
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
					
					String fname = downloadBitmapIfNotExists(url);
					
					mQueue.remove( msg.obj );
					
					if (fname == null) 
						return;
					
					Message responseMsg = mResponseHandler.obtainMessage(DOWNLOADED_BITMAP, msg.obj);
					Bundle data = new Bundle();
					data.putString(DOWNLOADED_BITMAP_FNAME, fname);
					responseMsg.setData( data );
					responseMsg.sendToTarget();
				}
				else if (msg.what == CLEAR_FILES)
				{
					String[] fileList = (String[])msg.obj;
					clearOldFiles(mApplicationContext, fileList);
				}
				
			}
		};
	}
	
	private String downloadBitmapIfNotExists(String url)
	{
		byte[] buffer = null;
		String fname = null;
		
		try {
			fname = NewsProviderUtils.getFilenameOfNewsImgLink(mApplicationContext, url);
			
			if (fname == null)
			{
				return fname;
			}
			Log.d(TAG, "look for " + fname);
			
			File file = new File(mApplicationContext.getCacheDir(), fname);
			
			if(file.exists())
			{
				Log.d(TAG, "file " + fname + "already exists");
				return fname;
			}
			
			Log.d(TAG, "file " + fname + " does not exists");
			
			
			buffer = HttpDownloader.getUrlAsBytes(url);
			
			if (buffer == null)
			{
				Log.d(TAG, "Failed to download url " + url);
				return null;
			}
			
			NewsProviderUtils.storeFileIndexedByProvider(
					mApplicationContext, 
					NewsProvider.authorityNews, 
					Integer.parseInt(fname), 
					buffer);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return fname;
	}
	
	private void clearOldFiles(Context ctx, String[] fnames)
	{
		String[] fileList = ctx.fileList();
		int fListSizeBefore = fileList.length;
		int fListSizeAfter = fileList.length;

		for (String s : fnames) {
			File f = new File(ctx.getCacheDir(), s);
			
			if (f.exists()) {
				ctx.deleteFile(f.getName());
			}
		}

		fListSizeAfter = ctx.fileList().length;
		
		Toast.makeText(ctx, 
			"FileListBefore " + fListSizeBefore + "FileListAfter " + fListSizeAfter,
			Toast.LENGTH_LONG).show();
	}	
	
	public static void clearOldFiles(Context ctx, List<FeedItem> items)
	{
		String[] fileList = ctx.fileList();
		int fListSizeBefore = fileList.length;
		int fListSizeAfter = fileList.length;


		for (FeedItem i : items) {
			if (i.getImageLink() == null) {
				continue;
			}
			
			String name = i.getImageLinkAsFileName();
			File f = new File(ctx.getCacheDir(), name);
			
			if (f.exists()) {
				ctx.deleteFile(f.getName());
			}
		}

		/*lastModDate = new Date(f.lastModified());
			
		int diffInDays = (int)( (currentDate.getTime() - lastModDate.getTime()) / (1000 * 60 * 60 * 24) );
		Log.d(TAG+"m", lastModDate.toString());
		if (f.exists() && diffInDays >= 1)
		{
		}*/

		fListSizeAfter = ctx.fileList().length;
		
		Toast.makeText(ctx, 
			"FileListBefore " + fListSizeBefore + "FileListAfter " + fListSizeAfter,
			Toast.LENGTH_LONG).show();
	}	
}
