package com.example.capstone_temi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.face.ContactModel;
import com.robotemi.sdk.face.OnFaceRecognizedListener;
import com.robotemi.sdk.listeners.OnMovementStatusChangedListener;

import java.util.List;


public class MainActivity extends AppCompatActivity {


    private WebView webView = null;
    public String url = "https://chen-han-np.github.io/Capstone-TEMI-Website-Demo/";
    public Robot robot;

    public Button takePhotoButton;
    public Button sendBut;
    public ImageView imageSending;
    public TextView name;
    public Button reload;
    public Button back;

    private static final int CAMERA_PIC_REQUEST = 1337;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        // For level 2 feature showcase
        takePhotoButton = (Button) findViewById(R.id.takePhotoBut);
        sendBut = (Button) findViewById(R.id.sendBut);
        imageSending = (ImageView) findViewById(R.id.personImage);

        reload = (Button) findViewById(R.id.refresh);
        back = (Button) findViewById(R.id.back);

        robot = Robot.getInstance();

        name = findViewById(R.id.name);




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

                        if(type.equals("turnBy")){
                            if(status.equals("complete")){
                                times += 1;
                                if(times == 2){
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

        // For level 2 showcase
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                startActivity(intent);
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

        Button face = findViewById(R.id.face);

        robot.addOnFaceRecognizedListener(new OnFaceRecognizedListener() {
            @Override
            public void onFaceRecognized(@NonNull List<ContactModel> list) {

                Log.v("urmum", "suck");


                name.setText(list.get(0).getFirstName() + " " + list.get(0).getLastName());
                robot.stopFaceRecognition();

            }
        });

        face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.v("urmum", "jkdn");
            }
        });




    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_PIC_REQUEST) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            imageSending.setImageBitmap(image);
        }
    }





}