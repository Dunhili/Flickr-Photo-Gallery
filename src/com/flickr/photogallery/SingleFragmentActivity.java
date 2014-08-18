package com.flickr.photogallery;

import com.flickr.photogallery.R;

import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Abstract class for activities that use a single fragment. Concrete classes must implement the
 * createFragment() method.
 * @author dunhili
 */
public abstract class SingleFragmentActivity extends FragmentActivity {
	/**
	 * Should create the fragment stored in this activity and return it.
	 * @return returns the fragment stored in this activity
	 */
    protected abstract Fragment createFragment();

    /**
     * When the activity is created, it will create the fragment and then begin using it.
     * @param savedInstanceState bundle that stores any data from other activites or intents
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction().add(R.id.fragmentContainer, fragment).commit();
        }
    }
}
