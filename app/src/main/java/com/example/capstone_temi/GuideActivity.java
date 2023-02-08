package com.example.capstone_temi;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuideActivity extends AppCompatActivity implements
        Robot.NlpListener,
        Robot.ConversationViewAttachesListener,
        Robot.WakeupWordListener,
        Robot.ActivityStreamPublishListener,
        Robot.TtsListener,
        OnBeWithMeStatusChangedListener,
        OnGoToLocationStatusChangedListener,
        OnLocationsUpdatedListener
{


    public OnGoToLocationStatusChangedListener listerner;
    public String flaskServer;

    // NOTE: Change this to TEMI's current level when downloading the app
    public String levelNo;

    public Boolean cancel;

    // Book details
    public String level;
    public String shelfNo;
    public String bookId;
    public String bookName;
    static boolean active = false;
    CountDownTimer waitTimer;

    public Robot robot;

    public Boolean answer = true;
    private String currentphotopath;
    public Bitmap imageReceived;

    public ImageButton back;
    public TextView booknametxt;
    public TextView bookidtxt;
    public TextView taskfinishtxt;
    public LottieAnimationView lottieAV;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        active = true;
        flaskServer = getString(R.string.flaskServer);
        levelNo = getString(R.string.levelNo);

        Log.v("jin", "on create");

        // Robot is busy at the moment
        SharedPreferences sharedPreferences = getSharedPreferences("Busy",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("busy", true);
        editor.apply();

        setContentView(R.layout.activity_guide);
        robot = Robot.getInstance();
        lottieAV = findViewById(R.id.animationView);
        lottieAV.setVisibility(View.INVISIBLE);

        Log.w("Httpd", "Web server initialized.");

        // ATTENTION: This was auto-generated to handle app links.
        handleIntent();

        back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        active = true;
        Log.v("jin", "new intent");
        handleIntent();
    }

    private void handleIntent(){
        Intent appLinkIntent = getIntent();

        // If diff level, get the verified book data from applink
        Uri appLinkData = appLinkIntent.getData();
        String vbookId = appLinkIntent.getStringExtra("verifiedBookId");
        String vlevel = appLinkIntent.getStringExtra("verifiedLevel");
        String vshelfNo = appLinkIntent.getStringExtra("verifiedShelfNo");
        String vbookName = appLinkIntent.getStringExtra("verifiedBookName");


        // This code is ran through clicking of url + same level
        // http://temibot.com/level/level=3&shelfno=1&bookname=Michelle%20Obama's%20Life%20%26%20Experience&bookid=E909%2E%20O24%20O12%20PBK
        if(appLinkData != null){

            // Extracting the book detail from the URL
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

            // Check if the book is at the same level
            if(level.equals(levelNo)){

                // While TEMI is moving, set the background with all the book details displayed
                booknametxt = findViewById(R.id.book_name);
                bookidtxt = findViewById(R.id.book_id);
                taskfinishtxt = findViewById(R.id.taskFinishTxt);

                booknametxt.setText(bookName);
                bookidtxt.setText(bookId);
                taskfinishtxt.setText("We've reached shelf " + shelfNo + "! Your book should be nearby :)");

                // Store the book detail on ResDB
                String requestUrl = "https://capstonetemi-3ec7.restdb.io/rest/book-history";
                Date currentTime = Calendar.getInstance().getTime();
                JSONObject postData = new JSONObject();
                try {
                    postData.put("level", level);
                    postData.put("shelfno", shelfNo);
                    postData.put("bookid", bookId);
                    postData.put("bookname", bookName);
                    postData.put("searchedDateTime", currentTime);
                }catch (JSONException e)
                {
                    e.printStackTrace();
                }

                // Use POST to update on ResDB
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, requestUrl, postData, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {}
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("content-type", "application/json");
                        params.put("x-apikey", "2f9040149a55d3c3e6bfa3f356b6dec655137");
                        params.put("cache-control","no-cache");
                        return params;
                    }
                };

                RequestQueue namerequestQueue = Volley.newRequestQueue(GuideActivity.this);
                namerequestQueue.add(jsonObjectRequest);

                // TEMI goes to the shelf
                robot.goTo("shelf"+shelfNo);

                // Checking on TEMI's status while moving
                robot.addOnGoToLocationStatusChangedListener(new OnGoToLocationStatusChangedListener() {
                    @Override
                    public void onGoToLocationStatusChanged(@NonNull String location, @NonNull String status, int id, @NonNull String desc) {

                        // If the TEMI has not returned to the home base yet
                        if(location.equals("shelf"+shelfNo)){
                            if(status.equals("complete")){

                                // TEMI does not have ringtone
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                r.play();

                                lottieAV = findViewById(R.id.animationView);
                                lottieAV.setVisibility(View.VISIBLE);

                                // Show the popup below
                                popup();
                                robot.removeOnGoToLocationStatusChangedListener(this);
                            }
                        }
                    }
                });
            }
            // For different level
            else{



                // Launch the take pic intent
                ActivityResultLauncher<Intent> imageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    imageReceived = BitmapFactory.decodeFile(currentphotopath);
                                    final boolean[] showPopup3 = {true};

                                    if (imageReceived != null) {
                                        // Send the image in json
                                        String requestUrl = flaskServer + "/receiveimage";
                                        JSONObject postData = new JSONObject();

                                        // Encode the bitmap
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        imageReceived.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                        byte[] imageBytes = baos.toByteArray();
                                        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                                        try {
                                            postData.put("image", encodedImage);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        // Post to /receiveimage to send the first image over to the user
                                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, requestUrl, postData, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {}
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                error.printStackTrace();
                                            }
                                        });
                                        RequestQueue nameRequestQueue = Volley.newRequestQueue(GuideActivity.this);
                                        nameRequestQueue.add(jsonObjectRequest);

                                        // Delete the image in the temi after use
                                        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                        deleteTempFiles(storageDirectory);

                                        // Make another api request to the server
                                        // The server will redirect the information to the MainActivity
                                        String wronglevelUrl = flaskServer + "/wronglevel";
                                        JSONObject bookData = new JSONObject();
                                        try {
                                            bookData.put("level", level);
                                            bookData.put("shelfno", shelfNo);
                                            bookData.put("bookid", bookId);
                                            bookData.put("bookname", bookName);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                        JsonObjectRequest wronglevelRequest = new JsonObjectRequest(Request.Method.POST, wronglevelUrl, bookData, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                try {
                                                    String rescode = response.getString("response_code");
                                                    if (rescode.equals("409")){
                                                        Toast.makeText(getApplicationContext(),"Temi is busy right now. Please try again later!",Toast.LENGTH_LONG).show();
                                                        Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                                                        startActivity(intent);

                                                    } else {


                                                        // After taking image and send it over to the server
                                                        // inflate the layout of the popup3 window
                                                        LayoutInflater inflater = LayoutInflater.from(GuideActivity.this);
                                                        View popup3View = inflater.inflate(R.layout.popup3, null);
                                                        TextView popup3txt = popup3View.findViewById(R.id.popup3txt);
                                                        popup3txt.setText("Your book is located at Level " + level +". Please kindly wait at Level "+ level +" staircase, where another TEMI will serve you!");

                                                        // create the popup window
                                                        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                                                        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                                                        boolean focusable = false; // lets taps outside the popup also dismiss it
                                                        final PopupWindow popupWindow = new PopupWindow(popup3View, width, height, focusable);

                                                        CountDownTimer waitTimer;
                                                        waitTimer = new CountDownTimer(3000, 1000) {
                                                            public void onTick(long millisUntilFinished) {}
                                                            public void onFinish() {
                                                                // show the popup window
                                                                // which view you pass in doesn't matter, it is only used for the window tolken
                                                                popupWindow.showAtLocation(GuideActivity.this.findViewById(R.id.main), Gravity.CENTER, 0, 0);
                                                                popup3View.setOnTouchListener(new View.OnTouchListener() {
                                                                    @Override
                                                                    public boolean onTouch(View v, MotionEvent event) {
                                                                        popupWindow.dismiss();

                                                                        // Goes back to the main activity
                                                                        Intent intent = new Intent(GuideActivity.this, MainActivity.class);
                                                                        startActivity(intent);

                                                                        return true;
                                                                    }
                                                                });
                                                            }
                                                        }.start();
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                error.printStackTrace();
                                            }
                                        });
                                        nameRequestQueue.add(wronglevelRequest);
                                    }
                                }
                            }
                        });

                    // inflate the layout of the popup2 window (right before the take pic intent
                    LayoutInflater inflater = LayoutInflater.from(GuideActivity.this);
                    View popup2View = inflater.inflate(R.layout.popup2, null);
                    ImageButton back = findViewById(R.id.back);
                    back.setVisibility(View.GONE);

                    // create the popup window
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = false; // lets taps outside the popup also dismiss it
                    final PopupWindow popupWindow = new PopupWindow(popup2View, width, height, focusable);

                    CountDownTimer waitTimer;
                    waitTimer = new CountDownTimer(3000, 1000) {
                        public void onTick(long millisUntilFinished) { }
                        public void onFinish() {
                            popupWindow.showAtLocation(GuideActivity.this.findViewById(R.id.main), Gravity.CENTER, 0, 0);
                            popup2View.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    popupWindow.dismiss();
                                    String fileName = "photo";
                                    File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                    try{
                                        File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                                        currentphotopath = imageFile.getAbsolutePath();
                                        Uri imageUri = FileProvider.getUriForFile(GuideActivity.this, "com.example.capstone_temi.fileprovider", imageFile);
                                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                                        imageActivityResultLauncher.launch(intent);
                                    }
                                    catch (IOException e){
                                        e.printStackTrace();
                                    }
                                    return true;
                                }
                            });
                        }
                    }.start();
                }
            }

        // If this activity is called with book from different level
        else if(vbookId != null){
            booknametxt = findViewById(R.id.book_name);
            bookidtxt = findViewById(R.id.book_id);
            taskfinishtxt = findViewById(R.id.taskFinishTxt);

            booknametxt.setText(vbookName);
            bookidtxt.setText(vbookId);
            taskfinishtxt.setText("We've reached shelf " + vshelfNo + "! Your book should be nearby :)");

            // Save it to the ResDB
            String requestUrl = "https://capstonetemi-3ec7.restdb.io/rest/book-history";
            JSONObject postData = new JSONObject();
            Date currentTime = Calendar.getInstance().getTime();
            try {
                postData.put("level", vlevel);
                postData.put("shelfno", vshelfNo);
                postData.put("bookid", vbookId);
                postData.put("bookname", vbookName);
                postData.put("searchedDateTime", currentTime);
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, requestUrl, postData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {}
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("content-type", "application/json");
                    params.put("x-apikey", "2f9040149a55d3c3e6bfa3f356b6dec655137");
                    params.put("cache-control","no-cache");
                    return params;
                }
            };
            RequestQueue namerequestQueue = Volley.newRequestQueue(GuideActivity.this);
            namerequestQueue.add(jsonObjectRequest);

            // Same as above
            robot.goTo("shelf"+vshelfNo);
            robot.addOnGoToLocationStatusChangedListener(new OnGoToLocationStatusChangedListener() {
                @Override
                public void onGoToLocationStatusChanged(@NonNull String location, @NonNull String status, int id, @NonNull String desc) {
                    // If the TEMI has not returned to the home base yet
                    if(location.equals("shelf"+vshelfNo)){
                        if(status.equals("complete")){

                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            r.play();

                            lottieAV = findViewById(R.id.animationView);
                            lottieAV.setVisibility(View.VISIBLE);

                            popup();
                            robot.removeOnGoToLocationStatusChangedListener(this);
                        }
                    }
                }
            });

        }

        // If it is being told to wait at the waiting area (after receiving POST from server at Main Activity)
        else{
            bookId = appLinkIntent.getStringExtra("bookId");
            level = appLinkIntent.getStringExtra("level");
            shelfNo = appLinkIntent.getStringExtra("shelfNo");
            bookName = appLinkIntent.getStringExtra("bookName");

            if(level.equals(levelNo)) {
                boolean difflevel = appLinkIntent.getBooleanExtra("difflevel", false);
                if (difflevel) {
                    robot.goTo("waitingarea");
                    listerner = new OnGoToLocationStatusChangedListener() {
                        @Override
                        public void onGoToLocationStatusChanged(@NonNull String location, @NonNull String status, int id, @NonNull String desc) {
                            // If the TEMI is not returned to the home base yet
                            if (location.equals("waitingarea")) {
                                if (status.equals("complete")) {
                                    Intent intent = new Intent(GuideActivity.this, FaceVerificationActivity.class);
                                    intent.putExtra("bookName", bookName);
                                    intent.putExtra("level", level);
                                    intent.putExtra("shelfNo", shelfNo);
                                    intent.putExtra("bookId", bookId);
                                    robot.removeOnGoToLocationStatusChangedListener(listerner);
                                    startActivity(intent);

                                }
                            }
                        }
                    };
                    robot.addOnGoToLocationStatusChangedListener(listerner);
                }
            }
        }
    }

    // Show popup below after reaching the shelf
    public void popup() {
        if(active){
            back.setVisibility(View.GONE);

            // inflate the layout of the popup window
            LayoutInflater inflater = LayoutInflater.from(this.getApplicationContext());
            View popupView = inflater.inflate(R.layout.popup, null);

            // create the popup window
            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            boolean focusable = false; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            popupWindow.setOutsideTouchable(false);
            popupWindow.setFocusable(false);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(this.findViewById(R.id.main), Gravity.BOTTOM, 0, 0);

            Button yes = popupView.findViewById(R.id.yes);
            Button no = popupView.findViewById(R.id.no);
            answer = true;

            cancel = false;


            TextView countdown = popupView.findViewById(R.id.timer);
            waitTimer = new CountDownTimer(60000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int time =  Integer.parseInt(countdown.getText().toString()) - 1;
                    countdown.setText(String.valueOf(time));

                    if(cancel){
                        cancel();
                    }
                }

                public void onFinish() {
                    if(answer == true){
                        answer = false;
                        Intent launchIntent = new Intent(GuideActivity.this, MainActivity.class);
                        startActivity(launchIntent);

                        popupWindow.dismiss();
                        robot.goTo("home base");
                    }
                }
            }.start();



            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancel = true;
                    answer = false;
                    waitTimer.cancel();
                    Intent launchIntent = new Intent(GuideActivity.this, MainActivity.class);
                    if (launchIntent != null) {

                        startActivity(launchIntent);//null pointer check in case package name was not found
                        finish();

                    }
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cancel = true;
                    answer = false;
                    Intent launchIntent = new Intent(GuideActivity.this, MainActivity.class);
                    waitTimer.cancel();
                    startActivity(launchIntent);

                    popupWindow.dismiss();
                    robot.goTo("home base");
                    listerner = new OnGoToLocationStatusChangedListener() {
                        @Override
                        public void onGoToLocationStatusChanged(@NonNull String location, @NonNull String status, int id, @NonNull String desc) {
                            // If the TEMI is not returned to the home base yet
                            if (location.equals("home base")) {
                                if (status.equals("complete")) {
                                    finish();
                                    robot.removeOnGoToLocationStatusChangedListener(listerner);

                                }
                            }
                        }
                    };


                }
            });

        }


    }

    // Set to busy when this intent is launch
    @Override
    protected void onResume() {
        super.onResume();
        active = true;
        Log.v("jin", "on resume");
        SharedPreferences sharedPreferences = getSharedPreferences("Busy",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("busy", true);
        editor.apply();


    }

    // Initialize the robot
    @Override
    protected void onStart() {
        super.onStart();
        robot.getInstance().addNlpListener(this);
        robot.getInstance().addOnBeWithMeStatusChangedListener(this);
        robot.getInstance().addOnGoToLocationStatusChangedListener(this);
        robot.getInstance().addConversationViewAttachesListenerListener(this);
        robot.getInstance().addWakeupWordListener(this);
        robot.getInstance().addTtsListener(this);
        robot.getInstance().addOnLocationsUpdatedListener(this);
    }

    // Delete the image taken
    private boolean deleteTempFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteTempFiles(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        return file.delete();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(waitTimer != null){
            waitTimer.cancel();
            Log.v("jin", "xcghjk");
        }



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
    public void onDestroy() {
        super.onDestroy();

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

}

