package com.fcbm.test.multifeedreader.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpDownloader {

	private static final String TAG = "FeedFetch";
	
	public static String getUrlAsString(String urlSpec) throws IOException
	{
		return new String(getUrlAsBytes(urlSpec));
	}
	
	public static byte[] getUrlAsBytes(String urlString) throws IOException
	{
		URL url = new URL(urlString);
		
		Log.d(TAG, "connecting to " + url.toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
		connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestProperty("User-agent", "Mozilla AppleWebKit Chrome Safari"); // some feeds need this to work properly
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setUseCaches(false);
        connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        
        connection.connect();
		
		Log.d(TAG, "connected!");
		
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream is = connection.getInputStream();
			Log.d(TAG, "got InputStream");
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
			{
				return null;
			}
			
			int byteRead = 0;
			
			byte[] buffer = new byte[1024];
			
			while ((byteRead = is.read(buffer)) > 0)
			{
				Log.d(TAG, "reading..");
				out.write(buffer, 0, byteRead);
			}
			is.close();
			out.close();
			return out.toByteArray();
		} finally {
			connection.disconnect();
		}
	}
	
	
}
