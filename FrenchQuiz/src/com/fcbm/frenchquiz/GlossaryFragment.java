package com.fcbm.frenchquiz;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.ListView;
import android.widget.TextView;

public class GlossaryFragment extends ListFragment {

	private static final String TAG = "GlossaryFragment";
	
	public static GlossaryFragment newInstance(Bundle args)
	{
		GlossaryFragment gf = new GlossaryFragment();
		gf.setArguments(args);
		return gf;
	}
	
	private GlossaryCursorAdapter mAdapter = null;
	private String[] mProjection = new String[] { LanguagesProvider.GLOSSARY_COL_ID, LanguagesProvider.GLOSSARY_COL_WORD, LanguagesProvider.GLOSSARY_COL_LANG};
	
	private LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
			
			Context ctx = getActivity();
			Uri uri = LanguagesProvider.authorityGlossary;
			String[] projection = mProjection;
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;
			
			CursorLoader cursorLoader = new CursorLoader( ctx, uri, projection, selection, selectionArgs, sortOrder);
			
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			mAdapter.swapCursor( arg1 );
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
		
		setHasOptionsMenu(true);
		
		LoaderManager lm = getLoaderManager();
		
		int loaderId = 0;
		Bundle loaderArgs = null;
		lm.initLoader(loaderId, loaderArgs, mLoaderCallbacks);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = super.onCreateView(inflater, container, savedInstanceState);
		
		mAdapter = new GlossaryCursorAdapter( getActivity() );
		setListAdapter( mAdapter);
		
		return v;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate( R.menu.menu_glossary, menu);
	}

	private void populateGlossary()
	{
		List<ContentValues> cvGlossary = TermsCreatorUtil.buildTerms();

		Log.d(TAG, "CV size : " + cvGlossary.size());
		synchronized (this)
		{
			for (ContentValues cv : cvGlossary)
			{
				getActivity().getContentResolver().insert( LanguagesProvider.authorityGlossary, cv);
			}
		}
	}
	
	private synchronized void wipeGlossary()
	{
		synchronized (this)
		{
			getActivity().getContentResolver().delete( LanguagesProvider.authorityGlossary, null, null);
			getActivity().getContentResolver().delete( LanguagesProvider.authorityTranslation, null, null);
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		
		Cursor c = (Cursor) l.getAdapter().getItem( position );
		Intent i = new Intent(getActivity(), WordDetailsActivity.class );
		
		String word = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD ));
		String orgLang = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_LANG ));
		String dstLang = "it";
		i.putExtra( WordDetailsFragment.ARG_WORD, word);
		i.putExtra( WordDetailsFragment.ARG_ORG_LANG, orgLang);
		i.putExtra( WordDetailsFragment.ARG_DST_LANG, dstLang);
		startActivity( i );
		//c.close();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_item_populate_glossary:
			Log.d(TAG, "Trigger Populate Glossary");
			new Thread() 
			{ 
				@Override
				public void run()
				{
					populateGlossary();
				}
			}.start();
			return true;
		case R.id.menu_item_wipe_glossary:
			Log.d(TAG, "Trigger Wipe Glossary");
			new Thread() 
			{ 
				@Override
				public void run()
				{
					wipeGlossary();
				}
			}.start();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private class GlossaryCursorAdapter extends SimpleCursorAdapter
	{
		public GlossaryCursorAdapter(Context context) {
			super(context, 0, null, mProjection, null, 0);
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			//super.getView(position, convertView, parent);
			
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate( R.layout.row_glossary_item, null); 
			}
			
			Cursor c = (Cursor) getItem(position);
			String word = null;
			String lang = null;
			
			if (c != null)
			{
				word = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD ));
				lang = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_LANG ));
				//c.close();
			}
			
			TextView tvWord =(TextView) convertView.findViewById( R.id.tvGlossaryWord );
			tvWord.setText( word );
			TextView tvLang = (TextView) convertView.findViewById( R.id.tvGlossaryLang );
			tvLang.setText( lang );
			
			return convertView;
		}
	};
}
