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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
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
    public String flaskServer;

    public String level;
    public String shelfNo;
    public String bookId;
    public String bookName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        flaskServer = getString(R.string.flaskServer);

        // Change the busy mode to true
        SharedPreferences sharedPreferences = getSharedPreferences("Busy",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("busy", true);
        editor.apply();
        setContentView(R.layout.activity_face_verification);

        goBackBtn = (ImageButton) findViewById(R.id.backBtn2);
        takePicBtn2 = (Button) findViewById(R.id.takePicBtn2);

        Log.w("Httpd", "Web server initialized.");

        // Get book detail from appLink data
        Intent appLinkIntent = getIntent();
        bookId = appLinkIntent.getStringExtra("bookId");
        level = appLinkIntent.getStringExtra("level");
        shelfNo = appLinkIntent.getStringExtra("shelfNo");
        bookName = appLinkIntent.getStringExtra("bookName");

        // A button that skips verification and go to the book shelf
        Button skipverification = findViewById(R.id.skipVerificationBtn);
        skipverification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FaceVerificationActivity.this, GuideActivity.class);
                intent.putExtra("verifiedBookName", bookName);
                intent.putExtra("verifiedLevel", level);
                intent.putExtra("verifiedShelfNo", shelfNo);
                intent.putExtra("verifiedBookId", bookId);
                startActivity(intent);
            }
        });

        // Open the take pic after clicking on the verification button
        imageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            imageReceived = BitmapFactory.decodeFile(currentphotopath);
                            if (imageReceived != null) {

                                // Send the second image in json
                                String requestUrl = flaskServer + "/faceverification";
                                JSONObject postData = new JSONObject();

                                // Encode the bitmap
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                imageReceived.compress(Bitmap.CompressFormat.JPEG, 60, baos);
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
                                        Boolean verified = null;
                                        try {
                                            verified = response.getBoolean("result");

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        if(verified) {
                                            Intent intent = new Intent(FaceVerificationActivity.this, GuideActivity.class);
                                            intent.putExtra("verifiedBookName", bookName);
                                            intent.putExtra("verifiedLevel", level);
                                            intent.putExtra("verifiedShelfNo", shelfNo);
                                            intent.putExtra("verifiedBookId", bookId);
                                            startActivity(intent);
                                        }
                                        else{
                                            Toast.makeText(getApplicationContext(),"Face not verified - try again.",Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(getApplicationContext(),"Face not verified - try again.",Toast.LENGTH_LONG).show();
                                        error.printStackTrace();
                                    }
                                });

                                int TIMEOUT_MS=10000;     //10 seconds
                                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                                        TIMEOUT_MS,
                                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                RequestQueue nameRequestQueue = Volley.newRequestQueue(FaceVerificationActivity.this);
                                nameRequestQueue.add(jsonObjectRequest);
                                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                deleteTempFiles(storageDirectory);
                            }
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
                    Uri imageUri = FileProvider.getUriForFile(FaceVerificationActivity.this, "com.example.capstone_temi.fileprovider", imageFile);
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

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean deleteTempFiles(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteTempFiles(f);
                    } else {
                        f.delete();
                    }
                }
            }
        }
        return file.delete();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences("Busy",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("busy", false);
        editor.apply();
    }

}