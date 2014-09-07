package com.example.searchapp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailsFragment extends Fragment {

	public static final String KEY_ID = "key_id";
	
	private static final int ID_LOAD_MOVIE = 101;
	private static final String TAG = "DetailsFragment";
	
	private TextView mTvTitle = null;
	private TextView mTvDirector = null;
	
	private LoaderCallbacks<Cursor> mCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
			String[] projection = new String[] { TheProvider.COL_ID, TheProvider.COL_TITLE, TheProvider.COL_DIRECTOR };
			String selection = null;
			String[] selectionArgs = null;
			String sortOrder = null;
			
			if (args != null && args.containsKey( KEY_ID))
			{
				selection = TheProvider.COL_ID + " = " + args.getString( KEY_ID );
			}
			
			return new CursorLoader(getActivity(), TheProvider.AUTHORITY, projection, selection, selectionArgs, sortOrder);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
			
			Log.d(TAG, "Cursor size = " + c.getCount() + " colCount " + c.getColumnCount() + " toString " + c.toString());
			c.moveToFirst();
			
			if (mTvTitle != null)
			{
				mTvTitle.setText( c.getString( c.getColumnIndex( TheProvider.COL_TITLE )));
			}
			if (mTvDirector != null)
			{
				mTvDirector.setText( c.getString( c.getColumnIndex( TheProvider.COL_DIRECTOR )));
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
		}
	};
	
	private DetailsFragment()
	{}
	
	public static DetailsFragment newInstance(Bundle args)
	{
		DetailsFragment df = new DetailsFragment();
		df.setArguments(args);
		return df;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate( R.layout.fragment_details, container,false);
		mTvTitle = (TextView)v.findViewById( R.id.tvTitle);
		mTvDirector = (TextView)v.findViewById( R.id.tvDirector);
		return v;
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
		Bundle args = getArguments();
		LoaderManager lm = getLoaderManager();
		
		if (lm.getLoader(ID_LOAD_MOVIE) == null)
		{
			getLoaderManager().initLoader(ID_LOAD_MOVIE, args, mCallbacks);
		}
		else
		{
			getLoaderManager().restartLoader(ID_LOAD_MOVIE, args, mCallbacks);
		}
	}
}
