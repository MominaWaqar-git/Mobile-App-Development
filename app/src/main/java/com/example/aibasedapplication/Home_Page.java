package com.example.aibasedapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class Home_Page extends AppCompatActivity {

    Button btnCamera, btnGallery, btnInstructions, btnExit;
    ImageView imageView; // Show captured/selected image

    // Permission request launcher
    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(isGranted){
                    openCamera();
                } else {
                    Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
                }
            });

    // Activity result launcher for camera
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK){
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);

                    // TODO: Pass imageBitmap to your ML model here
                    Toast.makeText(this, "Camera Image Captured!", Toast.LENGTH_SHORT).show();
                }
            });

    // Activity result launcher for gallery
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if(result.getResultCode() == RESULT_OK && result.getData() != null){
                    Uri uri = result.getData().getData();
                    imageView.setImageURI(uri);

                    // TODO: Pass URI to your ML model here
                    Toast.makeText(this, "Gallery Image Selected!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        // Link buttons
        btnCamera = findViewById(R.id.camera);
        btnGallery = findViewById(R.id.gallery);
        btnInstructions = findViewById(R.id.instructions);
        btnExit = findViewById(R.id.exit);
        imageView = findViewById(R.id.imageView);

        // CAMERA BUTTON
        btnCamera.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED){
                requestCameraPermission.launch(Manifest.permission.CAMERA);
            } else {
                openCamera();
            }
        });

        // GALLERY BUTTON
        btnGallery.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");
            galleryLauncher.launch(galleryIntent);
        });

        // INSTRUCTIONS BUTTON
        btnInstructions.setOnClickListener(v -> {
            startActivity(new Intent(Home_Page.this, InstructionActivity.class));
        });

        // EXIT BUTTON
        btnExit.setOnClickListener(v -> finishAffinity());
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }
}
