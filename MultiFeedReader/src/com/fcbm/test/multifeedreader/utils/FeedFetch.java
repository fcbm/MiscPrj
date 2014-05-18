package com.fcbm.test.multifeedreader.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.fcbm.test.multifeedreader.bom.FeedItem;
import com.fcbm.test.multifeedreader.bom.PageInfo;
import com.fcbm.test.multifeedreader.provider.NewsProvider;


public class FeedFetch {

	private static final String TAG = "FeedFetch";
	private static final String STR_FAVICON = "/favicon.ico";
	private static final String XML_ITEM = "item";
	
	
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
			String xmlString = HttpDownloader.getUrlAsString(urlStr);
			
			if (xmlString == null)
				return retVal;
			
			Log.i(TAG, "XML String = " + xmlString);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			
			parser.setInput( new StringReader( xmlString) );
			
			parseItems(pageInfo, parser);
			
			retVal = NewsProviderUtils.storeNewsInfo(ctx, pageInfo.getItems(), pageInfo.getUrl());
			
			NewsProviderUtils.updatePageInfo(ctx, pageInfo);
			
			Log.i(TAG, "items size : " + pageInfo.getItems().size());

			saveAndStoreFavicon(ctx, pageInfo);

		} catch (IOException e) {
			Log.e(TAG, "Failed to fetch items" , e );
			retVal = -1;
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Failed to parse items" , e );
			retVal = -1;
		}
		
		return retVal;
	}
	
	private static void saveAndStoreFavicon(Context ctx, PageInfo pageInfo)
	{
		String fname = pageInfo.getFaviconFileName( ctx );
		
		if (fname == null || PageInfo.faviconFileExists(fname))
		{
			return;
		}

		Log.i(TAG, "PageUrl " + pageInfo.getUrl());
		Log.i(TAG, "PageFaviconUrl " + pageInfo.getFaviconUrl() + " stripped " + fname);
		Log.i(TAG, "PageDescription " + pageInfo.getDescription());
			
		try {
			byte[] faviconBytes = HttpDownloader.getUrlAsBytes( pageInfo.getFaviconUrl() );
			Log.i(TAG, "bytes " + faviconBytes.length);
			FileOutputStream fos = new FileOutputStream( fname);
			fos.write(faviconBytes);
			fos.close();

		} catch (IOException e) {
			Log.e(TAG, "Failed to download favicon" , e );
			e.printStackTrace();
		}
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
}
