package com.example.searchapp;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		setContentView( R.layout.activity_main ); 

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, MainFragment.newInstance(null)).commit();
		}
		
		parseIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);
		parseIntent(intent);
	}
	
	private void parseIntent(Intent intent)
	{
		Log.d(TAG, "parseIntent " + intent.getAction());
		
		PreferenceManager.getDefaultSharedPreferences( this )
			.edit()
			.putString( MainFragment.KEY_QUERY, null)
			.commit();
		
		if (intent.getAction().equals(Intent.ACTION_SEARCH))
		{
			String searchQuery = intent.getStringExtra( SearchManager.QUERY );
			Log.d(TAG, "Query: " + searchQuery);
			
			PreferenceManager.getDefaultSharedPreferences( this )
				.edit()
				.putString( MainFragment.KEY_QUERY, searchQuery)
				.commit();
			
			MainFragment mf = (MainFragment) getSupportFragmentManager().findFragmentById( R.id.container );
			
			if (mf != null)
			{
				mf.refreshItems();
			}
		}
		else if (intent.getAction().equals(Intent.ACTION_VIEW))
		{
			Log.d(TAG, "Got ACTION_VIEW : " + intent.getDataString());
			Bundle args = new Bundle();
			args.putString( DetailsFragment.KEY_ID, intent.getDataString() );
			DetailsFragment df = DetailsFragment.newInstance( args );
			getSupportFragmentManager().beginTransaction().replace( R.id.container , df).addToBackStack(null).commit();
		}
	}

}
