package com.flickr.photogallery;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

public class PhotoGalleryFragment extends VisibleFragment {
	private static final String TAG = "PhotoGalleryFragment";
	
    private GridView mGridView;
    private ArrayList<GalleryItem> mItems;
    private ThumbnailDownloader<ImageView> mThumbnailThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setRetainInstance(true);
        setHasOptionsMenu(true);
        ActionBar titleBar = getActivity().getActionBar();
        if (!titleBar.isShowing()) {
        	titleBar.show();
        }
        updateItems();
        
        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
        	public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
        		if (isVisible()) {
        			imageView.setImageBitmap(thumbnail);
        		}
        	}
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started.");
    }
    
    @TargetApi(11)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	super.onCreateOptionsMenu(menu, inflater);
    	inflater.inflate(R.menu.fragment_photo_gallery, menu);
    	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		// Pull out the search view
    		MenuItem searchItem = menu.findItem(R.id.menu_item_search);
    		SearchView searchView = (SearchView) searchItem.getActionView();
    		
    		// Get the data from our searchable.xml as a SearchableInfo
    		SearchManager searchManager = (SearchManager)getActivity().getSystemService(Context.SEARCH_SERVICE);
    		ComponentName name = getActivity().getComponentName();
    		SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
    		
    		searchView.setSearchableInfo(searchInfo);
    	}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.menu_item_search:
    		getActivity().onSearchRequested();
    		return true;
    	case R.id.menu_item_clear:
    		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
    			.putString(FlickrFetchr.PREF_SEARCH_QUERY, null).commit();
    		updateItems();
    		return true;
    	case R.id.menu_item_toggle_polling:
    		boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
    		PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
    		
    		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    			getActivity().invalidateOptionsMenu();
    		}
    		
    		return true;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    	super.onPrepareOptionsMenu(menu);
    	
    	MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
    	toggleItem.setTitle(PollService.isServiceAlarmOn(getActivity()) 
    			? R.string.stop_polling : R.string.start_polling);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        
        mGridView = (GridView)view.findViewById(R.id.gridView);
        setupAdapter();
        
        mGridView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> gridView, View view, int pos, long id) {
        		GalleryItem item = mItems.get(pos);
        		Uri photoPageUri = Uri.parse(item.getPhotoPageUrl());
        		Intent intent = new Intent(getActivity(), PhotoPageActivity.class);
        		intent.setData(photoPageUri);
        		startActivity(intent);
        	}
        });
        
        return view;
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	mThumbnailThread.quit();
    	Log.i(TAG, "Background thread destroyed.");
    }
    
    @Override
    public void onDestroyView() {
    	super.onDestroyView();
    	mThumbnailThread.clearQueue();
    }
    
    public void updateItems() {
    	new FetchItemsTask().execute();
    }
    
    /* package */ void setupAdapter() {
        if (getActivity() == null || mGridView == null) return;
        
        if (mItems != null) {
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Void... params) {
        	Activity activity = getActivity();
        	if (activity == null) {
        		return new ArrayList<GalleryItem>();
        	}
        	
        	String query = PreferenceManager.getDefaultSharedPreferences(activity)
        			.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        	if (query != null) {
        		return new FlickrFetchr().search(query);
        	} else {
        		return new FlickrFetchr().fetchItems(1);
        	}
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }
    
    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
    	public GalleryItemAdapter(ArrayList<GalleryItem> items) {
    		super(getActivity(), 0, items);
    	}
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		if (convertView == null) {
    			convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
    		}
    		
    		ImageView imageView = (ImageView) convertView.findViewById(R.id.gallery_item_imageView);
    		imageView.setImageResource(R.drawable.blank);
    		GalleryItem item = getItem(position);
    		mThumbnailThread.queueThumbnail(imageView, item.getUrl());
    		return convertView;
    	}
    }
}
