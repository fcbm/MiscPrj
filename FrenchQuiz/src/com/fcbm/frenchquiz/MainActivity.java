package com.fcbm.frenchquiz;

import android.support.v4.app.Fragment;
import android.view.Menu;

public class MainActivity extends SingleFragmentActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected Fragment getFragment() {
		return new MainFragment();
	}

}
