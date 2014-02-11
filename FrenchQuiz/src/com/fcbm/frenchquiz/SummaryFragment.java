package com.fcbm.frenchquiz;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SummaryFragment extends Fragment {

	public static final String KEY_MATCH_MODEL = "key_match_model";

	private ArrayAdapter< String > mAdapter = null;
	private String[] mResults = null;
	private MatchModel mMatchModel = null;
	private Context mGlobalCtx = null;
	
	public static SummaryFragment newInstance(Bundle args)
	{
		SummaryFragment sf = new SummaryFragment();
		sf.setArguments(args);
		return sf;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		//super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate( R.layout.fragment_summary, null);
		
		mMatchModel = getArguments().getParcelable( KEY_MATCH_MODEL );
		mResults = mMatchModel.getSummary();
		int correctAnswer = mMatchModel.getCorrectAnswers();
		int totQuestions = mResults.length;
		float percCorrect = ((float)((float)correctAnswer/totQuestions))*100;
		
		String str  = "Summary\nRight answers : " + correctAnswer + "\\" + totQuestions + "\n" + percCorrect + "%";
		
		mAdapter = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_list_item_1, mResults);

		mGlobalCtx = getActivity().getApplicationContext();
		
		TextView tvSummary = (TextView) v.findViewById( R.id.tvSummary );
		tvSummary.setText( str );

		
		ListView lvSummary = (ListView) v.findViewById( R.id.lvSummary );
		lvSummary.setAdapter( mAdapter );
		
		return v;
	}
	
	@Override
	public void onPause()
	{
		super.onPause();

		new Thread( new Runnable() {
			@Override
			public void run() {
				updateResults();
			}
		}).start();
	}
	
	private void updateResults()
	{
		int size = mMatchModel.getNumberOfQuestion();
		for (int i = 0; i < size; i++ )
		{
			final QuestionModel qm = mMatchModel.getQuestionAt( i );
			ContentValues cv = new ContentValues();
			cv.put( LanguagesProvider.TRANS_COL_NUMBER_OF_ATTEMTS, qm.getNumberOfAttempts() + 1);
			if (qm.isAnswered() && qm.isCorrect())
				cv.put( LanguagesProvider.TRANS_COL_NUMBER_OF_SUCCESS, qm.getNumberOfSuccess() + 1);
			
			mGlobalCtx.getContentResolver().update( LanguagesProvider.authorityTranslation, cv, LanguagesProvider.TRANS_COL_ID + "=?",  new String[] { ""+qm.getId() });
		}
	}
}
