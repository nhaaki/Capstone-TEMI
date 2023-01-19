package com.example.capstone_temi;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.face.ContactModel;
import com.robotemi.sdk.face.OnFaceRecognizedListener;
import com.robotemi.sdk.listeners.OnMovementStatusChangedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;


public class MainActivity extends AppCompatActivity {


    private WebView webView = null;
    private MainActivity.WebServer server;
    public String url = "https://chen-han-np.github.io/Capstone-TEMI-Website-Demo/";
    public Robot robot;
    public boolean free;



   //public Button takePhotoButton;
   // public Button sendBut;
    //public ImageView imageSending;
    public TextView name;

    public ImageButton reload;
    public ImageButton back;
   // public Bitmap imageReceived;
    public Button home;


    private static final int CAMERA_PIC_REQUEST = 1337;
    public String goserver = "http://172.20.10.4:105";
    public int portNumber = 8080;
    public String levelNo = "3"; //TEMI current level


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        server = new MainActivity.WebServer();
        try {
            server.start();
        } catch (IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");


        setContentView(R.layout.activity_main);

        // For level 2 feature showcase
      //  takePhotoButton = (Button) findViewById(R.id.takePhotoBut);
    //    sendBut = (Button) findViewById(R.id.sendBut);
    //    imageSending = (ImageView) findViewById(R.id.picture);

        reload = (ImageButton) findViewById(R.id.refresh);
        back = (ImageButton) findViewById(R.id.back);

        robot = Robot.getInstance();

//        Button go = findViewById(R.id.go);
/*
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GuideActivity.class);
                intent.putExtra("level", "2");
                intent.putExtra("shelfno", "17");
                intent.putExtra("bookid", "HF1118-G569");
                intent.putExtra("bookname", "Diary of a Wimpy Kid");
                startActivity(intent);
            }
        });

 */
        home = findViewById(R.id.goHome);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robot.goTo("home base");
            }
        });


        Button dance = findViewById(R.id.dance);

        dance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean dancing = true;
                robot.turnBy(360, 1);
                robot.turnBy(-360, 1);

                robot.tiltBy(-25, 1);
                robot.tiltBy(55, 1);


                robot.addOnMovementStatusChangedListener(new OnMovementStatusChangedListener() {
                    @Override
                    public void onMovementStatusChanged(@NonNull String type, @NonNull String status) {

                        int times = 0;

                        if (type.equals("turnBy")) {
                            if (status.equals("complete")) {
                                times += 1;
                                if (times == 2) {
                                    robot.stopMovement();
                                }
                            }
                        }

                    }
                });
            }
        });


        this.webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //---- For Website hosting with INTERNET-----
           WebViewClientImpl webViewClient = new WebViewClientImpl(this);
            webView.setWebViewClient(webViewClient);
            webView.loadUrl(url);

        // For local HTML files
    //    webView.setWebViewClient(new Callback());
    //    webView.loadUrl("file:///android_asset/index.html");
/*
        ActivityResultLauncher<Intent> imageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            imageReceived = (Bitmap) data.getExtras().get("data");
                            imageSending.setImageBitmap(imageReceived);
                        }
                    }
                });

 */
/*
        // For level 2 showcase
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                imageActivityResultLauncher.launch(intent);
            }

        });

 */
/*
        sendBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                    RequestQueue nameRequestQueue = Volley.newRequestQueue(MainActivity.this);
                    nameRequestQueue.add(jsonObjectRequest);
                }
            }
        });

 */


        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.reload();

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.goBack();
            }
        });

    }
/*  Local HTML file
    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

 */


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAMERA_PIC_REQUEST) {
//            Bitmap image = (Bitmap) data.getExtras().get("data");
//            imageSending.setImageBitmap(image);
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
                    Log.w("Httpd", data);
                    JSONObject json = new JSONObject(data);

                    Context ctx=getApplicationContext();


                    Intent intent = new Intent(ctx, GuideActivity.class);
                    intent.putExtra("bookId", json.getString("bookid"));
                    intent.putExtra("level", json.getString("level"));
                    intent.putExtra("shelfNo", json.getString("shelfno"));
                    intent.putExtra("bookName", json.getString("bookname"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // You need this if starting
                    // the activity from a service
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.putExtra("free", false);
                    intent.putExtra("difflevel", true);
                    startActivity(intent);



                    return newFixedLengthResponse("fghjk");
                } catch (IOException | ResponseException | JSONException e) {
                    // handle
                    e.printStackTrace();
                }
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "The requested resource does not exist");

        }


    }


}