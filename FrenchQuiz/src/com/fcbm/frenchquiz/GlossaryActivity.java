package com.fcbm.frenchquiz;

import android.support.v4.app.Fragment;

public class GlossaryActivity extends SingleFragmentActivity {

	@Override
	protected Fragment getFragment() {
		// TODO Auto-generated method stub
		return GlossaryFragment.newInstance(null);
	}

}
