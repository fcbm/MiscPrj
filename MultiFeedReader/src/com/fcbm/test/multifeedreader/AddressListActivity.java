package com.fcbm.test.multifeedreader;

import android.support.v4.app.Fragment;

public class AddressListActivity extends SingleFragmentActivity {


	@Override
	protected Fragment createFragment() {
		return new AddressListFragment();
	}


}
