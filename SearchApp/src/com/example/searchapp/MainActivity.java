package com.example.searchapp;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new MainFragment()).commit();
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
		
		if (intent.getAction().equals(Intent.ACTION_SEARCH))
		{
			String searchQuery = intent.getStringExtra( SearchManager.QUERY );
			Log.d(TAG, "Query: " + searchQuery);
		}
	}

}
