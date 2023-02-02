package com.example.capstone_temi;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public TextView name;
    public ImageButton reload;
    public ImageButton back;
    public Button home;
    public int portNumber = 8080;
     // Temi current level: 3


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
        robot = Robot.getInstance();
        reload = (ImageButton) findViewById(R.id.refresh);
        back = (ImageButton) findViewById(R.id.back);
        home = findViewById(R.id.goHome);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robot.goTo("home base");
            }
        });

        this.webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //---- For Website hosting with INTERNET-----
        WebViewClientImpl webViewClient = new WebViewClientImpl(this);
        webView.setWebViewClient(webViewClient);
        webView.loadUrl(url);

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

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("jin", "notbusy");
        SharedPreferences sharedPreferences = getSharedPreferences("Busy",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("busy", false);
        editor.apply();
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(portNumber);
        }




        @Override
        public Response serve(IHTTPSession session) {
            if (session.getMethod() == Method.POST) {

                SharedPreferences sh = getSharedPreferences("Busy", MODE_PRIVATE);
                Boolean isbusy = sh.getBoolean("busy", true);



                if(!isbusy){
                    // Storing data into SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("Busy",MODE_PRIVATE);

                    // Creating an Editor object to edit(write to the file)
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    // Storing the key and its value as the data fetched from edittext
                    myEdit.putBoolean("busy", true);
                    myEdit.apply();
                    Log.v("jin", "setup");
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
                        intent.putExtra("difflevel", true);

                        startActivity(intent);

                        return newFixedLengthResponse("Request succeeded.");
                    } catch (IOException | ResponseException | JSONException e) {
                        // handle
                        e.printStackTrace();
                    }
                }
                else{
                    return newFixedLengthResponse(Response.Status.CONFLICT, MIME_PLAINTEXT, "This Temi is currently in use, come back later!");
                }
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "The requested resource does not exist");
        }
    }
}