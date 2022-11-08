package com.example.capstone_temi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create a WebSocket. The scheme part can be one of the following:
        // 'ws', 'wss', 'http' and 'https' (case-insensitive). The user info
        // part, if any, is interpreted as expected. If a raw socket failed
        // to be created, an IOException is thrown.
        Thread myThread = new Thread(new MyServerThread());
        myThread.start();
    }


    class MyServerThread implements Runnable{
        private PrintWriter output;
        private BufferedReader input;
        Handler h = new Handler();
        String message;
        public void run() {
            Socket socket;
            try {
                socket = new Socket("10.0.2.2", 8000);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                message = input.readLine();
                String line = "";
                while((line = input.readLine()) != null)
                {
                    String finalLine = line;
                    h.post(() -> Toast.makeText(getApplicationContext(), finalLine, Toast.LENGTH_SHORT).show());
                }




            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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