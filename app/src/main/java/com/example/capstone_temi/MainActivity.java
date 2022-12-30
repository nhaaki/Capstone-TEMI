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
import android.webkit.WebSettings;
import android.webkit.WebView;
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

    public Button takePhotoButton;
    public Button sendBut;
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
        takePhotoButton = (Button) findViewById(R.id.takePhotoBut);
        sendBut = (Button) findViewById(R.id.sendBut);
        imageSending = (ImageView) findViewById(R.id.picture);

        reload = (ImageButton) findViewById(R.id.refresh);
        back = (ImageButton) findViewById(R.id.back);

        robot = Robot.getInstance();



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

        WebViewClientImpl webViewClient = new WebViewClientImpl(this);
        webView.setWebViewClient(webViewClient);
        webView.loadUrl(url);

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

        // For level 2 showcase
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                imageActivityResultLauncher.launch(intent);
            }

        });

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


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == CAMERA_PIC_REQUEST) {
//            Bitmap image = (Bitmap) data.getExtras().get("data");
//            imageSending.setImageBitmap(image);
//        }
//    }


}