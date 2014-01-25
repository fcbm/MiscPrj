package com.fcbm.test.multifeedreader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ListFragment;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FeedListFragment extends ListFragment {
	
	private static final String TAG = "FeedListFragment";
	public static final String KEY_URL = "url";
	public static final String KEY_DESCRIPTION = "description";
	private static final int NO_OPERATION = 0;
	private static final int LOAD_SINGLE_URL = 1;
	private static final int LOAD_ALL_URLS = 2;
	//private ArrayList<FeedItem> mItems;
	//private ItemListAdapter mAdapter;
	private int mNewFeeds  = 0;
	private Handler mUpdateUiHandler;
	private GenericDownloader<ImageView> mBmpDownloader;
	private String mUrl;
	private int mIconId;
	private ListItemClickListener mClickItemListener;
	private AsyncQueryHandler mQueryHandler;
	
	public interface ListItemClickListener
	{
		public void onListItemClicked(Bundle b);
	}
	
	private class FeedDownloader extends AsyncTask<String, Void, Integer>
	{
		@Override
		protected Integer doInBackground(String... args) {
			String url = args[0] ;
			if (url.startsWith("http://"))
				return FeedFetch.downloadFeedItems( getActivity(), url );
			else
				return FeedFetch.downloadFeedItems( getActivity() );
		}
		
		@Override
		public void onPostExecute(Integer result)
		{
			//mItems = result;
			mNewFeeds = result;
			Log.i(TAG, "items.size " + result);
			//GenericDownloader.clearOldFiles( getActivity().getApplicationContext(), mItems );
			setupAdapter();
		}
		
	};
	
	
	public static FeedListFragment newInstance(String title, String description, String url, int iconId)
	{
		FeedListFragment f = new FeedListFragment();
		Bundle b = new Bundle();
		
		b.putString(FeedListActivity.KEY_TITLE, title);
		b.putString(FeedListActivity.KEY_DESCRIPTION, description);
		b.putString(FeedListActivity.KEY_URL, url);
		b.putInt(FeedListActivity.KEY_ICON, iconId);
		
		f.setArguments(b);
		
		return f;
	}
	
	private SimpleCursorAdapter mCursorAdapter = null;
	private String[] mProjection = new String[] {
			NewsProvider.NEWS_COL_ID, 
			NewsProvider.NEWS_COL_TITLE, 
			NewsProvider.NEWS_COL_CATEGORY, 
			NewsProvider.NEWS_COL_DATE,
			NewsProvider.NEWS_COL_LINK,
			NewsProvider.NEWS_COL_DESCRIPTION,
			NewsProvider.NEWS_COL_IMGLINK};
	
	private final LoaderCallbacks<Cursor> mLoaderCallback = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
			
			String selection = loaderId == LOAD_SINGLE_URL ? NewsProvider.NEWS_COL_SITE + " = ?" : null;
			String[] selectionArgs = loaderId == LOAD_SINGLE_URL ?  new String[] { mUrl  } : null;
			String sortOrder = NewsProvider.NEWS_COL_DATE + " DESC";
			
			CursorLoader loader = new CursorLoader( 
					getActivity().getApplicationContext(), 
					NewsProvider.authority, 
					mProjection, 
					selection, 
					selectionArgs,
					sortOrder);
			
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
			// TODO: drop this?
			if (mCursorAdapter != null && c != null)
				mCursorAdapter.swapCursor(c);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// TODO: drop this?
			if (mCursorAdapter != null)
				mCursorAdapter.swapCursor(null);
		}
	};
	
	@Override
	public void onAttach(Activity a)
	{
		super.onAttach(a);
		mClickItemListener = (ListItemClickListener) a;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);
		//String title = savedInstanceState.getString(FeedListActivity.KEY_TITLE);
		//String description = savedInstanceState.getString(FeedListActivity.KEY_DESCRIPTION);
		mUrl = getArguments().getString(FeedListActivity.KEY_URL);
		mIconId = getArguments().getInt(FeedListActivity.KEY_ICON);
		
		mUpdateUiHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if (msg.what == GenericDownloader.DOWNLOADED_BITMAP)
				{
					Bundle data = msg.getData();
					String fname = data.getString(GenericDownloader.DOWNLOADED_BITMAP_FNAME);
					
					if (fname == null) return;
					
					File file = new File(getActivity().getApplicationContext().getCacheDir(), fname);

					Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
					if (bmp == null) return;

					ImageView iv = (ImageView) msg.obj;
					iv.setImageBitmap( bmp );
				}
			}
			
		};
		
		mQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) { }; 
		mBmpDownloader = new GenericDownloader<ImageView>(getActivity().getApplicationContext(), mUpdateUiHandler);

		mBmpDownloader.start();
		mBmpDownloader.getLooper();
		
		int loaderId = 0;
		Bundle loaderArgs = null;
		
		if (mUrl.startsWith("http://"))
			loaderId = LOAD_SINGLE_URL;
		else
			loaderId = LOAD_ALL_URLS;
		
		getLoaderManager().initLoader(loaderId, loaderArgs, mLoaderCallback);
		
		new FeedDownloader().execute(mUrl);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		
		mCursorAdapter = new ItemCursorAdapter();
		setListAdapter(mCursorAdapter);
		
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		
		//FeedItem fi = (FeedItem)l.getAdapter().getItem(position);
		Cursor c = (Cursor)l.getAdapter().getItem(position);

		Bundle b = new Bundle();
		String link = c.getString( c.getColumnIndex( NewsProvider.NEWS_COL_LINK ));
		String description = c.getString( c.getColumnIndex( NewsProvider.NEWS_COL_DESCRIPTION ));
		if (link != null)
		{
			b.putString(KEY_URL, link);
		}
		if (description != null)
		{
			b.putString(KEY_DESCRIPTION, description);
		}
		if (!b.isEmpty() && mClickItemListener != null)
		{
			mClickItemListener.onListItemClicked(b);
		}

	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mBmpDownloader.quit();
	}
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		mBmpDownloader.clearQueue();
	}
	
	
	private void setupAdapter()
	{
		if (getActivity() == null) return;
		
		if (mNewFeeds > 0)
		{
			//mAdapter = new ItemListAdapter( mItems);
			//setListAdapter(mAdapter);
			//mAdapter.notifyDataSetChanged();
	
			mCursorAdapter.notifyDataSetChanged();
		}
		//else
		{
			//setListAdapter( null );	
		}
		if (mNewFeeds < 0)
		{
			Toast.makeText( getActivity(), "Failed to update feed", Toast.LENGTH_LONG).show();
		}
	}
		
	private class ItemCursorAdapter extends SimpleCursorAdapter
	{
		public ItemCursorAdapter() {
			super(getActivity(), R.layout.item_row, null, mProjection, null, 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_row, null);
			}
			
			Log.d(TAG, "getItem at " + position);
			Cursor c = (Cursor) getItem(position);
			Log.d(TAG, "gotItem at " + position);
			String title =  null;
			String category = null;
			String imageLink = null;
			long time = 0;
			
			if (c != null)
			{
				Log.d(TAG, "Cursor at " + position);
				
				title = c.getString( c.getColumnIndex( NewsProvider.NEWS_COL_TITLE) );
				Log.d(TAG, "Cursor at " + position + " gotTitle");
				category = c.getString( c.getColumnIndex( NewsProvider.NEWS_COL_CATEGORY) );
				Log.d(TAG, "Cursor at " + position + " gotCategory");
			
				time = c.getLong( c.getColumnIndex( NewsProvider.NEWS_COL_DATE) );
				
				imageLink = c.getString( c.getColumnIndex( NewsProvider.NEWS_COL_IMGLINK) );
				
			}
			else
			{
				Log.d(TAG, "Cursor at " + position + " is null!!");
			}
			
			if (title != null)
			{
				TextView tv = (TextView) convertView.findViewById( R.id.tvItemTitle );
				tv.setText(title);
			}
			if (category != null)
			{ 
				TextView tv = (TextView) convertView.findViewById( R.id.tvItemCategory );
				tv.setText(category);
			}
			if (time != 0)
			{
				Date d = new Date();
					
				int diffInDays = (int)( (d.getTime() - time) / (1000 * 60 * 60 * 24) );
				int diffInHours = (int)( (d.getTime() - time) / (1000 * 60 * 60) );	
				int diffInMins = (int)( (d.getTime() - time) / (1000 * 60) );
					
				TextView tv = (TextView) convertView.findViewById( R.id.tvItemDate);
				if (diffInDays != 0)
				{
					tv.setText("" + diffInDays + " days ago");
				}
				else if (diffInHours != 0) 
				{
					tv.setText("" + diffInHours + " hours ago");
				}
				else if (diffInMins != 0)
				{
					tv.setText("" + diffInMins + " minutes ago");
				}
				else
				{
					tv.setText("Just Wrote!" );
				}
			}
			ImageView iv = (ImageView)convertView.findViewById( R.id.ivItemImage );
			iv.setImageBitmap( null );
			
			if ( imageLink != null )
			{
				//iv.setImageBitmap( fi.getImage() );
				mBmpDownloader.queueDownloadBitmap( iv, imageLink);
			}			

			return convertView;
		}
	}
	
	private class ItemListAdapter extends ArrayAdapter<FeedItem>
	{
		public ItemListAdapter(ArrayList<FeedItem> items) {
			super(getActivity(), 0, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate(R.layout.item_row, null);
			}
			
			FeedItem fi = (FeedItem) getItem(position);
			
			if (fi.getTitle() != null)
			{ 
				TextView tv = (TextView) convertView.findViewById( R.id.tvItemTitle );
				tv.setText(fi.getTitle());
			}/*
			if (fi.getAuthor() != null)
			{ 
				TextView tv = (TextView) convertView.findViewById( R.id.tvItemAuthor);
				tv.setText(fi.getAuthor());
			}*/			
			if (fi.getCategory() != null)
			{ 
				TextView tv = (TextView) convertView.findViewById( R.id.tvItemCategory);
				tv.setText(fi.getCategory());
			}
			if (fi.getDate() != null)
			{
				Date d = new Date();
				
				int diffInDays = (int)( (d.getTime() - Date.parse( fi.getDate() )) / (1000 * 60 * 60 * 24) );
				int diffInHours = (int)( (d.getTime() - Date.parse( fi.getDate() )) / (1000 * 60 * 60) );	
				int diffInMins = (int)( (d.getTime() - Date.parse( fi.getDate() )) / (1000 * 60) );
				
				TextView tv = (TextView) convertView.findViewById( R.id.tvItemDate);
				if (diffInDays != 0)
				{
					tv.setText("" + diffInDays + " days ago");
				}
				else if (diffInHours != 0) 
				{
					tv.setText("" + diffInHours + " hours ago");
				}
				else if (diffInMins != 0)
				{
					tv.setText("" + diffInMins + " minutes ago");
				}
				else
				{
					tv.setText("Just Wrote!" );
				}
			}
			ImageView iv = (ImageView)convertView.findViewById( R.id.ivItemImage );
			iv.setImageBitmap( null );
			
			if ( fi.getImageLink() != null )
			{
				//iv.setImageBitmap( fi.getImage() );
				mBmpDownloader.queueDownloadBitmap( iv, fi.getImageLink());
			}

			return convertView;
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.feed_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.clearItems:
			Object cookie = null;
			String where = NewsProvider.NEWS_COL_SITE + "=\'" + mUrl + "\'";
			mQueryHandler.startDelete(NO_OPERATION, cookie, NewsProvider.authority, where, null);
			return true;
		case R.id.loadItems:
			new FeedDownloader().execute(mUrl);
			return true;
		case R.id.startUpdate:
			FeedUpdateService.startPeriodicUpdate( getActivity(), getArguments() );
			return true;
		case R.id.stopUpdate:
			FeedUpdateService.stopPeriodicUpdate( getActivity() );
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

}
