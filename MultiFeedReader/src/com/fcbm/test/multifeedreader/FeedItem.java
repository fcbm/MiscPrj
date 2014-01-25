package com.fcbm.test.multifeedreader;

import java.util.Date;

import android.graphics.Bitmap;

public class FeedItem {

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
	
	/*public FeedItem(String title, String description)
	{
		mTitle = title;
		mDescription = description;
	}*/

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
		mLongDate = Date.parse(mDate);
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
