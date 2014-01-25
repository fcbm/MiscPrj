package com.fcbm.frenchquiz;


import java.util.Random;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QuizFragment extends Fragment {

	private static final String TAG = "QuizFragment";
	
	public static QuizFragment newInstance(Bundle args)
	{
		QuizFragment qf = new QuizFragment();
		qf.setArguments(args);
		return qf;
	}
	
	private View mRootView = null;
	
	private String[] mProjection = new String[] {
			LanguagesProvider.GLOSSARY_COL_ID,
			LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION,
			LanguagesProvider.GLOSSARY_COL_WORD
	} ;
	
	private CursorLoader mCursorLoader = null;
	private SimpleCursorAdapter mAdapter;
	private Button mBtn1, mBtn2, mBtn3;
	private int mRightAnswer = 0;
	private TextView mTvQuestion = null;
	private LoaderCallbacks<Cursor> mCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
			String[] selectionArgs = new String[] { "it", "3", "fr" };
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
		public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
			
			c.moveToFirst();
			Log.d(TAG , "Cursor GetCount " + c.getCount());
			Log.d(TAG , "Cursor GetColumnCount " + c.getColumnCount());
			String word = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION ));
			String opt1 = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD ));
			Log.d(TAG , "W1 " + c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION )) +
						"W1T " + c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD )));
			c.moveToNext();
			String opt2 = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD ));
			Log.d(TAG , "W1 " + c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION )) +
						"W1T " + c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD )) );

			c.moveToNext();
			String opt3 = c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD ));
			Log.d(TAG , "W1 " + c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION )) +
						"W1T " + c.getString( c.getColumnIndex( LanguagesProvider.GLOSSARY_COL_WORD )));
			
			mTvQuestion.setText( "Which is the meaning of :\n"  + word );
			mRightAnswer = new Random().nextInt(3);
			if (mRightAnswer == 0)
			{
				mBtn1.setText( opt1 );
				mBtn2.setText( opt2 );
				mBtn3.setText( opt3 );
			}
			else if (mRightAnswer == 1)
			{
				mBtn1.setText( opt2 );
				mBtn2.setText( opt1 );
				mBtn3.setText( opt3 );
			}
			else if (mRightAnswer == 2)
			{
				mBtn1.setText( opt2 );
				mBtn2.setText( opt3 );
				mBtn3.setText( opt1 );
			}
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
		
		LoaderManager lm = getLoaderManager();
		int loaderId = 0;
		Bundle loaderArgs = null;
		lm.initLoader(loaderId, loaderArgs , mCallbacks);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView( inflater, container, savedInstanceState);
		
		mRootView = getActivity().getLayoutInflater().inflate( R.layout.fragment_quiz , null);
		
		mAdapter = new SimpleCursorAdapter(getActivity(), 0, null, mProjection, null, 0);
		
		final String correctAnswer = "This is the correct answer!";
		final String wrongAnswer = "This is the wrong answer!";
		
		mBtn1 = (Button) mRootView.findViewById( R.id.btnAnswer1 );
		mBtn1.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				String answer = wrongAnswer;
				if (mRightAnswer == 0)
				{
					answer = correctAnswer;
				}
				Toast.makeText( getActivity(), answer, Toast.LENGTH_LONG).show();
			}
		});
		mBtn2 = (Button) mRootView.findViewById( R.id.btnAnswer2 );
		mBtn2.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				String answer = wrongAnswer;
				if (mRightAnswer == 1)
				{
					answer = correctAnswer;
				}
				Toast.makeText( getActivity(), answer, Toast.LENGTH_LONG).show();

			}
		});

		mBtn3 = (Button) mRootView.findViewById( R.id.btnAnswer3 );
		mBtn3.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				String answer = wrongAnswer;
				if (mRightAnswer == 2)
				{
					answer = correctAnswer;
				}
				Toast.makeText( getActivity(), answer, Toast.LENGTH_LONG).show();

			}
		});

		Button buttonNext = (Button) mRootView.findViewById( R.id.btnNextQuestion );
		buttonNext.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				getLoaderManager().restartLoader( 0 , null, mCallbacks);
			}
		});
		
		mTvQuestion = (TextView) mRootView.findViewById( R.id.tvQuestion );
		return mRootView;
	}
	
	
}
