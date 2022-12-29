package com.example.capstone_temi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    private WebView webView = null;
    public String url = "https://chen-han-np.github.io/Capstone-TEMI-Website-Demo/";

    public Button takePhotoButton;
    public Button sendBut;
    public ImageView imageSending;

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

        reload = findViewById(R.id.refresh);
        back = findViewById(R.id.back);

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