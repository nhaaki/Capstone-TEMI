package com.example.capstone_temi;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.robotemi.sdk.NlpResult;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.activitystream.ActivityStreamPublishMessage;
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnLocationsUpdatedListener;
import com.robotemi.sdk.map.MapDataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;


public class GuideActivity extends AppCompatActivity implements
        Robot.NlpListener,
//        OnRobotReadyListener,
        Robot.ConversationViewAttachesListener,
        Robot.WakeupWordListener,
        Robot.ActivityStreamPublishListener,
        Robot.TtsListener,
        OnBeWithMeStatusChangedListener,
        OnGoToLocationStatusChangedListener,
        OnLocationsUpdatedListener
{
    private WebServer server;
    public String goserver = "http://192.168.0.192:10000";
    public int portNumber = 8080;
    public String levelNo = "3";
    public Robot robot;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        robot = Robot.getInstance();

        server = new WebServer();
        try {
            server.start();
        } catch (IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");
        // ATTENTION: This was auto-generated to handle app links.
        handleIntent();
        Log.v("ur dad", "oncreate");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent();
        Log.v("ur dad", "onnew");
    }

    private void handleIntent(){
        Intent appLinkIntent = getIntent();

        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
        Log.v("ur dad", appLinkIntent.toString());
        if(appLinkData != null){

            String rawdata = appLinkData.getLastPathSegment();
            String[] data = rawdata.split(":",4 );
            String level = data[0];
            String shelf = data[1];
            String bookName = data[2];
            String bookId = data[3];
            Log.v("ur dad", appLinkIntent.toString());
            if(level.equals(levelNo)){
                Log.v("ur dad", "ABC");

                TextView booknametxt = findViewById(R.id.book_name);
                TextView bookidtxt = findViewById(R.id.book_id);

                booknametxt.setText("Book Name: " + bookName);
                bookidtxt.setText("Book ID: " + bookId);

                appLinkIntent = null;
                robot.goTo("shelf"+shelf);


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

                RequestQueue namerequestQueue = Volley.newRequestQueue(GuideActivity.this);
                namerequestQueue.add(jsonObjectRequest);




            }

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

//        robot.getInstance().addOnRobotReadyListener(this);
        robot.getInstance().addNlpListener(this);
        robot.getInstance().addOnBeWithMeStatusChangedListener(this);
        robot.getInstance().addOnGoToLocationStatusChangedListener(this);
        robot.getInstance().addConversationViewAttachesListenerListener(this);
        robot.getInstance().addWakeupWordListener(this);
        robot.getInstance().addTtsListener(this);
        robot.getInstance().addOnLocationsUpdatedListener(this);
        MapDataModel locations = robot.getMapData();
        Log.v("ur mom", locations.getLocations().toString());
        


    }






    @Override
    protected void onStop() {
        super.onStop();
//        robot.getInstance().removeOnRobotReadyListener(this);
        robot.getInstance().removeNlpListener(this);
        robot.getInstance().removeOnBeWithMeStatusChangedListener(this);
        robot.getInstance().removeOnGoToLocationStatusChangedListener(this);
        robot.getInstance().removeConversationViewAttachesListenerListener(this);
        robot.getInstance().removeWakeupWordListener(this);
        robot.getInstance().removeTtsListener(this);
        robot.getInstance().removeOnLocationsUpdateListener(this);
    }

    // DON'T FORGET to stop the server
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (server != null)
            server.stop();
    }

    @Override
    public void onPublish(@NonNull ActivityStreamPublishMessage activityStreamPublishMessage) {

    }

    @Override
    public void onConversationAttaches(boolean b) {

    }

    @Override
    public void onNlpCompleted(@NonNull NlpResult nlpResult) {

    }

    @Override
    public void onTtsStatusChanged(@NonNull TtsRequest ttsRequest) {

    }

    @Override
    public void onWakeupWord(@NonNull String s, int i) {

    }

    @Override
    public void onBeWithMeStatusChanged(@NonNull String s) {

    }

    @Override
    public void onGoToLocationStatusChanged(@NonNull String s, @NonNull String s1, int i, @NonNull String s2) {

    }

    @Override
    public void onLocationsUpdated(@NonNull List<String> list) {

    }

//    @Override
//    public void onRobotReady(boolean isReady) {
//        if (isReady) {
//            try {
//                final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
//                robot.onStart(activityInfo);
//            } catch (PackageManager.NameNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

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


                    Intent intent = new Intent(ctx, GuideActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting
                    // the activity from a service
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    startActivity(intent);


                    JSONObject json = new JSONObject(data);


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

