package com.flickr.photogallery;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Fragment that handles creating the web page within the app.
 * @author dunhili
 */
public class PhotoPageFragment extends VisibleFragment {
    ////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////
	
    private String mUrl;
    private WebView mWebView;

    ////////////////////////////////////////////////////////////////////
    // Public Methods
    ////////////////////////////////////////////////////////////////////
    
    /**
     * Called when the fragment is created.
     * @param savedInstanceState bundle that contains stored information
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ActionBar titleBar = getActivity().getActionBar();
        if (titleBar.isShowing()) {
        	titleBar.hide();
        }

        mUrl = getActivity().getIntent().getData().toString();
    }

    /**
     * Called when the view is created. Displays the web page and progress bar.
     * @param inflater inflater for the view
     * @param parent view group that the fragment is part of
     * @param savedInstanceState bundle that contains any stored information
     */
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_page, parent, false);

        final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        progressBar.setMax(100); // WebChromeClient reports in range 0-100
        final TextView titleTextView = (TextView) view.findViewById(R.id.titleTextView);

        mWebView = (WebView) view.findViewById(R.id.webView);

        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView webView, int progress) {
                if (progress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(progress);
                }
            }

            public void onReceivedTitle(WebView webView, String title) {
                titleTextView.setText(title);
            }
        });
        
        mWebView.loadUrl(mUrl);

        return view;
    }
}
