package com.fcbm.frenchquiz;

import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class QuizFragment extends Fragment {

	private static final String TAG = "QuizFragment";
	
	public static QuizFragment newInstance(Bundle args)
	{
		QuizFragment qf = new QuizFragment();
		qf.setArguments(args);
		return qf;
	}
	
	private View mRootView = null;
	private static final String KEY_MATCH_MODEL = "key_match_model";
	
	private String[] mProjection = new String[] {
			LanguagesProvider.GLOSSARY_COL_ID,
			LanguagesProvider.GLOSSARY_COL_WORD_TRANSLATION,
			LanguagesProvider.GLOSSARY_COL_WORD
	} ;
	
	private CursorLoader mCursorLoader = null;
	private int mChoices = 3;
	private Button[] mBtns = null;
	private int mMaxQuestions = 5;
	private TextView mTvQuestion = null;
	private MatchModel mMatchModel = null;
	
	private LoaderCallbacks<Cursor> mCallbacks = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
			String[] selectionArgs = new String[] { "fr", "50", "it" };
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
			mMatchModel = new MatchModel( c , mMaxQuestions, "fr", "it", MatchModel.QUIZ_TYPE_MULTIPLE_CHOICE, false);
			updateQuestionUI();
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
		}
	};

	private void updateQuestionUI()
	{
		QuestionModel qm = mMatchModel.getCurrentQuestion();
		
		String word = qm.getOrgWord();
		
		mTvQuestion.setText( "Which is the meaning of :\n"  + word );
		//mRightAnswer = qm.getCorrectIndex();
		for (int i = 0; i < mChoices ; i++)
		{
			mBtns[i].setText(qm.getDstWords()[i]);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable( KEY_MATCH_MODEL, mMatchModel);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState == null)
		{
			LoaderManager lm = getLoaderManager();
			int loaderId = 0;
			Bundle loaderArgs = null;
			lm.initLoader(loaderId, loaderArgs , mCallbacks);
		}
		else
		{
			mMatchModel = savedInstanceState.getParcelable( KEY_MATCH_MODEL );
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView( inflater, container, savedInstanceState);
		
		mRootView = getActivity().getLayoutInflater().inflate( R.layout.fragment_quiz , null);
		
		//final String correctAnswer = "This is the correct answer!";
		//final String wrongAnswer = "This is the wrong answer!";
		
		mBtns = new Button[mChoices];
		mBtns[0] = (Button) mRootView.findViewById( R.id.btnAnswer1 );
		mBtns[1] = (Button) mRootView.findViewById( R.id.btnAnswer2 );
		mBtns[2] = (Button) mRootView.findViewById( R.id.btnAnswer3 );
		
		for (int i = 0; i < mChoices; i++)
		{
			mBtns[i].setTag(i);
			mBtns[i].setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
					//String answer = wrongAnswer;
					boolean correct = false;
					int rightAnswer = mMatchModel.getCurrentQuestion().getCorrectIndex();
					if (rightAnswer == (Integer)v.getTag() )
					{
						//answer = correctAnswer;
						//mCorrectAnswers++;
						correct = true;
					}
					mMatchModel.updateCurrentQuestion(correct);
					setButtonsEnabled( false );
					//Toast.makeText( getActivity(), answer, Toast.LENGTH_LONG).show();
				}
			});
		}
		Button buttonNext = (Button) mRootView.findViewById( R.id.btnNextQuestion );
		buttonNext.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMatchModel.getNextQuestion() != null)
				{
					updateQuestionUI();
					setButtonsEnabled( true );
				}
				else
				{
					setButtonsEnabled( false );
					//Toast.makeText( getActivity(), "Correct answers : " + mMatchModel.getCorrectAnswers() + "/" + mMatchModel.getNumberOfQuestion(), Toast.LENGTH_LONG).show();
					Intent i = new Intent(getActivity(), SummaryActivity.class);
					Bundle b = new Bundle();
					b.putParcelable( SummaryFragment.KEY_MATCH_MODEL, mMatchModel);
					i.putExtras( b );
					startActivity( i );
					getActivity().finish();
				}
			}
		});
		
		mTvQuestion = (TextView) mRootView.findViewById( R.id.tvQuestion );
		
		if (mMatchModel != null)
		{
			updateQuestionUI();
			setButtonsEnabled( !mMatchModel.getCurrentQuestion().isAnswered() );
		}
		
		return mRootView;
	}
	
	private void setButtonsEnabled(boolean isEnabled)
	{
		for (int i = 0; i < mChoices; i++)
		{
			mBtns[i].setEnabled(isEnabled);
		}
		
	}
}
