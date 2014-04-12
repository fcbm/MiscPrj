package com.fcbm.test.multifeedreader.bom;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fcbm.test.multifeedreader.R;
import com.fcbm.test.multifeedreader.R.drawable;
import com.fcbm.test.multifeedreader.provider.NewsContract;
import com.fcbm.test.multifeedreader.provider.NewsProvider;
import com.fcbm.test.multifeedreader.provider.PagesContract;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class PageInfo implements Parcelable{

	private static final String TAG = "PageInfo";
	
	private String mTitle;
	private String mDescription;
	private String mUrl;
	private String mFaviconUrl;
	private Bitmap mFavicon;
	private List<FeedItem> mItems;
	//private static Random mGenerator = new Random();

	public PageInfo(Cursor c)
	{
		int colPosition = c.getColumnIndex( PagesContract.COL_TITLE ); 
		if (colPosition >= 0)
			mTitle = c.getString( colPosition );
		colPosition = c.getColumnIndex( PagesContract.COL_DESCRIPTION );
		if (colPosition >= 0)
			mDescription = c.getString( colPosition );
		colPosition = c.getColumnIndex( PagesContract.COL_LINK);
		if (colPosition >= 0)
			mUrl = c.getString( colPosition );
		colPosition = c.getColumnIndex( PagesContract.COL_IMGLINK );
		if (colPosition >= 0)
			mFaviconUrl = c.getString( colPosition );
		mItems = new ArrayList<FeedItem>();
	}
	
	public PageInfo(String title, String description, String url)
	{
		mTitle = title;
		mDescription = description;
		mUrl = url;
		mItems = new ArrayList<FeedItem>();
		mFavicon = null;
		//mColor = mGenerator.nextInt(256) * mGenerator.nextInt(256) * mGenerator.nextInt(256);
		//Log.d("Color", "is " + mColor);
	}
	
	private PageInfo(Parcel source)
	{
		readFromParcel(source);
	}
	
	public static final Parcelable.Creator<PageInfo> CREATOR = new Parcelable.Creator<PageInfo>() {

		@Override
		public PageInfo createFromParcel(Parcel source) {
			return new PageInfo(source);
		}

		@Override
		public PageInfo[] newArray(int size) {
			return new PageInfo[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	public boolean faviconFileExists(String faviconFileName)
	{
		return (faviconFileName != null && new File(faviconFileName).exists());
	}
		
	public String getFaviconFileName(Context ctx)
	{
		String faviconFileName = null;
		
		if (mFaviconUrl != null)
		{
			faviconFileName = ctx.getCacheDir() + "/" + mFaviconUrl.replaceAll( "[:/]", "");
			Log.d(TAG, mTitle + " filename : " + faviconFileName);
		}
		return faviconFileName;
	}
	
	public Bitmap getFavicon(Context ctx) {
		
		if (mFavicon != null)
		{
			Log.d(TAG, mTitle + " : we have a favicon"); 
			return mFavicon;
		}
		
		String faviconFileName = getFaviconFileName(ctx);
		if (faviconFileExists(faviconFileName))
		{
			Log.d(TAG, mTitle + " filename : exists");
			mFavicon = BitmapFactory.decodeFile(faviconFileName);
		}
		else
		{
			Log.d(TAG, mTitle + " filename : take slashdot one");
			mFavicon = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.slashdot);
		}
		return mFavicon;
	}


	public void setFavicon(Bitmap favicon) {
		mFavicon = favicon;
	}	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mTitle);
		dest.writeString(mDescription);
		dest.writeString(mUrl);
		dest.writeString(mFaviconUrl);
		// mFavicon not encoded, will be retrieved by getFavicon

		// TODO: do we really need ArrayList ?
		dest.writeList( mItems );
	}	
	
	
	private void readFromParcel(Parcel source)
	{
		mTitle = source.readString();
		mDescription = source.readString();
		mUrl = source.readString();
		mFaviconUrl = source.readString();
		// mFavicon not decoded, if needed will be retrieved by getFavicon		
		mItems = new ArrayList<FeedItem>();
		source.readList(mItems , FeedItem.class.getClassLoader());
	}

	public String getFaviconUrl() {
		return mFaviconUrl;
	}

	public void setFaviconUrl(String faviconUrl) {
		mFaviconUrl = faviconUrl;
	}	
	
	public List<FeedItem> getItems() {
		return mItems;
	}

	public void setItems(List<FeedItem> items) {
		mItems = items;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		mDescription = description;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}
	
	@Override
	public String toString()
	{
		return mTitle + "\n" + mDescription + "\n" + mUrl;
	}	
}
