package com.fcbm.test.multifeedreader;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PageFragment extends Fragment {

	public static final String URL_KEY = "url_key";
	public static final String DESCRIPTION_KEY = "description_key";
	
	private static final String TAG = "PageFragment";
	private String mUrl;
	private String mDescription;
	
	public static Fragment newInstance(String url, String description)
	{
		Fragment f = new PageFragment();
		Bundle b = new Bundle();
		b.putString(URL_KEY, url);
		b.putString(DESCRIPTION_KEY, description);
		f.setArguments(b);
		return f;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
		
		mUrl = //"http://ftr.fivefilters.org/makefulltextfeed.php?" + 
		getArguments().getString(URL_KEY);
		mDescription = getArguments().getString(DESCRIPTION_KEY);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate(R.layout.page_fragment, container, false);
		
		WebView wv = (WebView) v.findViewById( R.id.webViewPage );
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebViewClient( new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				return false;
			}
		});	
		
		if (mUrl != null)
		{
			Log.d(TAG, "Loading url : " + mUrl);
			wv.loadUrl(mUrl);
		}
		else
		{
			Log.d(TAG, "Loading desc : " + mDescription);
			wv.loadData(mDescription, "text/html", "UTF-8");
		}
		
		return v;
	}
}
