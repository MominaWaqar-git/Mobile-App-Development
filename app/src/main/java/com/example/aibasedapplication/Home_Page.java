package com.example.aibasedapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Home_Page extends AppCompatActivity {

    Button btnCamera, btnGallery, btnPredict, btnExit;
    ImageView imageView;
    Bitmap selectedBitmap;

    ModelHelper modelHelper;
    List<String> labels = new ArrayList<>();

    // Camera Result
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    selectedBitmap = (Bitmap) result.getData().getExtras().get("data");
                    imageView.setImageBitmap(selectedBitmap);
                }
            });

    // Gallery Result
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    Uri uri = result.getData().getData();
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    imageView.setImageBitmap(selectedBitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        btnCamera = findViewById(R.id.camera);
        btnGallery = findViewById(R.id.gallery);
        btnPredict = findViewById(R.id.predictButton);
        btnExit = findViewById(R.id.exit);
        imageView = findViewById(R.id.imageView);

        modelHelper = new ModelHelper(this);
        loadLabels();

        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
            } else {
                openCamera();
            }
        });

        btnGallery.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            galleryLauncher.launch(i);
        });

        btnPredict.setOnClickListener(v -> {
            if(selectedBitmap != null){
                runPrediction(selectedBitmap);
            } else {
                Toast.makeText(this, "Select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        btnExit.setOnClickListener(v -> finishAffinity());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void runPrediction(Bitmap bitmap){
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        float[][][][] input = new float[1][224][224][3];

        for(int x=0; x<224; x++){
            for(int y=0; y<224; y++){
                int pixel = resized.getPixel(x,y);
                input[0][x][y][0] = ((pixel >> 16) & 0xFF)/255f;
                input[0][x][y][1] = ((pixel >> 8) & 0xFF)/255f;
                input[0][x][y][2] = (pixel & 0xFF)/255f;
            }
        }

        float[][] output = modelHelper.run(input);

        int maxIndex = 0;
        for(int i=0; i<output[0].length; i++){
            if(output[0][i] > output[0][maxIndex]){
                maxIndex = i;
            }
        }

        String result = labels.get(maxIndex);
        float confidence = output[0][maxIndex];

        // Open ResultActivity
        Intent intent = new Intent(Home_Page.this, ResultActivity.class);
        intent.putExtra("image", bitmap);
        intent.putExtra("label", result);
        intent.putExtra("confidence", confidence);
        startActivity(intent);
    }

    private void loadLabels(){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt")));
            String line;
            while((line = br.readLine()) != null){
                labels.add(line);
            }
            br.close();
        }catch(Exception e){ e.printStackTrace(); }
    }
}
