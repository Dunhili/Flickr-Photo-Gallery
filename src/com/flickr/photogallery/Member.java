package com.flickr.photogallery;

public class Member {
	private String mUserName;
	private String mNSID;
	
	public Member(String userName) {
		this(userName, FlickrFetchr.getNSIDFromUserName(userName));
	}
	
	public Member(String userName, String NSID) {
		setUserName(userName);
		setNSID(NSID);
	}
	
	public String getUserName() {
		return mUserName;
	}
	
	public void setUserName(String userName) {
		mUserName = userName;
	}
	
	public String getNSID() {
		return mNSID;
	}
	
	public void setNSID(String NSID) {
		mNSID = NSID;
	}	
}
