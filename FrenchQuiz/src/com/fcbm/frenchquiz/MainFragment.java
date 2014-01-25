package com.fcbm.frenchquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainFragment extends Fragment {

	static MainFragment newInstance(Bundle args)
	{
		MainFragment mf = new MainFragment();
		mf.setArguments( args );
		return mf;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate(R.layout.fragment_main, null);
		
		Button btnListTerms = (Button) v.findViewById( R.id.btnListTerms );
		btnListTerms.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent( getActivity(), GlossaryActivity.class);
				startActivity( i ); 
			}
		});

		Button btnStartQuizTest = (Button) v.findViewById( R.id.btnStartQuizTest );
		btnStartQuizTest.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent( getActivity(), QuizActivityTest.class);
				startActivity( i ); 
			}
		});

		Button btnStartQuiz = (Button) v.findViewById( R.id.btnStartQuiz );
		btnStartQuiz.setOnClickListener( new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent( getActivity(), QuizActivity.class);
				startActivity( i ); 
			}
		});		
		return v;
	}
}
