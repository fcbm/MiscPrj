package com.fcbm.test.multifeedreader;

import android.content.Intent;
import android.support.v4.app.Fragment;

public class PageActivity extends SingleFragmentActivity {

	@Override
	protected Fragment createFragment() {
		Intent i = getIntent();
		String url = i.getExtras().getString( PageFragment.URL_KEY);
		String description = i.getExtras().getString( PageFragment.DESCRIPTION_KEY);
		return PageFragment.newInstance(url, description);
	}

}
