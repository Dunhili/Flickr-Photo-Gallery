package com.flickr.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Represents a receiver for notifications that will then alert the notification manager.
 * @author dunhili
 */
public class NotificationReceiver extends BroadcastReceiver {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
	private static final String TAG = "NotificationReceiver";
	
    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * When a notification is received, notifies the notification manager.
	 * @param context context from which the notification was received
	 * @param intent intent sent to the notification
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "received result: " + getResultCode());
		if (getResultCode() != Activity.RESULT_OK) {
			// A foreground activity cancelled the broadcast
			return;
		}
		
		int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
		Notification notification = (Notification)intent.getParcelableExtra("NOTIFICATION");
		NotificationManager notificationManager = 
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(requestCode, notification);
	}
}
