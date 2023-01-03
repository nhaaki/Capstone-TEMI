package com.example.capstone_temi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.robotemi.sdk.face.ContactModel;
import com.robotemi.sdk.face.OnFaceRecognizedListener;
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnLocationsUpdatedListener;
import com.robotemi.sdk.map.MapDataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

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
    public String levelNo = "3"; //TEMI current level
    public String level; // Level from the req URL
    public String shelfNo; // Shelf No from the req URL
    public String bookId; // Bookid from the req URL
    public String bookName; // BookName from the req URL
    public Robot robot;
    public Bitmap imageReceived;
    public Boolean answer = true;


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

            // "http://temibot.com/level/level=3&shelfno=1&bookname=Michelle%20Obama's%20Life%20%26%20Experience&bookid=E909%2E%20O24%20O12%20PBK"
            String rawdata = appLinkData.getLastPathSegment();
            String[] data = rawdata.split("&",4 );

            for (int i =0; i < 4; i++) {
                String[] dataPair = data[i].split("=", 2);
                String key = dataPair[0];
                if (key.equals("level")) {
                    level = dataPair[1];
                }
                else if (key.equals("shelfno")) {
                    shelfNo = dataPair[1];
                }
                else if (key.equals("bookid")) {
                    bookId = dataPair[1];
                }
                else if (key.equals("bookname")) {
                    bookName = dataPair[1].replace("~", "&");
                }
            }

 /*

            String rawdata = appLinkData.getLastPathSegment();
            String[] data = rawdata.split(";",4 );
            level = data[0];
            shelfNo = data[1];
            bookName = data[2];
            bookId = data[3];
  */


            if(level.equals(levelNo)){
                TextView booknametxt = findViewById(R.id.book_name);
                TextView bookidtxt = findViewById(R.id.book_id);

                booknametxt.setText(bookName);
                bookidtxt.setText(bookId);

                appLinkIntent = null;

                robot.goTo("shelf"+shelfNo);
                robot.addOnGoToLocationStatusChangedListener(new OnGoToLocationStatusChangedListener() {
                    @Override
                    public void onGoToLocationStatusChanged(@NonNull String location, @NonNull String status, int id, @NonNull String desc) {
                       // If the TEMI is not returned to the home base yet
                        if(!location.equals("home base")){
                            if(status.equals("complete")){
                                popup();
                            }
                        }
                    }
                });
            }

            // For different Level TEMIs
            else{
                // Launch take photo
                ActivityResultLauncher<Intent> imageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    Intent data = result.getData();
                                    imageReceived = (Bitmap) data.getExtras().get("data");
                                    if (imageReceived != null) {
                                        // Send the image in json
                                        String requestUrl = goserver + "/image";
                                        JSONObject postData = new JSONObject();

                                        // Encode the bitmap
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        imageReceived.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        byte[] imageBytes = baos.toByteArray();
                                        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                                        try {
                                            postData.put("image", encodedImage);
                                        } catch (JSONException e) {
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

                                        RequestQueue nameRequestQueue = Volley.newRequestQueue(GuideActivity.this);
                                        nameRequestQueue.add(jsonObjectRequest);
                                    }
                                }
                            }
                        });

                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                imageActivityResultLauncher.launch(intent);






                String requestUrl = goserver + "/wronglevel";
                JSONObject postData = new JSONObject();
                try {
                    postData.put("level", level);
                    postData.put("shelfno", shelfNo);
                    postData.put("bookid", bookId);
                    postData.put("bookname", bookName);

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

    public void popup() {

        // inflate the layout of the popup window
        LayoutInflater inflater = LayoutInflater.from(this.getApplicationContext());
        View popupView = inflater.inflate(R.layout.popup, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM, 0, 0);

        Button yes = popupView.findViewById(R.id.yes);
        Button no = popupView.findViewById(R.id.no);
        answer = true;


        CountDownTimer waitTimer;
        TextView countdown = popupView.findViewById(R.id.timer);
        waitTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                int time =  Integer.parseInt(countdown.getText().toString()) - 1;
                countdown.setText(String.valueOf(time));

            }

            public void onFinish() {

                if(answer == true){
                    robot.goTo("home base");
                }

            }
        }.start();

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                answer = false;
                Intent launchIntent = new Intent(GuideActivity.this, MainActivity.class);
                if (launchIntent != null) {
                    Log.w("jy", "your mum");
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(launchIntent);
                popupWindow.dismiss();
                robot.goTo("home base");
                answer = false;
            }
        });

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

