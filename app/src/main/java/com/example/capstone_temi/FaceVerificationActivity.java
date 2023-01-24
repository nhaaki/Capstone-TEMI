package com.example.capstone_temi;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class FaceVerificationActivity extends AppCompatActivity {

    public TextView name;
    public ImageButton goBackBtn;
    public Button takePicBtn2;
    public ActivityResultLauncher<Intent> imageActivityResultLauncher;
    public Bitmap imageReceived;
    private String currentphotopath;
    //public String goserver = "http://172.20.10.7:8080";
    public String goserver = "http://192.168.43.244:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_verification);

        goBackBtn = (ImageButton) findViewById(R.id.backBtn2);
        takePicBtn2 = (Button) findViewById(R.id.takePicBtn2);


        imageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            imageReceived = (Bitmap) data.getExtras().get("data");

                            CountDownTimer waitTimer;
                            waitTimer = new CountDownTimer(3000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    if (imageReceived != null) {
                                        // Send the image in json
                                        String requestUrl = goserver + "/receiveimage";
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
                                                Log.v("jy", "ugu");
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                error.printStackTrace();
                                            }
                                        });
                                        RequestQueue nameRequestQueue = Volley.newRequestQueue(FaceVerificationActivity.this);
                                        nameRequestQueue.add(jsonObjectRequest);

                                    }
                                }
                                public void onFinish() {
                                    Intent intent = new Intent(FaceVerificationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            }.start();

                        }
                    }

                });


        takePicBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName = "photo";
                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                try{
                    File imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                    currentphotopath = imageFile.getAbsolutePath();
                    Uri imageUri = FileProvider.getUriForFile(FaceVerificationActivity.this, "com.example.myapplication.fileprovider", imageFile);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
                    imageActivityResultLauncher.launch(intent);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
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