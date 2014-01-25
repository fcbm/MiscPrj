package com.fcbm.frenchquiz;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public abstract class SingleFragmentActivity extends FragmentActivity {

	protected abstract Fragment getFragment();
	
	protected int getFragmentContainerId()
	{
		return R.id.fragmentContainer;
	}
	
	protected int getLayoutId()
	{
		return R.layout.fragment_container;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView( getLayoutId() );
		
		FragmentManager fm = getSupportFragmentManager(); 
		Fragment f = fm.findFragmentById( getFragmentContainerId() );
		
		if (f == null)
		{
			FragmentTransaction ft = fm.beginTransaction();
			ft.add( getFragmentContainerId(), getFragment());
			ft.commit();
		}
	}
	
}
