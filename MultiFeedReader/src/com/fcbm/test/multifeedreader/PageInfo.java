package com.fcbm.test.multifeedreader;

import java.util.Random;

import android.util.Log;


public class PageInfo {

	private String mTitle;
	private String mDescription;
	private String mUrl;
	private int mIcon;
	private int mColor;
	private static Random mGenerator = new Random();
	
	public PageInfo(String title, String description, String url, int icon, int color)
	{
		mTitle = title;
		mDescription = description;
		mUrl = url;
		mIcon = icon;
		mColor = color;
		//mColor = mGenerator.nextInt(256) * mGenerator.nextInt(256) * mGenerator.nextInt(256);
		//Log.d("Color", "is " + mColor);
	}
	
	public int getIcon() {
		return mIcon;
	}

	public void setIcon(int icon) {
		mIcon = icon;
	}

	public int getColor() {
		return mColor;
	}

	public void setColor(int color) {
		mColor = color;
	}

	@Override
	public String toString()
	{
		return mTitle + "\n" + mDescription + "\n" + mUrl;
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
}
