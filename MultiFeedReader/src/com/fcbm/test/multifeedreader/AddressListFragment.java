package com.fcbm.test.multifeedreader;

import java.util.ArrayList;
import java.util.List;

import com.fcbm.test.multifeedreader.bom.PageInfo;
import com.fcbm.test.multifeedreader.provider.NewsProvider;
import com.fcbm.test.multifeedreader.provider.PagesContract;
import com.fcbm.test.multifeedreader.provider.PagesJoinNewsContract;

import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AddressListFragment extends ListFragment {

	private static final String TAG = "AddressListFragment";
	
	private static final int NO_OPERATION = 0;
	
	private AsyncQueryHandler mQueryHandler;
	
	PageInfoCursorAdapter mAdapter;
	String[] mProjection =  
	new String[] {
			PagesContract.COL_ID, 
			PagesContract.COL_TITLE, 
			PagesContract.COL_DESCRIPTION,
			PagesContract.COL_LINK, 
			PagesContract.COL_IMGLINK,
			PagesJoinNewsContract.COL_COUNT_CNT
			};
	
	private LoaderCallbacks<Cursor> mCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int requestId, Bundle requestArgs) {

			// TODO: fix this using ProjectionMap and SQLiteQueryBuilder
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			// Retrieve cnt of feeds for each Page (no special cases)
			// select pagestable.title, count(newstable.link) from pagestable left join newstable on (pagestable.link=newstable.site) group by pagestable.link;
			
			// Retrieve cnt for special cases
			// (select pagestable.title, cnt from (select count(newstable.link) as cnt from newstable), pagestable where pagestable.title='All Feeds');
			
			return new CursorLoader( getActivity(), NewsProvider.authorityPagesAndCountedNews, mProjection, selection, selectionArgs, sortOrder);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
			//if (mAdapter != null && c != null)
				mAdapter.swapCursor( c );
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor( null );
		}
	};  
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreat" );
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		Log.d(TAG, "onCreateView" );
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated" );		
		mQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) { };
		
		// Move setListAdapter from onCreate to onActivityCreated, see :
		// http://stackoverflow.com/questions/8041206/android-fragment-oncreateview-vs-onactivitycreated
		// Remember : You must use ListFragment.setListAdapter() to associate the list with an adapter. 
		// Do not directly call ListView.setAdapter() or else important initialization will be skipped.
		mAdapter = new PageInfoCursorAdapter();
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(TAG, "onResume" );
		int requestId = 0;
		Bundle requestArgs = null;

		// Call initLoader here instead of onActivityCreated, see:
		// http://stackoverflow.com/questions/15515799/should-we-really-call-getloadermanager-initloader-in-onactivitycreated-which
		LoaderManager lm = getLoaderManager();
		
		if (lm.getLoader( requestId ) != null)
		{
			lm.restartLoader( requestId, requestArgs, mCallbacks);
		}
		else
		{
			lm.initLoader( requestId, requestArgs, mCallbacks);
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		//super.onListItemClick(l, v, position, id);
		
		Intent intent = new Intent(getActivity(), FeedListActivity.class);
		Cursor c = (Cursor)getListAdapter().getItem( position );
		
		PageInfo info = new PageInfo(c);
		
		Bundle b = new Bundle();
		b.putParcelable(FeedListActivity.KEY_PAGE_INFO, info);
		intent.putExtras(b);
		
		startActivity(intent);
		
		return;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate( R.menu.address_list, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.action_load_pages:
			loadPages();
			return true;

		case R.id.action_clear_pages:
			clearPages();
			return true;

		case R.id.action_reset_pages:
			resetPages();
			return true;
		}
		
		return super.onOptionsItemSelected( item);
	}
	
	private class PageInfoCursorAdapter extends SimpleCursorAdapter
	{
		public PageInfoCursorAdapter()
		{
			super(getActivity(), R.layout.page_row, null, mProjection, null, 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate( R.layout.page_row, null);
			}
			
			Cursor c = (Cursor) getItem(position);
			Log.d(TAG, "cursor cnt " + c.getCount());
			PageInfo pi = new PageInfo( c );
			int index = c.getColumnIndex( PagesJoinNewsContract.COL_COUNT_CNT );
			int size = c.getInt( index );
			Log.d(TAG, "index " + index + " size " + size);
			
			TextView tvTitle = (TextView) convertView.findViewById( R.id.title );
			TextView tvDescription = (TextView) convertView.findViewById( R.id.description );
			TextView tvNumberOfItems = (TextView) convertView.findViewById( R.id.numberOfItems );
			ImageView ivFavicon = (ImageView) convertView.findViewById( R.id.favicon );

			tvTitle.setText( pi.getTitle() );
			tvDescription.setText( pi.getDescription() + " " + index );
			tvNumberOfItems.setText( "" + size );
			//ivFavicon.setImageBitmap( pi.getFavicon( getActivity() ) );
			// see http://stackoverflow.com/questions/18660672/setting-background-of-imagview-in-relativelayout
			ivFavicon.setImageDrawable( 
					new BitmapDrawable( getActivity().getResources(), pi.getFavicon( getActivity() ) ));
			
			return convertView;
		}
	}
/*	
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
			//File fileIcon = pi.getFileIcon();
			File fileIcon = new File( getContext().getFilesDir() + "/" + pi.getUrl().replaceAll("[:/]", "") );
			
			//if (fileIcon == null || !fileIcon.exists())
			if (!fileIcon.exists())
			{
				Log.d(TAG, fileIcon + " not exist");
				//iv.setImageResource( pi.getIcon() );
			}
			else
			{
				Log.d(TAG, fileIcon + " exists");
				Bitmap bmpIcon = BitmapFactory.decodeFile( fileIcon.getPath() );
				iv.setImageBitmap( bmpIcon );
			}
			
			//View border = v.findViewById( R.id.border );
			//border.setBackgroundColor( pi.getColor() );
			
			return v;
		}
	}
*/	
	private void loadPages()
	{
		ArrayList<PageInfo> mPages = new ArrayList<PageInfo>();
		
		mPages.add( new PageInfo("All Feeds", "", "AllFeeds"));
		mPages.add( new PageInfo("Starred", "", "Starred"));
		mPages.add( new PageInfo("Unread", "", "Unread"));

		mPages.add( new PageInfo("Il Fatto Quotidiano", "NEWS", "http://www.ilfattoquotidiano.it/feed/"));
		mPages.add( new PageInfo("Repubblica.it", "NEWS", "http://rss.feedsportal.com/c/32275/f/438637/index.rss"));
		mPages.add( new PageInfo("Link2Universe.net", "Science", "http://feeds.feedburner.com/Link2universe"));
		mPages.add( new PageInfo("SlashDot.org", "News for nerds, stuff that matters", "http://rss.slashdot.org/Slashdot/slashdot"));
		mPages.add( new PageInfo("LaStampa.it", "NEWS", "http://lastampa.feedsportal.com/c/32418/f/637885/index.rss"));
		mPages.add( new PageInfo("Il Sole 24 Ore", "NEWS", "http://feeds.ilsole24ore.com/c/32276/f/438662/index.rss"));
		mPages.add( new PageInfo("Il Messaggero", "NEWS", "http://www.ilmessaggero.it/rss/home.xml"));
		mPages.add( new PageInfo("Il Giornale", "NEWS", "http://www.ilgiornale.it/feed.xml"));
		mPages.add( new PageInfo("Libero", "NEWS", "http://libero-news.it.feedsportal.com/c/34068/f/618095/index.rss"));
		mPages.add( new PageInfo("L'Unità", "NEWS", "http://www.unita.it/cmlink/feed-homepage-1.244567"));
		mPages.add( new PageInfo("Adnkronos", "NEWS", "http://rss.feedsportal.com/c/32375/f/448341/index.rss"));
		mPages.add( new PageInfo("ANSA", "NEWS", "https://www.ansa.it/main/notizie/awnplus/topnews/synd/ansait_awnplus_topnews_medsynd_Today_Idx.xml"));
		mPages.add( new PageInfo("Rainews24.it", "NEWS", "http://www.rainews.it/dl/rainews/rss/ContentSet-945f3230-f5e5-4292-a9b0-4e8bf5d266ef-0.html"));
		mPages.add( new PageInfo("Corriere Dello Sport", "NEWS", "http://corrieredellosport.feedsportal.com/c/34176/f/619144/index.rss"));
		mPages.add( new PageInfo("Corriere Della Sera", "NEWS", "http://xml.corriereobjects.it/rss/homepage.xml"));
		mPages.add( new PageInfo("HardwareUpgrade", "NEWS", "http://feeds.hwupgrade.it/rss_hwup.xml"));
		mPages.add( new PageInfo("XKCD", "Comics", "http://xkcd.com/rss.xml"));
		mPages.add( new PageInfo("Spinoza.it", "Comics", "http://feeds.feedburner.com/Spinoza"));

		pushToDb(mPages);
	}
	
	private void clearPages()
	{
		Object cookie = null;
		//mPages.clear();
		//getActivity().getContentResolver().delete( NewsProvider.authorityPages, null, null);
		// TODO: adapter will not be refreshed because we're using a different uri
		mQueryHandler.startDelete(NO_OPERATION, cookie, NewsProvider.authorityPagesAndCountedNews, null, null);
	}
	
	private void resetPages()
	{
		clearPages();
		loadPages();
	}
	
	private void pushToDb(List<PageInfo> items)
	{
		ContentValues[] listOfValues = new ContentValues[items.size()];
		for(int i = 0; i < items.size(); i++)
		{
			PageInfo info = items.get(i);
			ContentValues cv = new ContentValues();
			cv.put(PagesContract.COL_LINK, info.getUrl());
			cv.put(PagesContract.COL_TITLE, info.getTitle());
			cv.put(PagesContract.COL_DESCRIPTION, info.getDescription());
			Log.d(TAG, "inserting Items " + info.getUrl());
			listOfValues[i] = cv ;
		}
		int inserted = getActivity().getContentResolver().bulkInsert( NewsProvider.authorityPagesAndCountedNews, listOfValues);
		Log.d(TAG, "inserted Items " + inserted);
	}	
	
}
