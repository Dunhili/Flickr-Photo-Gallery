package com.flickr.photogallery;

/**
 * Model for the photo, contains information about the owner, url, id, and caption.
 * @author dunhili
 */
public class Photo {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
    private String mCaption;
    private String mId;
    private String mUrl;
    private Member mOwner;

    ////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ////////////////////////////////////////////////////////////////////
    
	public String getCaption() {
        return mCaption;
    }
    
    public String getId() {
        return mId;
    }
    
    public String getUrl() {
        return mUrl;
    }
    
    public Member getOwner() {
		return mOwner;
	}

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public void setId(String id) {
        mId = id;
    }
    
    public void setUrl(String url) {
        mUrl = url;
    }
    
	public void setOwner(Member owner) {
		mOwner = owner;
	}
    
    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
	
    public String getPhotoPageUrl() {
    	return "http://www.flickr.com/photos/" + mOwner + "/" + mId;
    }

    public String toString() {
        return mCaption;
    }
}
