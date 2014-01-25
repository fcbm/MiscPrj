package com.fcbm.frenchquiz;

import android.content.Intent;
import android.support.v4.app.Fragment;

public class WordDetailsActivity extends SingleFragmentActivity {

	@Override
	protected Fragment getFragment() {
		// TODO Auto-generated method stub
		Intent i = getIntent();
		return WordDetailsFragment.newInstance(i.getExtras());
	}

}
