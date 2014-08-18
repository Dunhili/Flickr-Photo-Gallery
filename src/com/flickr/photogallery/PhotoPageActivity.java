package com.flickr.photogallery;

import android.support.v4.app.Fragment;

/**
 * Activity for the photo page fragment.
 * @author dunhili
 */
public class PhotoPageActivity extends SingleFragmentActivity {
    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates the photo page fragment and returns it.
	 * @return photo page fragment
	 */
	@Override
	public Fragment createFragment() {
		return new PhotoPageFragment();
	}
}
