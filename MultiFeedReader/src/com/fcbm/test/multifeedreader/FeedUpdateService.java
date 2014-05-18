package com.fcbm.test.multifeedreader;

import com.fcbm.test.multifeedreader.bom.PageInfo;
import com.fcbm.test.multifeedreader.utils.FeedFetch;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class FeedUpdateService extends IntentService {

	private static final String TAG = "FeedUpdateService";
	private static final int UPDATE_CODE = 100;
	private static final int UPDATE_RATE = 1000 * 15;
	
	public FeedUpdateService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent inputIntent) {
		
		PageInfo pageInfo = inputIntent.getExtras().getParcelable( FeedListActivity.KEY_PAGE_INFO );
		
		Bitmap favicon = pageInfo.getFavicon( this );
		
		//Toast.makeText( this, TAG + " startDownload " + url, Toast.LENGTH_LONG).show();
		
		int newFeeds =  FeedFetch.downloadFeedItems( this, pageInfo );
		
		
		//Toast.makeText( this, TAG + " endDownload " + newFeeds, Toast.LENGTH_LONG).show();
		
		if (newFeeds > 0)
		{
			Intent  i = new Intent(this, FeedListActivity.class);
			i.putExtras( inputIntent.getExtras() );
			PendingIntent pi = PendingIntent.getActivity( this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
			Notification n = new NotificationCompat.Builder( this )
				.setTicker("")
				.setSmallIcon( R.drawable.ic_launcher )
				.setLargeIcon( favicon )
				.setContentTitle( "Feed Update : " + newFeeds)
				.setContentText( "url:  " + pageInfo.getUrl() )
				.setContentIntent( pi )
				.setVibrate( new long[] {300, 100, 300, 100, 300, 100, 800, 200, 800, 200, 300, 100, 300, 100, 300, 100})
				.build();
			
			NotificationManager nm = (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE );
			nm.notify( 0 , n);
		}

	}
	
	public static void startPeriodicUpdate(Context ctx, Bundle b)
	{
		if (!isRunning(ctx))
		{
			Toast.makeText( ctx, TAG + " start periodic update" , Toast.LENGTH_LONG).show();
			Intent i = new Intent(ctx, FeedUpdateService.class);
			i.putExtras( b );
			PendingIntent pendingIntent = PendingIntent.getService(  ctx, UPDATE_CODE, i, 0);
			AlarmManager am = (AlarmManager) ctx.getSystemService( Context.ALARM_SERVICE );
			am.setInexactRepeating( AlarmManager.RTC, System.currentTimeMillis(), UPDATE_RATE, pendingIntent);
		}
		else
		{
			Toast.makeText( ctx, TAG + " won't start, it's already running" , Toast.LENGTH_LONG).show();
		}
	}
	
	public static void stopPeriodicUpdate(Context ctx)
	{
		if (isRunning( ctx ))
		{
			Toast.makeText( ctx, TAG + " stop periodic update" , Toast.LENGTH_LONG).show();
			AlarmManager am = (AlarmManager) ctx.getSystemService( Context.ALARM_SERVICE );
			Intent i = new Intent( ctx, FeedUpdateService.class);
			PendingIntent pendingIntent = PendingIntent.getService( ctx, UPDATE_CODE, i, 0);
			am.cancel( pendingIntent );
			pendingIntent.cancel();
		}
		else
		{
			Toast.makeText( ctx, TAG + " won't stop, it's not running" , Toast.LENGTH_LONG).show();
		}
	}
	
	public static boolean isRunning(Context ctx)
	{
		Intent i = new Intent( ctx, FeedUpdateService.class);
		PendingIntent pendingIntent = PendingIntent.getService( ctx, UPDATE_CODE, i, PendingIntent.FLAG_NO_CREATE);
		return (pendingIntent != null);
	}

}
