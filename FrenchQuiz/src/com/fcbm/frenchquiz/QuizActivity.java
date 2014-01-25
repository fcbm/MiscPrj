package com.fcbm.frenchquiz;

import android.support.v4.app.Fragment;

public class QuizActivity extends SingleFragmentActivity {

	@Override
	protected Fragment getFragment() {
		// TODO Auto-generated method stub
		return QuizFragment.newInstance(null);
	}

}
