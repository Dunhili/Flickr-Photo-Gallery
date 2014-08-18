package com.flickr.photogallery;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/**
 * Intent service that handles the alarm for the notifications.
 * @author dunhili
 */
public class PollService extends IntentService {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
	private static final String TAG = "PollService";
	private static final int POLL_INTERVAL = 1000 * 60 * 5; // 5 minutes
	public static final String PREF_IS_ALARM_ON = "isAlarmOn";
	public static final String ACTION_SHOW_NOTIFICATION = "com.flickr.photogallery.SHOW_NOTIFICATION";
	public static final String PERM_PRIVATE = "com.flickr.photogallery.PRIVATE";
	
    ////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////
	/**
	 * Default constructor.
	 */
	public PollService() {
		super(TAG);
	}
	
    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Sets or cancels the service alarm for the notifications.
	 * @param context context that contains the alarm
	 * @param isOn should the alarm keep going or stop
	 */
	public static void setServiceAlarm(Context context, boolean isOn) {
		Intent intent = new Intent(context, PollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);
		
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (isOn) {
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
		
		PreferenceManager.getDefaultSharedPreferences(context)
			.edit().putBoolean(PollService.PREF_IS_ALARM_ON, isOn).commit();
	}
	
	/**
	 * Returns true if the alarm is currently on, false otherwise
	 * @param context context that contains the alarm
	 * @return true if the alarm is on, false otherwise
	 */
	public static boolean isServiceAlarmOn(Context context) {
		Intent intent = new Intent(context, PollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
		return pi != null;
	}
	
    ////////////////////////////////////////////////////////////////////
    // Protected Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Called when an intent is sent, handles creating and showing the notification.
	 * @param intent intent sent to the poll service
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		@SuppressWarnings("deprecation")
		boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;
		if (!isNetworkAvailable) {
			return;
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String query = prefs.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
		String lastResultId = prefs.getString(FlickrFetchr.PREF_LAST_RESULT_ID, null);
		
		ArrayList<Photo> items;
		if (query != null) {
			items = new FlickrFetchr().search(query);
		} else {
			items = new FlickrFetchr().getRecentPhotos(1);
		}
		
		if (items.isEmpty()) {
			return;
		}
		
		String resultId = items.get(0).getId();
		if (!resultId.equals(lastResultId)) {
			Log.i(TAG, "Got a new result: " + resultId);
			
			Resources resources = getResources();
			PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, PhotoGalleryActivity.class), 0);
			
			Notification notification = new NotificationCompat.Builder(this)
				.setTicker(resources.getString(R.string.new_pictures_title))
				.setSmallIcon(android.R.drawable.ic_menu_report_image)
				.setContentTitle(resources.getString(R.string.new_pictures_title))
				.setContentText(resources.getString(R.string.new_pictures_text))
				.setContentIntent(pi)
				.setAutoCancel(true)
				.build();
			
			showBackgroundNotification(0, notification);
		}
		
		prefs.edit().putString(FlickrFetchr.PREF_LAST_RESULT_ID, resultId).commit();
		Log.i(TAG, "Received an intent: " + intent);
	}
	
    ////////////////////////////////////////////////////////////////////
    // Package Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Shows the notification.
	 * @param requestCode code for the ordered broadcast
	 * @param notification notification that should be shown
	 */
	/* package */ void showBackgroundNotification(int requestCode, Notification notification) {
		Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
		intent.putExtra("REQUEST_CODE", requestCode);
		intent.putExtra("NOTIFICATION", notification);
		sendOrderedBroadcast(intent, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
	}
}
