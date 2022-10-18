package com.example.capstone_temi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

//import com.robotemi.sdk.NlpResult;
//import com.robotemi.sdk.Robot;
//import com.robotemi.sdk.TtsRequest;
//import com.robotemi.sdk.activitystream.ActivityStreamPublishMessage;
//import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
//import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
//import com.robotemi.sdk.listeners.OnLocationsUpdatedListener;
//import com.robotemi.sdk.listeners.OnRobotReadyListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, BotDirectingPage.class);
                MainActivity.this.startActivity(myIntent);
            }
        });




    }

//    @Override
//    public void onPublish(@NonNull ActivityStreamPublishMessage activityStreamPublishMessage) {
//
//    }
//
//    @Override
//    public void onConversationAttaches(boolean b) {
//
//    }
//
//    @Override
//    public void onNlpCompleted(@NonNull NlpResult nlpResult) {
//
//    }
//
//    @Override
//    public void onTtsStatusChanged(@NonNull TtsRequest ttsRequest) {
//
//    }
//
//    @Override
//    public void onWakeupWord(@NonNull String s, int i) {
//
//    }
//
//    @Override
//    public void onBeWithMeStatusChanged(@NonNull String s) {
//
//    }
//
//    @Override
//    public void onGoToLocationStatusChanged(@NonNull String s, @NonNull String s1, int i, @NonNull String s2) {
//
//    }
//
//    @Override
//    public void onLocationsUpdated(@NonNull List<String> list) {
//
//    }
//
//    @Override
//    public void onRobotReady(boolean b) {
//
//    }
}