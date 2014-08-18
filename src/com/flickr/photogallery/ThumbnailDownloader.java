package com.flickr.photogallery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

/**
 * Handles downloading the images from the list of images.
 * @author dunhili
 *
 * @param <Token>
 */
public class ThumbnailDownloader<Token> extends HandlerThread {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOAD = 0;
	
	private static LruCache<String, Bitmap> cache;
	
	private Handler mHandler;
	private Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
	private Handler mResponseHandler;
	private Listener<Token> mListener;
	
	public interface Listener<Token> {
		void onThumbnailDownloaded(Token token, Bitmap thumbnail);
	}
	
    ////////////////////////////////////////////////////////////////////
    // Constructors
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Comprehensive constructor, also initializes the cache.
	 * @param responseHandler handler for the thumbnail that handles retrieving images
	 */
	public ThumbnailDownloader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
		
		// Calculate the largest amount of memory used and use 1/8th of it
	    final int cacheSize = ((int) (Runtime.getRuntime().maxMemory() / 1024)) / 8;
		cache = new LruCache<String, Bitmap>(cacheSize);
	}
	
    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Adds the thumbnail to the list of images to download on the handler.
	 * @param token contains data about the image
	 * @param url url for the image
	 */
	public void queueThumbnail(Token token, String url) {
		Log.i(TAG, "Got an URL: " + url);
		requestMap.put(token, url);
		mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
	}
	
	/**
	 * Sets the listener for the downloader.
	 * @param listener new listener for the downloader
	 */
	public void setListener(Listener<Token> listener) {
		mListener = listener;
	}
	
	/**
	 * Removes any messages from the queue that haven't been handled and clears the requests.
	 */
	public void clearQueue() {
		mHandler.removeMessages(MESSAGE_DOWNLOAD);
		requestMap.clear();
	}
	
    ////////////////////////////////////////////////////////////////////
    // Protected Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * When the looper is prepared, creates a new message handler for the given message.
	 */
	@SuppressLint("HandlerLeak")
	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_DOWNLOAD) {
					@SuppressWarnings("unchecked")
					Token token = (Token)msg.obj;
					Log.i(TAG, "Got a request for url: " + requestMap.get(token));
					handleRequest(token);
				}
			}
		};
	}
	
    ////////////////////////////////////////////////////////////////////
    // Private Methods
    ////////////////////////////////////////////////////////////////////
	
	/**
	 * Takes the given token and creates an image from the token.
	 * @param token contains URL for the image
	 */
	private void handleRequest(final Token token) {
		try {
			final String url = requestMap.get(token);
			if (url == null) {
				return;
			}
			
			final Bitmap bitmap;
			// Check the cache to see if it already has the image stored
			if (cache.get(url) == null) {
				byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
				bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
				cache.put(url, bitmap);
			}
			else {
				bitmap = cache.get(url);
			}
			
			Log.i(TAG, "Bitmap created.");
			
			mResponseHandler.post(new Runnable() {
				public void run() {
					if (requestMap.get(token) != url) {
						return;
					}
					
					requestMap.remove(token);
					mListener.onThumbnailDownloaded(token, bitmap);
				}
			});
		} catch (IOException ioe) {
			Log.e(TAG, "Error downloading image", ioe);
		}
	}
}
