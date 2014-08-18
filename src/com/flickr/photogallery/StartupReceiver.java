package com.flickr.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Receiver for the alarm for searching images.
 * @author dunhili
 */
public class StartupReceiver extends BroadcastReceiver {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
	private static final String TAG = "StartupReceiver";
	
    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * When an intent is received, calls the service alarm for the PollService.
	 * @param context context from which the intent was received
	 * @param intent intent sent to the receiver
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Received broadcast intent: " + intent.getAction());
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isOn = prefs.getBoolean(PollService.PREF_IS_ALARM_ON, false);
		PollService.setServiceAlarm(context, isOn);
	}
}
