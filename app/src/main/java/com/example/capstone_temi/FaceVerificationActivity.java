package com.example.capstone_temi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.FaceDetector;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class FaceVerificationActivity extends AppCompatActivity {

    public TextView name;
    public ImageButton goBackBtn;
    public Button takePicBtn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_verification);

        goBackBtn = (ImageButton) findViewById(R.id.backBtn2);
        takePicBtn2 = (Button) findViewById(R.id.takePicBtn2);

        takePicBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add code for opening camera
            }
        });

        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FaceVerificationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}