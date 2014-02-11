package com.fcbm.frenchquiz;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WordDetailsFragment extends Fragment {

	public static final String ARG_WORD = "arg_word";
	public static final String ARG_ORG_LANG = "arg_org_lang";
	public static final String ARG_DST_LANG = "arg_dst_lang";
	
	public static WordDetailsFragment newInstance(Bundle args)
	{
		WordDetailsFragment wdf = new WordDetailsFragment();
		wdf.setArguments( args );
		return wdf;
	}
	
	private View mRootView = null;
	private SimpleCursorAdapter mCursorAdapter = null;
	private String[] mProjection = new String [] { 
			LanguagesProvider.GLOSSARY_COL_ID,
			LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION,
			LanguagesProvider.GLOSSARY_COL_WORD
			};
	
	private LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
			Context context = getActivity();
			Uri uri = LanguagesProvider.authorityTranslation;
			String[] projection = mProjection;
			String selection = null;
			String[] selectionArgs = new String[] {args.getString( ARG_WORD) , args.getString( ARG_ORG_LANG) , args.getString( ARG_DST_LANG)};
			String sortOrder = null;
			CursorLoader cl = new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
			return cl;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			
			if (cursor != null && cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				
				String word = cursor.getString( cursor.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION ));
				String dstWord = cursor.getString( cursor.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD));
				while (cursor.moveToNext())
					dstWord += ", " + cursor.getString( cursor.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD));
				
				TextView tvOrigWord = (TextView) mRootView.findViewById( R.id.tvOrigWord );
				TextView tvDestWord = (TextView) mRootView.findViewById( R.id.tvDestWord );
				
				tvOrigWord.setText( word );
				tvDestWord.setText( dstWord );
			}
			
			mCursorAdapter.swapCursor( cursor );
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mCursorAdapter.swapCursor( null );
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		
		LoaderManager lm = getLoaderManager();
		int loaderId = 0;
		lm.initLoader( loaderId, args, mLoaderCallbacks);
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		
		mRootView = inflater.inflate( R.layout.fragment_word_details, null);
		
		mCursorAdapter = new SimpleCursorAdapter( 
				getActivity(), 
				R.layout.fragment_word_details, 
				null, 
				mProjection, 
				new int[] { 0, R.id.tvOrigWord, R.id.tvDestWord },
				0);
		
		/*
		TextView tvWordDetails =  (TextView)v.findViewById( R.id.tvWordDetails );
		
		StringBuilder sb = new StringBuilder();
		sb.append( "Word " + word + "\n");
		sb.append( "OrigLang " + lang + "\n");
		tvWordDetails.setText( sb.toString()) ;
		*/
		
		return mRootView;
	}
}
