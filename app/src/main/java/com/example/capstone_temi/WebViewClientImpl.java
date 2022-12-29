package com.example.capstone_temi;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewClientImpl extends WebViewClient {

    private Activity activity = null;

    public WebViewClientImpl(Activity activity) {
        this.activity = activity;
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Intent intent;

        if (url.contains("/level/")) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            activity.startActivity(intent);

            return true;
        }else {
            return false;
        }
    }



}