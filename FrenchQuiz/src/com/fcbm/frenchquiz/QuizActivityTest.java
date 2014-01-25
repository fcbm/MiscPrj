package com.fcbm.frenchquiz;

import android.support.v4.app.Fragment;

public class QuizActivityTest extends SingleFragmentActivity {

	@Override
	protected Fragment getFragment() {
		// TODO Auto-generated method stub
		return QuizFragmentTest.newInstance(null);
	}

}
