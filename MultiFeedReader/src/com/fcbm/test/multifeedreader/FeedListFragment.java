package com.fcbm.test.multifeedreader;

import java.io.File;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fcbm.test.multifeedreader.bom.PageInfo;
import com.fcbm.test.multifeedreader.provider.NewsContract;
import com.fcbm.test.multifeedreader.provider.NewsProvider;
import com.fcbm.test.multifeedreader.utils.FeedFetch;
import com.fcbm.test.multifeedreader.utils.GenericBackgroundThread;

public class FeedListFragment extends ListFragment {
	
	private static final String TAG = "FeedListFragment";
	public static final String KEY_URL = "url";
	public static final String KEY_DESCRIPTION = "description";
	private static final int NO_OPERATION = 0;
	private static final int LOAD_SINGLE_URL = 1;
	private static final int LOAD_ALL_URLS = 2;

	private int mNewFeeds  = 0;
	private PageInfo mPageInfo;
	private Handler mUpdateUiHandler;
	private GenericBackgroundThread<ImageView> mBmpDownloader;
	private ListItemClickListener mClickItemListener;
	private AsyncQueryHandler mQueryHandler;
	
	public interface ListItemClickListener
	{
		public void onListItemClicked(Bundle b);
	}
	
	private class FeedDownloader extends AsyncTask<PageInfo, Void, Integer>
	{
		@Override
		protected Integer doInBackground(PageInfo... args) {
			PageInfo pi = args[0] ;
			if (pi.getUrl().startsWith("http://") || pi.getUrl().startsWith("https://"))
			{
				return FeedFetch.downloadFeedItems( getActivity(), pi);
			}
			else
			{
				return FeedFetch.downloadFeedItems( getActivity() );
			}
		}
		
		@Override
		public void onPostExecute(Integer result)
		{
			mNewFeeds = result;
			setupAdapter();
		}
	};
	
	
	public static FeedListFragment newInstance(Bundle args)
	{
		FeedListFragment f = new FeedListFragment();
		f.setArguments(args);
		return f;
	}
	
	private SimpleCursorAdapter mCursorAdapter = null;
	private String[] mProjection = new String[] {
			NewsContract.COL_ID, 
			NewsContract.COL_TITLE, 
			NewsContract.COL_CATEGORY, 
			NewsContract.COL_DATE,
			NewsContract.COL_LINK,
			NewsContract.COL_DESCRIPTION,
			NewsContract.COL_IMGLINK};
	
	private final LoaderCallbacks<Cursor> mLoaderCallback = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) 
		{
			//String selection = loaderId == LOAD_SINGLE_URL ? NewsContract.COL_SITE + " = ?" : null;
			//String[] selectionArgs = loaderId == LOAD_SINGLE_URL ?  new String[] { mPageInfo.getUrl()  } : null;
			String selection = NewsContract.COL_SITE + "='" + mPageInfo.getUrl() + "'" ;
			String[] selectionArgs = null; //LOAD_SINGLE_URL ?  new String[] {   } : null;

			String sortOrder = NewsContract.COL_DATE + " DESC";
			
			CursorLoader loader = new CursorLoader( 
					getActivity().getApplicationContext(), 
					NewsProvider.authorityNews, 
					mProjection, 
					selection, 
					selectionArgs,
					sortOrder);
			
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
			Log.i(TAG, "Cursor count : " + c.getCount());
			mCursorAdapter.swapCursor(c);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
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
	@SuppressLint("HandlerLeak")
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setHasOptionsMenu(true);

		mPageInfo = getArguments().getParcelable( FeedListActivity.KEY_PAGE_INFO );
		
		mUpdateUiHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if (msg.what == GenericBackgroundThread.DOWNLOADED_BITMAP)
				{
					Bundle data = msg.getData();
					String fname = data.getString(GenericBackgroundThread.DOWNLOADED_BITMAP_FNAME);
					
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
		mBmpDownloader = new GenericBackgroundThread<ImageView>(getActivity().getApplicationContext(), mUpdateUiHandler);

		mBmpDownloader.start();
		mBmpDownloader.getLooper();
		
		int loaderId = 0;
		Bundle loaderArgs = null;
		
		if (mPageInfo.getUrl().startsWith("http://"))
			loaderId = LOAD_SINGLE_URL;
		// TODO : Allow load all news only through ActionItem
		//else
		//	loaderId = LOAD_ALL_URLS;
		
		getLoaderManager().initLoader(loaderId, loaderArgs, mLoaderCallback);
		
		new FeedDownloader().execute(mPageInfo);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		View v = super.onCreateView(inflater, parent, savedInstanceState);

		ListView lv = (ListView)v.findViewById( android.R.id.list );
		lv.setPadding(5, 5, 5, 5);
		lv.setBackgroundColor(  Color.parseColor("#D4D4D2"));
		lv.setDivider( new ColorDrawable(Color.TRANSPARENT) );
		lv.setDividerHeight( 5 );
		
		mCursorAdapter = new ItemCursorAdapter();
		setListAdapter(mCursorAdapter);
		
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		
		Cursor c = (Cursor)l.getAdapter().getItem(position);

		Bundle b = new Bundle();
		String link = c.getString( c.getColumnIndex( NewsContract.COL_LINK ));
		String description = c.getString( c.getColumnIndex( NewsContract.COL_DESCRIPTION ));
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
		
		Log.i(TAG, "Number of new items " + mNewFeeds);

		if (mNewFeeds < 0)
		{
			Toast.makeText( getActivity(), "Failed to update feed", Toast.LENGTH_LONG).show();
		}
		else
		{
			mCursorAdapter.notifyDataSetChanged();
		}
	}
		
	private class ItemCursorAdapter extends SimpleCursorAdapter
	{
		public ItemCursorAdapter() {
			super(getActivity(), R.layout.item_row, null, mProjection, null, 0);
		}
		
		@Override
		public View newView(Context ctx, Cursor c, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
			return inflater.inflate(R.layout.item_row, null, false);
		}
		
		@Override
		public void bindView(View view, Context ctx, Cursor c)
		{
			String title =  null;
			String category = null;
			String imageLink = null;
			long time = 0;
			int position = c.getPosition();
			
			Log.d(TAG, "Cursor at " + position);
			Log.d(TAG, "Cursor at " + position + " gotTitle");
			Log.d(TAG, "Cursor at " + position + " gotCategory");

			title = c.getString( c.getColumnIndex( NewsContract.COL_TITLE) );
			category = c.getString( c.getColumnIndex( NewsContract.COL_CATEGORY) );
			time = c.getLong( c.getColumnIndex( NewsContract.COL_DATE) );
			imageLink = c.getString( c.getColumnIndex( NewsContract.COL_IMGLINK) );
			
			if (title != null)
			{
				TextView tv = (TextView) view.findViewById( R.id.tvItemTitle );
				tv.setText(title);
			}
			if (category != null)
			{ 
				TextView tv = (TextView) view.findViewById( R.id.tvItemCategory );
				tv.setText(category);
			}
			if (time != 0)
			{
				Date d = new Date();
					
				int diffInDays = (int)( (d.getTime() - time) / (1000 * 60 * 60 * 24) );
				int diffInHours = (int)( (d.getTime() - time) / (1000 * 60 * 60) );	
				int diffInMins = (int)( (d.getTime() - time) / (1000 * 60) );
					
				TextView tv = (TextView) view.findViewById( R.id.tvItemDate);
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
			ImageView iv = (ImageView)view.findViewById( R.id.ivItemImage );
			iv.setImageBitmap( null );
			
			if ( imageLink != null )
			{
				//iv.setImageBitmap( fi.getImage() );
				mBmpDownloader.queueDownloadBitmap( iv, imageLink);
			}			
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
			String where = NewsContract.COL_SITE + "=\'" + mPageInfo.getUrl() + "\'";
			mQueryHandler.startDelete(NO_OPERATION, cookie, NewsProvider.authorityNews, where, null);
			return true;
		case R.id.loadItems:
			new FeedDownloader().execute(mPageInfo);
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
