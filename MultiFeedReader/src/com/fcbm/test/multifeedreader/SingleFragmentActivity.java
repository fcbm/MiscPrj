package com.fcbm.test.multifeedreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public abstract class SingleFragmentActivity extends FragmentActivity {
	
	protected abstract Fragment createFragment();
	
	protected int getLayoutResId()
	{
		return R.layout.activity_fragment;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState );
		setContentView( getLayoutResId() );
		FragmentManager fm = getSupportFragmentManager();
		
		Fragment fragment = fm.findFragmentById( R.id.fragmentContainer );
		
		if (fragment == null)
		{
			fragment = createFragment();
			FragmentTransaction ft = fm.beginTransaction();
			// Container view ID serves two purposes: 
			// - tells the FM where in the Activity the fragment's View should appear
			// - it is used as unique identifier for a Fragment within the FM's list
			ft.add(R.id.fragmentContainer, fragment);
			// It may seem odd that FragmentManager identifies CrimeFragment using the resource ID of a
			// FrameLayout. Buy identifying a UI fragment by the resource ID of its container view is how
			// the FragmentManager operates
			ft.commit();
		}
	}

}
