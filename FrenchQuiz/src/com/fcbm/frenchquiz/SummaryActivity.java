package com.fcbm.frenchquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class SummaryActivity extends SingleFragmentActivity {

	@Override
	protected Fragment getFragment() {
		Intent i = getIntent();
		Bundle args = i.getExtras();
		return SummaryFragment.newInstance(args);
	}

}
