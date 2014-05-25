package com.fcbm.test.multifeedreader;

import java.io.File;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
	private static final int LOAD_STARRED_URLS = 3;
	private static final int LOAD_UNREAD_URLS = 4;

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
			NewsContract.COL_UNREAD,
			NewsContract.COL_STARRED,
			NewsContract.COL_IMGLINK};
	
	private final LoaderCallbacks<Cursor> mLoaderCallback = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) 
		{
			String selection = null;
			if (loaderId == LOAD_SINGLE_URL)
			{
				selection = NewsContract.COL_SITE + "='" + mPageInfo.getUrl() + "'" ;
			}
			else if (loaderId == LOAD_STARRED_URLS)
			{
				selection = NewsContract.COL_STARRED + ">0";
			}
			else if (loaderId == LOAD_UNREAD_URLS)
			{
				selection = NewsContract.COL_UNREAD + " is null";
			}
			
			String[] selectionArgs = null;

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
		mQueryHandler = new AsyncQueryHandler(getActivity().getContentResolver()) { };

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
		
		 
		mBmpDownloader = new GenericBackgroundThread<ImageView>(getActivity().getApplicationContext(), mUpdateUiHandler);

		mBmpDownloader.start();
		mBmpDownloader.getLooper();
				
		//new FeedDownloader().execute(mPageInfo);
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		int loaderId = 0;
		Bundle loaderArgs = null;
		if (mPageInfo.getUrl().startsWith("http://"))
		{
			loaderId = LOAD_SINGLE_URL;
		}
		else if (mPageInfo.getUrl().equals("Starred"))
		{
			loaderId = LOAD_STARRED_URLS;
		}
		else if (mPageInfo.getUrl().equals("Unread"))
		{
			loaderId = LOAD_UNREAD_URLS;			
		}
		else if (mPageInfo.getUrl().equals("AllFeeds"))
		{
			loaderId = LOAD_ALL_URLS;
		}
		// TODO : Allow load all news only through ActionItem

		// Call initLoader here instead of onActivityCreated, see:
		// http://stackoverflow.com/questions/15515799/should-we-really-call-getloadermanager-initloader-in-onactivitycreated-which
		// I did this to have "unread" fresh update
		LoaderManager lm = getLoaderManager();
		
		if (lm.getLoader( loaderId ) != null)
		{
			lm.restartLoader( loaderId, loaderArgs, mLoaderCallback);
		}
		else
		{
			lm.initLoader( loaderId, loaderArgs, mLoaderCallback);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		View v = super.onCreateView(inflater, parent, savedInstanceState);
		v.setBackgroundColor(  Color.parseColor("#D4D4D2"));
		ListView lv = (ListView)v.findViewById( android.R.id.list );
		lv.setPadding(15, 15, 15, 15);
		lv.setBackgroundColor(  Color.parseColor("#D4D4D2"));
		lv.setDivider( new ColorDrawable(Color.TRANSPARENT) );
		lv.setDividerHeight( 10 );
		
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
		int unread = c.getInt( c.getColumnIndex( NewsContract.COL_UNREAD ));
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
			Object cookie = null;
			String selection = NewsContract.COL_LINK + "=\'" + link + "\'";
			String[] selectionArgs = null;
			ContentValues values = new ContentValues();
			values.put( NewsContract.COL_UNREAD, (unread+1));
			mQueryHandler.startUpdate(NO_OPERATION, cookie, NewsProvider.authorityNews, values, selection, selectionArgs);
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
		
	private class ItemCursorAdapter extends SimpleCursorAdapter implements OnCheckedChangeListener
	{
		public ItemCursorAdapter() {
			super(getActivity(), R.layout.item_row, null, mProjection, null, 0);
		}
		
		@Override
		public View newView(Context ctx, Cursor c, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.item_row, null, false);
			return v;
		}
		
		@Override
		public void bindView(View view, Context ctx, Cursor c)
		{
			String title =  null;
			String link =  null;
			String category = null;
			String imageLink = null;
			long time = 0;

			int unreadInt = 0;
			int starredInt = 0; 
			int position = c.getPosition();
			
			Log.d(TAG, "Cursor at " + position);
			Log.d(TAG, "Cursor at " + position + " gotTitle");
			Log.d(TAG, "Cursor at " + position + " gotCategory");

			starredInt = c.getInt( c.getColumnIndex( NewsContract.COL_STARRED) );
			unreadInt = c.getInt( c.getColumnIndex( NewsContract.COL_UNREAD) );
			title = c.getString( c.getColumnIndex( NewsContract.COL_TITLE) );
			category = c.getString( c.getColumnIndex( NewsContract.COL_CATEGORY) );
			time = c.getLong( c.getColumnIndex( NewsContract.COL_DATE) );
			imageLink = c.getString( c.getColumnIndex( NewsContract.COL_IMGLINK) );
			link = c.getString( c.getColumnIndex( NewsContract.COL_LINK) );

			CheckBox cb = (CheckBox) view.findViewById( R.id.cbFavourite );
			cb.setTag(link);
			cb.setOnCheckedChangeListener(null);
			cb.setChecked( (starredInt > 0 ? true : false));
			cb.setOnCheckedChangeListener( this );
			
			if (title != null)
			{
				TextView tv = (TextView) view.findViewById( R.id.tvItemTitle );
				tv.setText(title);
				int col = ((unreadInt > 0) ? Color.GRAY : Color.BLACK);
				tv.setTextColor( col );
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

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			
			
			String tagLink = (String)buttonView.getTag();
			Log.d("unread", "changed " + tagLink);
			String selection = NewsContract.COL_LINK + "=\'" + tagLink + "\'";
			ContentValues values = new ContentValues(1);
			int stInt = buttonView.isChecked() ? 1 : 0;
			values.put( NewsContract.COL_STARRED, stInt);
			mQueryHandler.startUpdate(NO_OPERATION, null, NewsProvider.authorityNews, values, selection, null);		
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
