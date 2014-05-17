package com.fcbm.test.multifeedreader.bom;

import java.util.Date;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class FeedItem implements Parcelable{

	private String mTitle;
	private String mDescription;
	private String mLink;
	private String mCategory;
	private String mAuthor;
	private String mDate;
	private String mThumb;
	private String mImageLink;
	private String mImagePath;
	private Bitmap mImage;
	private long mLongDate;

	public FeedItem()
	{}
	

	private FeedItem(Parcel source)
	{
		readFromParcel(source);
	}

	public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>()
	{
		@Override
		public FeedItem createFromParcel(Parcel source) {
			return new FeedItem(source);
		}

		@Override
		public FeedItem[] newArray(int size) {
			return new FeedItem[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString( mTitle );
		dest.writeString( mDescription );
		dest.writeString( mLink );
		dest.writeString( mCategory );
		dest.writeString( mAuthor );
		dest.writeString( mDate );
		dest.writeString( mThumb );
		dest.writeString( mImageLink );
		dest.writeString( mImagePath );
		dest.writeParcelable( mImage, 0);
		dest.writeLong( mLongDate );
	}
	
	private void readFromParcel(Parcel source)
	{
		mTitle = source.readString();
		mDescription = source.readString();
		mLink = source.readString();
		mCategory = source.readString();
		mAuthor = source.readString();
		mDate = source.readString();
		mThumb = source.readString();
		mImageLink = source.readString();
		mImagePath = source.readString();
		mImage = source.readParcelable( Bitmap.class.getClassLoader() );
		mLongDate = source.readLong();
	}
	
	@Override
	public String toString()
	{
		return mTitle;
	}	

	public String getImageLink() {
		return mImageLink;
	}

	public void setImageLink(String imageLink) {
		mImageLink = imageLink;
	}
	
	public String getImageLinkAsFileName()
	{
		String aRetVal = null;
		// TODO: cache this value
		if (mImageLink != null)
		{
			aRetVal = mImageLink.replaceAll("[^a-zA-Z0-9]", "_");
		}
		return aRetVal;
	}

	public String getImagePath() {
		return mImagePath;
	}

	public void setImagePath(String imagePath) {
		mImagePath = imagePath;
	}

	public String getThumb() {
		return mThumb;
	}

	public void setThumb(String thumb) {
		mThumb = thumb;
	}
	
	
	public Bitmap getImage() {
		return mImage;
	}

	public void setImage(Bitmap image) {
		mImage = image;
	}
	
	
	public String getLink() {
		return mLink;
	}

	public void setLink(String link) {
		mLink = link;
	}

	public String getCategory() {
		return mCategory;
	}

	public void setCategory(String category) {
		mCategory = category;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public void setAuthor(String author) {
		mAuthor = author;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		mDate = date;
		//try {
			//mLongDate = new SimpleDateFormat("dd-MMM-yyyy", Locale.ITALY).parse(mDate).getTime();
			mLongDate = Date.parse(mDate);
		//} catch (ParseException e) {
		//	mLongDate = 0;
		//	e.printStackTrace();
		//}
	}
	
	public long getLongDate()
	{
		return mLongDate;
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
}
