package com.fcbm.test.multifeedreader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


public class FeedFetch {

	private static final String TAG = "FeedFetch";
	
	private static final String XML_ITEM = "item";
	
	private static String getUrlAsString(String urlSpec) throws IOException
	{
		return new String(getUrlAsBytes(urlSpec));
	}
	
	public static int downloadFeedItems(Context ctx)
	{
		Cursor c = ctx.getContentResolver().query( NewsProvider.authorityPages, new String[] { "DISTINCT " + NewsProvider.PAGES_COL_LINK}, null, null, null);
		int retVal = 0;
		c.moveToFirst();
		Log.d(TAG+"s", "Count " + c.getCount());
		while (!c.isAfterLast())
		{
			String url = c.getString( c.getColumnIndex( NewsProvider.PAGES_COL_LINK));
			String title = "";//c.getString( c.getColumnIndex( NewsProvider.NEWS_COL_TITLE));
			Log.d(TAG+"s", "Url " + url + " title " + title);
			retVal += downloadFeedItems( ctx , url);
			c.moveToNext();
		}
		c.close();
		return retVal;
	}
	public static int downloadFeedItems(Context ctx, String url)
	{
		ArrayList<FeedItem> items = new ArrayList<FeedItem>();
		int retVal = 0;
		try
		{
			String urlStr = Uri.parse(url).buildUpon().build().toString();
			
			Log.i(TAG, "URL STR input -" + urlStr + "-");
			String xmlString = getUrlAsString(urlStr);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			
			parser.setInput( new StringReader( xmlString) );
			
			parseItems(items, parser);
			
			ContentValues[] values = new ContentValues[items.size()];
			for (int i = 0; i < items.size(); i++)
			{
				ContentValues cv = new ContentValues();
				
				cv.put(NewsProvider.NEWS_COL_TITLE, items.get(i).getTitle());
				cv.put(NewsProvider.NEWS_COL_SITE, url);
				cv.put(NewsProvider.NEWS_COL_DATE, items.get(i).getLongDate());
				cv.put(NewsProvider.NEWS_COL_LINK, items.get(i).getLink());
				cv.put(NewsProvider.NEWS_COL_IMGLINK, items.get(i).getImageLink());
				cv.put(NewsProvider.NEWS_COL_CATEGORY, items.get(i).getCategory());
				cv.put(NewsProvider.NEWS_COL_AUTHOR, items.get(i).getAuthor());
				cv.put(NewsProvider.NEWS_COL_DESCRIPTION, items.get(i).getDescription());
				values[i] = cv;
			}
			
			retVal = ctx.getContentResolver().bulkInsert(NewsProvider.authority, values);
			
			Log.i(TAG, "items size : " + items.size());
		} catch (IOException e) {
			Log.e(TAG, "Failed to fetch items" , e );
			retVal = -1;
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Failed to parse items" , e );
			retVal = -1;
		}
		
		return retVal;
	}
	
	private static void parseItems(ArrayList<FeedItem> items, XmlPullParser parser) throws XmlPullParserException, IOException
	{
		
		int eventType = parser.next();
		boolean insideItem = false;

		FeedItem item = null;
		while (eventType != XmlPullParser.END_DOCUMENT)
		{
			if (eventType == XmlPullParser.START_TAG)
			{
				if (XML_ITEM.equals( parser.getName() ))
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
							Log.d(TAG+"s", item.getTitle() + " : " + matcher.group(3) + " data size:" + data.length);
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
					Log.d(TAG+"t", "Got thumb: " + thumb);
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
					items.add(item);
			}
			eventType = parser.next();
		}
	}	
	
	public static byte[] getUrlAsBytes(String urlString) throws IOException
	{
		URL url = new URL(urlString);
		
		Log.d(TAG, "connecting to " + url.toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        /*
		connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestProperty("User-agent", "Mozilla AppleWebKit Chrome Safari"); // some feeds need this to work properly
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(30000);
        connection.setUseCaches(false);*/


        //connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
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
