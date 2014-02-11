package com.fcbm.frenchquiz;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class QuizFragmentTest extends ListFragment {

	public static QuizFragmentTest newInstance(Bundle args)
	{
		QuizFragmentTest qf = new QuizFragmentTest();
		qf.setArguments(args);
		return qf;
	}
	
	private String[] mProjection = new String[] {
			LanguagesProvider.GLOSSARY_COL_ID,
			LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION,
			LanguagesProvider.GLOSSARY_COL_WORD
	} ;
	private QuizCursorAdapter mAdapter = null;
	private CursorLoader mCursorLoader = null;
	private LoaderCallbacks<Cursor> mCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
			String[] selectionArgs = new String[] { "fr", "3", "it" };
			mCursorLoader = new CursorLoader( 
					getActivity(), 
					LanguagesProvider.authorityRandomWord, 
					mProjection, 
					null, 
					selectionArgs, 
					null);
			return mCursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
			mAdapter.swapCursor(arg1);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor( null );
		}
	};
	
	private class QuizCursorAdapter extends SimpleCursorAdapter
	{

		public QuizCursorAdapter(Context context) {
			super(context, R.layout.row_glossary_item, null, mProjection, null, 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup root)
		{
			//super.getView(position, convertView, null);
			
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate( R.layout.row_glossary_item , null);
			}
			
			Cursor c = (Cursor) getItem(position);
			
			String word = null;
			String translations = null;
			if (c != null)
			{
				word = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION ));
				translations = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD ));
			}
			
			TextView tvWord = (TextView) convertView.findViewById( R.id.tvGlossaryWord );
			tvWord.setText( word );
			TextView tvTranslation = (TextView) convertView.findViewById( R.id.tvGlossaryLang );
			tvTranslation.setText( translations );
			
			return convertView;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		LoaderManager lm = getLoaderManager();
		int loaderId = 0;
		Bundle loaderArgs = null;
		lm.initLoader(loaderId, loaderArgs , mCallbacks);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//View v = super.onCreateView( inflater, container, savedInstanceState);
		View v = inflater.inflate( R.layout.fragment_quiz_test, null);
		
		mAdapter = new QuizCursorAdapter( getActivity() );
		setListAdapter( mAdapter );
		
		Button buttonNext = (Button) v.findViewById( R.id.btnNextQuestionTest );
		buttonNext.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				getLoaderManager().restartLoader( 0 , null, mCallbacks);
			}
		});
		return v;
	}
	
	
}
