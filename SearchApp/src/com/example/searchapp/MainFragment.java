package com.example.searchapp;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
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
	
	public static final String KEY_QUERY = "KEY_QUERY";
	
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
			final String NO_QUERY = "";
			
			String query = PreferenceManager.getDefaultSharedPreferences( getActivity() ).getString( KEY_QUERY, NO_QUERY);
			
			if ( query != null && query.length() > 0)
			{
				selection = new String(TheProvider.COL_TITLE + " LIKE \"%" + query + "%\"");
			}
			
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
	
	public void refreshItems()
	{
		Bundle loaderArgs = null;
		getLoaderManager().restartLoader( KEY_LOAD_MOVIES, loaderArgs, mLoaderCallbacks);;
	}
	
	public static MainFragment newInstance(Bundle b)
	{
		MainFragment mf = new MainFragment();
		mf.setArguments(b);
		return mf;
	}
	
	private MainFragment() {
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
			//lm.restartLoader( KEY_LOAD_MOVIES, loaderArgs, mLoaderCallbacks);
			refreshItems();
		}
	}
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "onCreateOptionsMenu");
		
		inflater.inflate(R.menu.main, menu);
		
		// Get ComponentName from Activity
		Activity a = getActivity();
		ComponentName cn = a.getComponentName();
		
		// Get ServiceManager from context (Activity)
		SearchManager sm = (SearchManager) a.getSystemService( Context.SEARCH_SERVICE );
		
		// Get SearchableInfo for the given ComponentName
		// SearchableInfo contains info from searchable.xml
		SearchableInfo si = sm.getSearchableInfo( cn );
		
		// Get ManuItem from Menu (needed to get the SearchView)
		MenuItem mi = (MenuItem) menu.findItem( R.id.action_search_view );
		
		// Get SearchView from MenuItem (for compatibility from MenuItemCompat)
		SearchView sw = (SearchView) MenuItemCompat.getActionView(mi);
		
		// Let the SearchView know about the SearchableInfo 
		sw.setSearchableInfo( si );
		
		sw.setOnQueryTextListener( new OnQueryTextListener() {
			
			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.d(TAG+"FC", "onQueryTextSubmit " + query);
				return handleEmptyQuery(query);
			}
			
			@Override
			public boolean onQueryTextChange(String query) {
				Log.d(TAG+"FC", "onQueryTextChange " + query);
				return handleEmptyQuery(query);
			}
			
			private boolean handleEmptyQuery(String query)
			{
				if (query == null || query.length() == 0)
				{
					PreferenceManager.getDefaultSharedPreferences( getActivity() )
						.edit()
						.putString(KEY_QUERY, null)
						.commit();
					refreshItems();
					return true;
				}
				return false;
			}
		});
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
			Log.d(TAG+"FC", "Search View");
		}
		else if (id == R.id.action_populate)
		{
			Log.d(TAG, "PopulateDB");
			ContentValues[] cvList = new ContentValues[5];
			cvList[0] = new ContentValues();
			cvList[0].put(TheProvider.COL_TITLE, "Title A");
			cvList[0].put(TheProvider.COL_DIRECTOR, "Director A");

			cvList[1] = new ContentValues();
			cvList[1].put(TheProvider.COL_TITLE, "Super Title");
			cvList[1].put(TheProvider.COL_DIRECTOR, "Super Director");

			cvList[2] = new ContentValues();
			cvList[2].put(TheProvider.COL_TITLE, "Mega Title");
			cvList[2].put(TheProvider.COL_DIRECTOR, "Mega Director");

			cvList[3] = new ContentValues();
			cvList[3].put(TheProvider.COL_TITLE, "SuperMega Title");
			cvList[3].put(TheProvider.COL_DIRECTOR, "SuperMega Director");

			cvList[4] = new ContentValues();
			cvList[4].put(TheProvider.COL_TITLE, "Arci Title");
			cvList[4].put(TheProvider.COL_DIRECTOR, "Arci Director");

			getActivity().getContentResolver().bulkInsert(TheProvider.AUTHORITY, cvList);
			// TODO : check, is it correct?
			sca.notifyDataSetChanged();
		}
		else if (id == R.id.action_clear)
		{
			Log.d(TAG, "ClearDb");
			// TODO: search when it's safe to call getActivity
			// within a Fragment
			getActivity().getContentResolver().delete(TheProvider.AUTHORITY, null, null);
			sca.notifyDataSetChanged();
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
			View v = li.inflate( R.layout.row_movie, parent, false);
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
