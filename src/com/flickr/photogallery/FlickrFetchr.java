package com.flickr.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;
import android.util.Log;

/**
 * Handles the HTTP requests for receiving the images from Flickr.
 * @author dunhili
 */
public class FlickrFetchr {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
    public static final String TAG = "PhotoFetcher";
    public static final String PREF_SEARCH_QUERY = "searchQuery";
    public static final String PREF_LAST_RESULT_ID = "lastResultId";
    
    /** Strings for building the URL for the API call. */
    private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "1d8d06a6cff33f87f7d895be8fdfd3ff";
    private static final String PARAM_EXTRAS = "extras";
    private static final String PAGE = "page";
    private static final String EXTRA_SMALL_URL = "url_s";
    private static final String XML_PHOTO = "photo";
    
    /** Strings for the methods */
    private static final String METHOD_FIND_BY_EMAIL = "flickr.people.findByEmail";
    private static final String METHOD_FIND_BY_USERNAME = "flickr.people.findByUsername";
    private static final String METHOD_GET_PUBLIC_PHOTOS = "flickr.people.getPublicPhotos";
    private static final String METHOD_GET_PUBLIC_GROUPS = "flickr.people.getPublicGroups";
    private static final String METHOD_GET_PHOTOS_OF = "flickr.people.getPhotosOf";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";

    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the 100 most recent images, argument is which page to load.
     * @param page page of recent images to load
     * @return array list of recent images
     */
    public ArrayList<Photo> getRecentPhotos(int page) {
    	String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_RECENT)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PAGE, "" + page)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .build().toString();
    	return downloadPhotos(url);
    }

    /**
     * Searches for images matching the query criteria.
     * @param query criteria for the image search
     * @return array list of images matching the criteria
     */
    public ArrayList<Photo> search(String query) {
    	String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_SEARCH)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter("text", query)
                .build().toString();
    	return downloadPhotos(url);
    }
    
    /**
     * Searches for a user by using his/her user name as the search query.
     * @param userName user to search for
     * @return member that matches the criteria or null if not found
     */
    public Member searchByUserName(String userName) {
    	String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_FIND_BY_USERNAME)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("username", userName)
                .build().toString();
    	return searchByUserName(url);
    }
    
    /**
     * Searches for a user by using his/her email address as the search query.
     * @param findEmail email to use as search query
     * @return member that matches the criteria or null if not found
     */
    public Member searchByEmail(String findEmail) {
    	String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_FIND_BY_EMAIL)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("find_email", findEmail)
                .build().toString();
    	return getUserName(url);
    }
    
    public ArrayList<Photo> getPublicPhotos(Member member, int page) {
    	String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_PUBLIC_PHOTOS)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PAGE, "" + page)
                .appendQueryParameter("user_id", member.getNSID())
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .build().toString();
    	return downloadPhotos(url);
    }
    
    /*public ArrayList<Group> getPublicGroups(String query) {
    	String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_PUBLIC_GROUPS)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_TEXT, query)
                .build().toString();
    }*/
    
    /*public ArrayList<Photo> getPhotosOf(Member member) {
    	String url = Uri.parse(ENDPOINT).buildUpon()
                .appendQueryParameter("method", METHOD_GET_PHOTOS_OF)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
                .appendQueryParameter(PARAM_TEXT, member.getUserName())
                .build().toString();
    }*/

    ////////////////////////////////////////////////////////////////////
    // Package Methods
    ////////////////////////////////////////////////////////////////////
    /**
     * Goes through the XML file and parses all the images stored in the XML file.
     * @param items array list to add the images to
     * @param parser XML file to parse through
     * @throws XmlPullParserException thrown if there's an issue parsing the XML
     * @throws IOException thrown if there's an issue reading the XML
     */
    /* package */ void parsePhotosXML(ArrayList<Photo> items, XmlPullParser parser) 
    		throws XmlPullParserException, IOException {
        int eventType = parser.next();
        
        // Used for getPublicPhotos so that we don't have to lookup the same member multiple times
        String previousNSID = "";
        String userName = "";

        // Goes through the document and parses each image in the XML.
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals(XML_PHOTO)) {
                String id = parser.getAttributeValue(null, "id");
                String caption = parser.getAttributeValue(null, "title");
                String smallUrl = parser.getAttributeValue(null, EXTRA_SMALL_URL);
                String nsid = parser.getAttributeValue(null, "owner");
                if (!nsid.equals(previousNSID)) {
                    userName = getUserNameFromNSID(nsid);
                }

                Photo item = new Photo();
                item.setId(id);
                item.setCaption(caption);
                item.setUrl(smallUrl);
                item.setOwner(new Member(userName, nsid));
                items.add(item);
                
                previousNSID = nsid;
            }

            eventType = parser.next();
        }
    }
    
    /**
     * Goes through the XML file and parses the user name stored in the XML file.
     * @param parser XML file to parse through
     * @return member if it was found, otherwise null
     * @throws XmlPullParserException thrown if there's an issue parsing the XML
     * @throws IOException thrown if there's an issue reading the XML
     */
    /* package */ Member parseMemberXML(XmlPullParser parser) 
    		throws XmlPullParserException, IOException {
        if (parser.getName().equals("user")) {
        	String userName = parser.getAttributeValue(null, "username");
        	String nsid = parser.getAttributeValue(null, "nsid");
        	return new Member(userName, nsid);
        }
        else {
        	return null;
        }
    }
    
    /**
     * Returns a new String from the array of bytes created from an image URL.
     * @param urlSpec URL to get the image bytes from
     * @return String created from image byte array
     * @throws IOException thrown if there's an issue reading the file
     */
    /* package */ String getUrl(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
    
    /**
     * Takes a given URL and reads the image bytes. Returns the byte array created from the image.
     * @param urlSpec URL to find the image
     * @return byte array representing the image
     * @throws IOException thrown if there is an issue getting the image
     */
    /* package */ byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Finds the NSID based on the user name.
     * @param userName user to find the NSID of
     * @return NSID of the user
     */
    /* package */ static String getNSIDFromUserName(String userName) {
    	// TODO
    	return "";
    }
    

    /**
     * Finds the user name based on the NSID.
     * @param NSID search query for finding the user name
     * @return user name
     */
    /* package */ static String getUserNameFromNSID(String NSID) {
    	// TODO
    	return "";
    }
    
    ////////////////////////////////////////////////////////////////////
    // Private Methods
    ////////////////////////////////////////////////////////////////////
    
    /**
     * Takes a given URL and retrieves all the images at that URL.
     * @param url URL to look for the images.
     * @return array list of images at the URL
     */
    private ArrayList<Photo> downloadPhotos(String url) {
        ArrayList<Photo> items = new ArrayList<Photo>();
        
        try {
            String xmlString = getUrl(url);
            Log.i(TAG, "Received xml: " + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            
            parsePhotosXML(items, parser);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Failed to parse items", xppe);
        }
        return items;
    }
    
    /**
     * Retrieves the user based on either the user name or the email.
     * @param url url to send to flickr
     * @return member if they were found, otherwise null
     */
    private Member getUserName(String url) {
    	Member member = null;
    	try {
            String xmlString = getUrl(url);
            Log.i(TAG, "Received xml: " + xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlString));
            
            member = parseMemberXML(parser);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (XmlPullParserException xppe) {
            Log.e(TAG, "Failed to parse items", xppe);
        }
    	return member;
    }
}
