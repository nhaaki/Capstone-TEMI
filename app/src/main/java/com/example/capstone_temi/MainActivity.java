package com.example.capstone_temi;

import android.app.Activity;
import android.content.ContentResolver;
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
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private WebView webView = null;
    public String url = "https://chen-han-np.github.io/Capstone-TEMI-Website-Demo/";
    public Robot robot;

    public String goserver = "http://192.168.0.112:10000";
    public ImageView imageSending;
    public TextView name;

    public ImageButton reload;
    public ImageButton back;
    public Bitmap imageReceived;


    private static final int CAMERA_PIC_REQUEST = 1337;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        // For level 2 feature showcase


        reload = (ImageButton) findViewById(R.id.refresh);
        back = (ImageButton) findViewById(R.id.back);

        robot = Robot.getInstance();

        Button go = findViewById(R.id.go);

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

        /*
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

         */




        this.webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        //---- For Website hosting with INTERNET-----
          //  WebViewClientImpl webViewClient = new WebViewClientImpl(this);
          //  webView.setWebViewClient(webViewClient);
          //  webView.loadUrl(url);

        // For local HTML files
        webView.setWebViewClient(new Callback());
        webView.loadUrl("file:///android_asset/index.html");

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

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAMERA_PIC_REQUEST) {
//            Bitmap image = (Bitmap) data.getExtras().get("data");
//            imageSending.setImageBitmap(image);
//        }
//    }


}