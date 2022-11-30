package com.example.capstone_temi;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.IOException;
import java.util.HashMap;

import fi.iki.elonen.NanoHTTPD;


public class MainActivity extends AppCompatActivity
{
    private WebServer server;
    public String goserver = "http://192.168.0.192:10000";
    public int portNumber = 8080;
    public String levelNo = "3";

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
            if(level.equals(levelNo)){
                TextView leveltxt = findViewById(R.id.level);
                TextView shelfnotxt = findViewById(R.id.shelfno);
                leveltxt.setText("Level: " + level);
                shelfnotxt.setText("Shelf Number: " + shelf);

            }
            else{
                String requestUrl = goserver + "/wronglevel";

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
            super(portNumber);
        }

        @Override
        public Response serve(IHTTPSession session) {

            if (session.getMethod() == Method.POST) {
                try {
                    final HashMap<String, String> map = new HashMap<String, String>();
                    session.parseBody(map);
                    String data = map.get("postData");
                    Context ctx=getApplicationContext();


                    Intent intent = new Intent(ctx, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting
                    // the activity from a service
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(intent);


                    JSONObject json = new JSONObject(data);
                    TextView leveltxt = findViewById(R.id.level);
                    TextView shelfnotxt = findViewById(R.id.shelfno);
                    leveltxt.setText("Level: " + json.getString("level"));
                    shelfnotxt.setText("Shelf Number: " + json.getString("shelfno"));


                    return newFixedLengthResponse(data);
                } catch (IOException | ResponseException | JSONException e) {
                    // handle
                    e.printStackTrace();
                }
            }
            if (session.getMethod() == Method.POST) {
                try {
                    session.parseBody(new HashMap<>());
                    String requestBody = session.getQueryParameterString();
                    return newFixedLengthResponse("Request body = " + requestBody);
                } catch (IOException | ResponseException e) {
                    return newFixedLengthResponse(e.getMessage());
                }
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
