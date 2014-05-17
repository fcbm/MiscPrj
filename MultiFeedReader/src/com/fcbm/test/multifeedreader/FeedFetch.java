package com.fcbm.test.multifeedreader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.fcbm.test.multifeedreader.bom.FeedItem;
import com.fcbm.test.multifeedreader.bom.PageInfo;
import com.fcbm.test.multifeedreader.provider.NewsContract;
import com.fcbm.test.multifeedreader.provider.NewsProvider;
import com.fcbm.test.multifeedreader.provider.PagesContract;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


public class FeedFetch {

	private static final String TAG = "FeedFetch";
	private static final String STR_FAVICON = "/favicon.ico";
	private static final String XML_ITEM = "item";
	
	private static String getUrlAsString(String urlSpec) throws IOException
	{
		return new String(getUrlAsBytes(urlSpec));
	}
	
	public static int downloadFeedItems(Context ctx)
	{
		int retVal = 0;
		
		Cursor c = ctx.getContentResolver().query( NewsProvider.authorityPages, null, null, null, null);
		
		if (c == null)
			return retVal;
		
		Log.d(TAG, "Count " + c.getCount());
		
		for (c.moveToFirst(); !c.isAfterLast() ; c.moveToNext())
		{
			PageInfo pi = new PageInfo( c );
			String url = pi.getUrl();
			String title = pi.getTitle(); 
			if (url.startsWith("http://") || url.startsWith("https://"))
			{
				Log.d(TAG, "Url " + url + " title " + title);
				retVal += downloadFeedItems( ctx , pi);
			}
		}
		
		c.close();
		return retVal;
	}
	
	public static int downloadFeedItems(Context ctx, PageInfo pageInfo)
	{
		int retVal = 0;
		
		try
		{
			String urlStr = Uri.parse(pageInfo.getUrl()).buildUpon().build().toString();
			
			Log.i(TAG, "URL STR input -" + urlStr + "-");
			String xmlString = getUrlAsString(urlStr);
			
			if (xmlString == null)
				return retVal;
			
			Log.i(TAG, "XML String = " + xmlString);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			
			parser.setInput( new StringReader( xmlString) );
			
			parseItems(pageInfo, parser);
			
			ContentValues[] values = new ContentValues[pageInfo.getItems().size()];
			ContentValues pageValues = null;
			for (int i = 0; i < values.length; i++)
			{
				ContentValues cv = new ContentValues();
				FeedItem item = pageInfo.getItems().get(i);
				
				if (pageValues == null && pageInfo.getFaviconUrl() != null )//&& pageInfo.getFileIcon() == null)
				{
					pageValues = new ContentValues();
					pageValues.put( PagesContract.COL_IMGLINK, pageInfo.getFaviconUrl());
					pageValues.put( PagesContract.COL_DESCRIPTION, pageInfo.getDescription());
				}
				
				cv.put(NewsContract.COL_TITLE, item.getTitle());
				cv.put(NewsContract.COL_SITE, pageInfo.getUrl());
				cv.put(NewsContract.COL_DATE, item.getLongDate());
				cv.put(NewsContract.COL_LINK, item.getLink());
				cv.put(NewsContract.COL_IMGLINK, item.getImageLink());
				cv.put(NewsContract.COL_CATEGORY, item.getCategory());
				cv.put(NewsContract.COL_AUTHOR, item.getAuthor());
				cv.put(NewsContract.COL_DESCRIPTION, item.getDescription());
				values[i] = cv;
			}
			
			retVal = ctx.getContentResolver().bulkInsert(NewsProvider.authorityNews, values);
			int pagesUpdated = 0;
			if (pageValues != null)
			{
				Log.i(TAG, "PageUrl " + pageInfo.getUrl());
				String fname = pageInfo.getFaviconFileName( ctx );
				Log.i(TAG, "PageFaviconUrl " + pageInfo.getFaviconUrl() + " stripped " + fname);
				Log.i(TAG, "PageDescription " + pageInfo.getDescription());
				byte[] faviconBytes = getUrlAsBytes( pageInfo.getFaviconUrl() );
				Log.i(TAG, "bytes " + faviconBytes.length);
				FileOutputStream fos = new FileOutputStream( fname);
				fos.write(faviconBytes);
				fos.close();
				String where = PagesContract.COL_LINK+ "=\'" + pageInfo.getUrl()+"\'";
				Log.i(TAG, "where " + where);
				pagesUpdated = ctx.getContentResolver().update( NewsProvider.authorityPages, pageValues, where, null);
			}
			
			Log.i(TAG, "items size : " + pageInfo.getItems().size() + " pagesUpdated " + pagesUpdated);
		} catch (IOException e) {
			Log.e(TAG, "Failed to fetch items" , e );
			retVal = -1;
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Failed to parse items" , e );
			retVal = -1;
		}
		
		return retVal;
	}
	
	private static void parseItems(PageInfo pageInfo, XmlPullParser parser) throws XmlPullParserException, IOException
	{
		boolean insideChannel = false;
		boolean insideItem = false;
		FeedItem item = null;
		
		for (int eventType = parser.next(); eventType != XmlPullParser.END_DOCUMENT ; eventType = parser.next())
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (!insideItem  && !insideChannel && parser.getName().equalsIgnoreCase("channel") )
				{
					insideChannel = true;
				}
				else if (insideChannel && !insideItem  && parser.getName().equalsIgnoreCase("description"))
				{
					String feedDescription = parser.nextText();
					Log.d(TAG+"d", "FeedDescription: " + feedDescription);
					pageInfo.setDescription( feedDescription );
				}
				else if (insideChannel && !insideItem && parser.getName().equalsIgnoreCase("link"))
				{
					String feedBaseLink = parser.nextText();
					Log.d(TAG+"d", "FeedBaseLink: " + feedBaseLink);
					pageInfo.setFaviconUrl( feedBaseLink + STR_FAVICON );
				}
				
				else if (XML_ITEM.equals( parser.getName() ))
				{
					insideItem = true;
					item = new FeedItem();
				}
				else if (parser.getName().equalsIgnoreCase("title") && insideItem == true)
				{
					//String title = parser.getAttributeValue( null, "title");
					String title = parser.nextText();
					Log.d(TAG, "item title " + title);
					item.setTitle( title) ;
				}
				else if (parser.getName().equalsIgnoreCase("link") && insideItem == true)
				{
					String link = parser.nextText();
					item.setLink(link);
				}
				else if (parser.getName().equalsIgnoreCase("description") && insideItem == true)
				{
					String description = parser.nextText();
					
					Pattern pattern = Pattern.compile( "(?m)(?s).*img\\s+(.*)src\\s*=\\s*(\"|')([^\"']+\\.jpg)(\"|').*" );
					Matcher matcher = pattern.matcher( description );
					if( matcher.matches()) 
					{
						/*
						byte[] data = getUrlAsBytes(matcher.group(3));
						if (data != null)
						{
							Log.d(TAG, item.getTitle() + " : " + matcher.group(3) + " data size:" + data.length);
							Bitmap bmp = BitmapFactory.decodeByteArray(data , 0, data.length);
							item.setImage(bmp);
						}
						*/
						item.setImageLink( matcher.group(3));
					}
					item.setDescription(description);
				}
				else if (parser.getName().equalsIgnoreCase("category") && insideItem == true && item.getCategory()==null)
				{
					String category = parser.nextText();
					item.setCategory(category);
				}
				else if (parser.getName().equalsIgnoreCase("media:thumbnail") && insideItem == true && item.getCategory()==null)
				{
					String thumb = parser.nextText();
					item.setThumb(thumb);
					Log.d(TAG, "Got thumb: " + thumb);
				}				
				else if (parser.getName().equalsIgnoreCase("author") && insideItem == true)
				{
					String author = parser.nextText();
					item.setAuthor(author);
				}				
				else if (parser.getName().equalsIgnoreCase("pubDate") && insideItem == true)
				{
					String pubDate = parser.nextText();
					item.setDate( pubDate );
				}
			}
			else if (eventType == XmlPullParser.END_TAG && parser.getName().equalsIgnoreCase("item"))
			{
				insideItem = false;
				
				if (item.getTitle() != null && item.getTitle().length() > 0)
					pageInfo.getItems().add(item);
			}
		}
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
