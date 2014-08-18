package com.flickr.photogallery;

import com.flickr.photogallery.R;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Activity that creates the fragment for the Photo Gallery. Also sends the intents for searches.
 * @author dunhili
 */
public class PhotoGalleryActivity extends SingleFragmentActivity {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
	private static final String TAG = "PhotoGalleryActivity";
	
    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Creates the Photo Gallery fragment.
	 * @return photo gallery fragment
	 */
    @Override
    public Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
    
    /**
     * When an intent is received, if it's an intent for searching, then it grabs the search query and
     * puts it in the preference manager.
     * @param intent intent sent to this activity
     */
    @Override
    public void onNewIntent(Intent intent) {
    	PhotoGalleryFragment fragment = (PhotoGalleryFragment) 
    			getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
    	if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_SEARCH)) {
    		String query = intent.getStringExtra(SearchManager.QUERY);
    		Log.i(TAG, "Received a new search query: " + query);
    		
    		PreferenceManager.getDefaultSharedPreferences(this).edit()
    			.putString(FlickrFetchr.PREF_SEARCH_QUERY, query).commit();
    	}
    	
    	fragment.updateItems();
    }
}
