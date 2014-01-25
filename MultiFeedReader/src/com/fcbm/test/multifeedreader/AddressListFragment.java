package com.fcbm.test.multifeedreader;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AddressListFragment extends ListFragment {

	private static final String TAG = "AddressListFragment";
	
	ArrayList<PageInfo> mPages;
	PageInfoAdapter mAdapter;

	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mPages = new ArrayList<PageInfo>();
		mPages.add( new PageInfo("All Feeds", "", "AllFeeds", R.drawable.ic_launcher, Color.RED));
		mPages.add( new PageInfo("Starred", "", "Starred", R.drawable.ic_launcher, Color.RED));
		mPages.add( new PageInfo("Unread", "", "Unread", R.drawable.ic_launcher, Color.RED));

		mPages.add( new PageInfo("Il Fatto Quotidiano", "NEWS", "http://www.ilfattoquotidiano.it/feed/", R.drawable.ilfattoquotidiano, Color.RED));
		mPages.add( new PageInfo("Repubblica.it", "NEWS", "http://rss.feedsportal.com/c/32275/f/438637/index.rss", R.drawable.repubblica, Color.BLUE ));
		mPages.add( new PageInfo("Link2Universe.net", "Science", "http://feeds.feedburner.com/Link2universe", R.drawable.link2universe, Color.MAGENTA));
		mPages.add( new PageInfo("SlashDot.org", "News for nerds, stuff that matters", "http://rss.slashdot.org/Slashdot/slashdot", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("LaStampa.it", "NEWS", "http://lastampa.feedsportal.com/c/32418/f/637885/index.rss", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Il Sole 24 Ore", "NEWS", "http://feeds.ilsole24ore.com/c/32276/f/438662/index.rss", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Il Messaggero", "NEWS", "http://www.ilmessaggero.it/rss/home.xml", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Il Giornale", "NEWS", "http://www.ilgiornale.it/feed.xml", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Libero", "NEWS", "http://libero-news.it.feedsportal.com/c/34068/f/618095/index.rss", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("L'Unità", "NEWS", "http://www.unita.it/cmlink/feed-homepage-1.244567", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Adnkronos", "NEWS", "http://rss.feedsportal.com/c/32375/f/448341/index.rss", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("ANSA", "NEWS", "https://www.ansa.it/main/notizie/awnplus/topnews/synd/ansait_awnplus_topnews_medsynd_Today_Idx.xml", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Rainews24.it", "NEWS", "http://www.rainews.it/dl/rainews/rss/ContentSet-945f3230-f5e5-4292-a9b0-4e8bf5d266ef-0.html", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Corriere Dello Sport", "NEWS", "http://corrieredellosport.feedsportal.com/c/34176/f/619144/index.rss", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Corriere Della Sera", "NEWS", "http://xml.corriereobjects.it/rss/homepage.xml", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("HardwareUpgrade", "NEWS", "http://feeds.hwupgrade.it/rss_hwup.xml", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("XKCD", "Comics", "http://xkcd.com/rss.xml", R.drawable.slashdot, Color.GREEN ));
		mPages.add( new PageInfo("Spinoza.it", "Comics", "http://feeds.feedburner.com/Spinoza", R.drawable.slashdot, Color.GREEN ));
		
		
		
		mAdapter = new PageInfoAdapter( mPages);
		pushToDb(mPages);
		
		setListAdapter(mAdapter);
	}
	
	private void pushToDb(List<PageInfo> items)
	{
		
		ContentValues[] listOfValues = new ContentValues[items.size()];
		for(int i = 0; i < items.size(); i++)
		{
			PageInfo info = items.get(i);
			ContentValues cv = new ContentValues();
			cv.put(NewsProvider.PAGES_COL_LINK, info.getUrl());
			cv.put(NewsProvider.PAGES_COL_TITLE, info.getTitle());
			cv.put(NewsProvider.PAGES_COL_DESCRIPTION, info.getDescription());
			Log.d(TAG, "inserting Items " + info.getUrl());
			listOfValues[i] = cv ;
		}
		int inserted = getActivity().getContentResolver().bulkInsert( NewsProvider.authorityPages, listOfValues);
		Log.d(TAG, "inserted Items " + inserted);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		return v;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		//super.onListItemClick(l, v, position, id);
		
		Intent intent = new Intent(getActivity(), FeedListActivity.class);
		
		PageInfo info = (PageInfo) getListAdapter().getItem(position);
		
		intent.putExtra( FeedListActivity.KEY_TITLE, info.getTitle());
		intent.putExtra( FeedListActivity.KEY_DESCRIPTION, info.getDescription());
		intent.putExtra( FeedListActivity.KEY_URL, info.getUrl());
		intent.putExtra( FeedListActivity.KEY_COLOR, info.getColor());
		intent.putExtra( FeedListActivity.KEY_ICON, info.getIcon());
		
		startActivity(intent);
		
		return;
	}
	
	private class PageInfoCursorAdapter extends SimpleCursorAdapter
	{
		public PageInfoCursorAdapter()
		{
			super(getActivity(), R.layout.item_img_row, null, null, null, 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate( R.layout.item_img_row, null);
			}
			
			Cursor c = (Cursor) getItem(position);
			c.getString( c.getColumnIndex( NewsProvider.PAGES_COL_TITLE ));
			c.getString( c.getColumnIndex( NewsProvider.PAGES_COL_DESCRIPTION ));
			c.getInt( c.getColumnIndex( NewsProvider.PAGES_COL_IMGLINK ));
			return convertView;
		}
		
	}
	
	private class PageInfoAdapter extends ArrayAdapter<PageInfo>
	{

		public PageInfoAdapter(ArrayList<PageInfo> pages) {
			super(getActivity(), 0,  pages);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View v = convertView;
			
			if (v == null)
			{
				v = getActivity().getLayoutInflater().inflate(R.layout.page_row, null);
			}
			
			PageInfo pi = getItem(position);
			
			TextView title = (TextView)v.findViewById( R.id.title );
			title.setText( pi.getTitle() );
			TextView description = (TextView)v.findViewById( R.id.description );
			description.setText( pi.getDescription() );
			
			ImageView iv = (ImageView) v.findViewById( R.id.favicon );
			iv.setImageResource( pi.getIcon() );
			
			//View border = v.findViewById( R.id.border );
			//border.setBackgroundColor( pi.getColor() );
			
			return v;
		}
	}
	
}
