package com.flickr.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Represents a fragment that is seen by the user.
 * @author dunhili
 */
public abstract class VisibleFragment extends Fragment {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
	public static final String TAG = "VisibleFragment";
	
	private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// If we receive this, we're visible, so cancel the notification
			Log.i(TAG, "canceling notification");
			setResultCode(Activity.RESULT_CANCELED);
		}
	};
	
    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * When the fragment is resumed, registers the notification receiver.
	 */
	@Override
	public void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
		getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
	}
	
	/**
	 * When the fragment is paused, stops the notification receiver.
	 */
	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mOnShowNotification);
	}
}
