package com.fcbm.test.multifeedreader;

import java.util.ArrayList;

import com.fcbm.test.multifeedreader.bom.PageInfo;
import com.fcbm.test.multifeedreader.provider.NewsProvider;
import com.fcbm.test.multifeedreader.provider.PagesContract;
import com.fcbm.test.multifeedreader.provider.PagesJoinNewsContract;
import com.fcbm.test.multifeedreader.utils.NewsProviderUtils;

import android.content.AsyncQueryHandler;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
		public Loader<Cursor> onCreateLoader(int requestId, Bundle requestArgs) 
		{
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;

			return new CursorLoader( getActivity(), NewsProvider.authorityPagesAndCountedNews, mProjection, selection, selectionArgs, sortOrder);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
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
		Log.d(TAG, "onCreate" );
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreateView" );
		
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		ListView lv = (ListView)v.findViewById( android.R.id.list );
		lv.setPadding(5, 5, 5, 5);
		// TODO : define resource
		lv.setBackgroundColor(  Color.parseColor("#D4D4D2"));
		lv.setDivider( new ColorDrawable(Color.TRANSPARENT) );
		lv.setDividerHeight( 5 );
		
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
		private final String[] colors = new String[] 
				{ "#33B5E5", "#AA66CC", "#99CC00", "#FFBB33", "#FF4444",
				  "#0099CC", "#9933CC", "#669900", "#FF8800", "#CC0000" };
		
		public PageInfoCursorAdapter()
		{
			super(getActivity(), R.layout.page_row, null, mProjection, null, 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				// Notice : we could use getSystemService here
				// getActivity().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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
			TextView tvFavicon = (TextView) convertView.findViewById( R.id.favicon );

			tvTitle.setText( pi.getTitle() );
			tvDescription.setText( pi.getDescription() );
			tvNumberOfItems.setText( String.valueOf(size) );

			int colorIndex = position % colors.length ;
			tvFavicon.setBackgroundColor( Color.parseColor(colors[colorIndex]) );
			tvFavicon.setText( ""+tvTitle.getText().charAt(0) );
			
			//ivFavicon.setImageBitmap( pi.getFavicon( getActivity() ) );
			// see http://stackoverflow.com/questions/18660672/setting-background-of-imagview-in-relativelayout

			//ivFavicon.setImageDrawable( 
				//	new BitmapDrawable( getActivity().getResources(), pi.getFavicon( getActivity() ) ));
			
			return convertView;
		}
	}

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

		NewsProviderUtils.storePageInfo(getActivity(), mPages);
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
}
