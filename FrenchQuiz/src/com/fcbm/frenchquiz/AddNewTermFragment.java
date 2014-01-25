package com.fcbm.frenchquiz;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AddNewTermFragment extends Fragment {

	static AddNewTermFragment newInstance(Bundle args)
	{
		AddNewTermFragment f = new AddNewTermFragment();
		f.setArguments(args);
		return f;
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
	{
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate( R.layout.fragment_add_new_term, null);
		
		return v;
	}
	
}
