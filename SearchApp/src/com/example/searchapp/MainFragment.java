package com.example.searchapp;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


public class MainFragment extends Fragment {
	
	private static final String TAG = "MainFragment";
	private static final int KEY_LOAD_MOVIES = 1;
	private static final String mProjection[] = new String[] { 
			TheProvider.COL_ID, 
			TheProvider.COL_TITLE, 
			TheProvider.COL_DIRECTOR };
	private SimpleCursorAdapter sca = null;
	
	private final LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
			Log.d(TAG, "onCreateLoader");
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;
			
			return new CursorLoader(getActivity(), 
					TheProvider.AUTHORITY, 
					mProjection, 
					selection, 
					selectionArgs, 
					sortOrder);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
			Log.d(TAG, "onLoadFinished");
			sca.swapCursor(c);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			Log.d(TAG, "onLoaderReset");
			sca.swapCursor(null);
		}
	};
	
	public MainFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		Log.d(TAG, "onCreateView");
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		
		setHasOptionsMenu(true);
		
		sca = new MoviesCursorAdapter(getActivity(), R.layout.row_movie, null, mProjection, null);
		
		ListView lv = (ListView)rootView.findViewById( R.id.lvMovies );
		
		lv.setAdapter( sca );
		return rootView;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(TAG, "onResume");
		Bundle loaderArgs = null;
		LoaderManager lm = getLoaderManager();
		if (lm.getLoader(KEY_LOAD_MOVIES) == null )
		{
			Log.d(TAG, "onResume initLoader");
			lm.initLoader( KEY_LOAD_MOVIES, loaderArgs, mLoaderCallbacks);
		}
		else
		{
			Log.d(TAG, "onResume restartLoader");
			lm.restartLoader( KEY_LOAD_MOVIES, loaderArgs, mLoaderCallbacks);
		}
	}
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
		
		Activity a = getActivity();
		if (a == null)
		{
			Log.d(TAG, "onCreateOptionsMenu a is null");
			return;
		}
		Log.d(TAG, "onCreateOptionsMenu create SV");
		SearchManager sm = (SearchManager) a.getSystemService( Context.SEARCH_SERVICE );
		Log.d(TAG, "onCreateOptionsMenu create SV1");
		if (a.getComponentName() == null)
		{
			Log.d(TAG, "onCreateOptionsMenu create cn null");
		}
		SearchableInfo si = sm.getSearchableInfo( a.getComponentName() );
		Log.d(TAG, "onCreateOptionsMenu create SV2");
		MenuItem mi = (MenuItem) menu.findItem( R.id.action_search_view );
		SearchView sw = (SearchView)MenuItemCompat.getActionView(mi);
		sw.setSearchableInfo( si );
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "onOptionsItemSelected");
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		else if (id == R.id.action_search_btn)
		{
			return getActivity().onSearchRequested();
		}
		else if (id == R.id.action_search_view)
		{
			Log.d(TAG, "Search View");
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private static class MoviesCursorAdapter extends SimpleCursorAdapter
	{
		public MoviesCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to, 0);
			Log.d(TAG, "MoviesCursorAdapter");
			
		}
		
		public View newView(Context ctx, Cursor c, ViewGroup parent)
		{
			Log.d(TAG, "newView");
			LayoutInflater li = (LayoutInflater) ctx.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
			View v = li.inflate( R.layout.row_movie, null, false);
			return v;
		}
		
		public void bindView(View container, Context ctx, Cursor c)
		{
			if (c == null || c.getCount() < 1)
			{
				Log.d(TAG, "bindView c is null");
				return;
			}
			
			String title = c.getString( c.getColumnIndex( TheProvider.COL_TITLE ));
			String director = c.getString( c.getColumnIndex( TheProvider.COL_DIRECTOR ));
			
			Log.d(TAG, "bindView : title : " + title + " director: " + director);
			TextView tvTitle = (TextView)container.findViewById( R.id.tvTitle );
			TextView tvDirector = (TextView)container.findViewById( R.id.tvDirector );
			
			tvTitle.setText( title );
			tvDirector.setText( director );
		}
	};
}
