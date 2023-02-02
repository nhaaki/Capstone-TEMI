package com.example.capstone_temi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.robotemi.sdk.Robot;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {

    private WebView webView = null;
    private MainActivity.WebServer server;
    public String url = "https://chen-han-np.github.io/Capstone-TEMI-Website-Demo/";
    public Robot robot;

    public TextView name;
    public ImageButton reload;
    public ImageButton back;
    public ImageButton home;
    public int portNumber = 8080;

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

        // Buttons for the browser
        reload = (ImageButton) findViewById(R.id.refresh);
        back = (ImageButton) findViewById(R.id.back);
        home = (ImageButton) findViewById(R.id.goHome);

        this.webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // Browser - need internet access
        WebViewClientImpl webViewClient = new WebViewClientImpl(this);
        webView.setWebViewClient(webViewClient);
        webView.loadUrl(url);

        // Button functions
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                robot.goTo("home base");
            }
        });

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

    // When the user leaves main activity
    @Override
    protected void onStop() {
        super.onStop();
    }

    // When the user comes back to the main activity
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("Busy", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("busy", false);
        editor.apply();
    }

    // Setting up the Nanohttpd server
    private class WebServer extends NanoHTTPD {
        public WebServer()
        {
            super( portNumber );
        }

        // -> Listening to any POST request from the Server
        // -> Usually book detail from the server after calling /wronglevel at GuideActivity
        @Override
        public Response serve(IHTTPSession session) {
            if (session.getMethod() == Method.POST) {
                // Check if the user is at other activity at the moment
                SharedPreferences sh = getSharedPreferences("Busy", MODE_PRIVATE);
                Boolean isbusy = sh.getBoolean("busy", true);
                if(!isbusy){
                    // Storing data into SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("Busy",MODE_PRIVATE);

                    // Creating an Editor object to edit(write to the file)
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();

                    // Storing the key and its value as the data fetched from edittext

                    // Set as busy as it is now going to navigate to GuideActivity
                    myEdit.putBoolean("busy", true);
                    myEdit.apply();
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

                        // Return to the server successful message
                        return newFixedLengthResponse("Request succeeded.");

                    } catch (IOException | ResponseException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                //  if the robot is BUSY
                else{
                    return newFixedLengthResponse(Response.Status.CONFLICT, MIME_PLAINTEXT, "This Temi is currently in use, come back later!");
                }
            }
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT,
                    "The requested resource does not exist");
        }
    }
}