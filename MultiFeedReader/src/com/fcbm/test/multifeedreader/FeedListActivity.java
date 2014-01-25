package com.fcbm.test.multifeedreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;

public class FeedListActivity extends SingleFragmentActivity implements FeedListFragment.ListItemClickListener {

	private static final String TAG = "FeedListActivity";
	
	public static final String KEY_URL = "key_url";
	public static final String KEY_TITLE = "key_title";
	public static final String KEY_DESCRIPTION = "key_description";
	public static final String KEY_COLOR = "key_color";
	public static final String KEY_ICON = "key_icon";
	
	@Override
	protected int getLayoutResId()
	{
		return R.layout.masterdetails_activity;
	}
	
	@Override
	protected Fragment createFragment() {
		
		Intent i = getIntent();
		
		String title = i.getStringExtra(KEY_TITLE);
		String description = i.getStringExtra(KEY_DESCRIPTION);
		int iconId = i.getIntExtra(KEY_ICON, R.drawable.ic_launcher);
		String url = i.getStringExtra(KEY_URL);
		
		return FeedListFragment.newInstance(title, description, url, iconId);
	}
	
	/*
	@Override
	protected void onRestoreInstanceState(Bundle outState)
	{
		// TODO: move this
		FragmentManager fm = getSupportFragmentManager();
		Fragment oldDetails = fm.findFragmentById( R.id.fragmentDetails );
		if (oldDetails != null && findViewById(R.id.fragmentDetails) == null)
		{
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(oldDetails);
			ft.commit();
			Log.d(TAG, "Destroying.. FoundOldDetails and remove");
		}
		else
		{
			Log.d(TAG, "Destroying.. NotFoundOldDetails");
		}
		super.onRestoreInstanceState(outState);
	}*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Intent i = getIntent();
		
		String title = i.getStringExtra(KEY_TITLE);
		
		setTitle(title);
	}

	@Override
	public void onListItemClicked(Bundle b) {
		
		
		String url = b.getString(FeedListFragment.KEY_URL);
		String description = b.getString(FeedListFragment.KEY_DESCRIPTION);
		
		if (findViewById(R.id.fragmentDetails) != null)
		{
			FragmentManager fm = getSupportFragmentManager();
			Fragment oldDetails = fm.findFragmentById( R.id.fragmentDetails );
			Fragment newDetails = PageFragment.newInstance(url, description);
			
			FragmentTransaction ft = fm.beginTransaction();
			if (oldDetails != null) { ft.remove( oldDetails); }
			ft.add( R.id.fragmentDetails , newDetails );
			ft.commit();
		}
		else
		{
			Intent i = new Intent(this, PageActivity.class);
			i.putExtra(PageFragment.URL_KEY, url);
			i.putExtra(PageFragment.DESCRIPTION_KEY, description);
			startActivity(i);
		}
	}

}
