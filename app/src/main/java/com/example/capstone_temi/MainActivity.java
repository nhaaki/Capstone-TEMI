package com.example.capstone_temi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

import fi.iki.elonen.NanoHTTPD;


public class MainActivity extends AppCompatActivity
{
    private WebServer server;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        server = new WebServer();
        try {
            server.start();
        } catch (IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");
        // ATTENTION: This was auto-generated to handle app links.
        handleIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
    }

    private void handleIntent(){
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if(appLinkData != null){

            String rawdata = appLinkData.getLastPathSegment();
            String[] data = rawdata.split(":",2 );
            String level = data[0];
            String shelf = data[1];
            if(level.equals("3")){
                TextView leveltxt = findViewById(R.id.level);
                TextView shelfnotxt = findViewById(R.id.shelfno);
                leveltxt.setText("Level: " + level);
                shelfnotxt.setText("Shelf Number: " + shelf);

            }
            else{
                String requestUrl = "http://10.0.2.2:10000/wronglevel";

                JSONObject postData = new JSONObject();
                try {
                    postData.put("level", level);
                    postData.put("shelfno", shelf);
                }catch (JSONException e)
                {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, requestUrl, postData, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

                RequestQueue namerequestQueue = Volley.newRequestQueue(MainActivity.this);
                namerequestQueue.add(jsonObjectRequest);




            }

        }
    }



    // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (session.getMethod() == Method.GET) {
                String shelfno = session.getParameters().get("shelfno").get(0);
                String level = session.getParameters().get("level").get(0);
                return newFixedLengthResponse("Requested level = " + level);
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "The requested resource does not exist");
        }
    }

}



//    @Override
//    public void onPublish(@NonNull ActivityStreamPublishMessage activityStreamPublishMessage) {
//
//    }
//
//    @Override
//    public void onConversationAttaches(boolean b) {
//
//    }
//
//    @Override
//    public void onNlpCompleted(@NonNull NlpResult nlpResult) {
//
//    }
//
//    @Override
//    public void onTtsStatusChanged(@NonNull TtsRequest ttsRequest) {
//
//    }
//
//    @Override
//    public void onWakeupWord(@NonNull String s, int i) {
//
//    }
//
//    @Override
//    public void onBeWithMeStatusChanged(@NonNull String s) {
//
//    }
//
//    @Override
//    public void onGoToLocationStatusChanged(@NonNull String s, @NonNull String s1, int i, @NonNull String s2) {
//
//    }
//
//    @Override
//    public void onLocationsUpdated(@NonNull List<String> list) {
//
//    }
//
//    @Override
//    public void onRobotReady(boolean b) {
//
//    }
