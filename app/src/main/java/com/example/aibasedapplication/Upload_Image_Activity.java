package com.example.aibasedapplication;

import android.Manifest;
import android.app.AlertDialog;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Upload_Image_Activity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;

    ImageView imgPreview;
    Button btnChooseImage, btnPredict;
    Bitmap selectedBitmap;
    ModelHelper modelHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image);

        imgPreview = findViewById(R.id.imgPreview);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        btnPredict = findViewById(R.id.btnPredict);

        try {
            modelHelper = new ModelHelper(this);
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_LONG).show();
        }

        btnChooseImage.setOnClickListener(v -> showImagePickerDialog());

        btnPredict.setOnClickListener(v -> {
            if (selectedBitmap != null) {
                try {
                    PredictionResult result = modelHelper.predict(selectedBitmap);

                    // Resize & compress before sending
                    Bitmap sendBitmap = Bitmap.createScaledBitmap(selectedBitmap, 224, 224, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    sendBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                    byte[] byteArray = stream.toByteArray();

                    Intent intent = new Intent(this, ResultActivity.class);
                    intent.putExtra("image", byteArray);
                    intent.putExtra("plant", result.plant);
                    intent.putExtra("disease", result.disease);
                    intent.putExtra("confidence", result.confidence);
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Prediction failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Please choose an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openCamera();
                    else openGallery();
                }).show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_REQUEST);
        }
    }

    private void openGallery() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_IMAGES : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, GALLERY_REQUEST);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            try {
                if (requestCode == CAMERA_REQUEST && data.getExtras() != null) {
                    selectedBitmap = (Bitmap) data.getExtras().get("data");
                } else if (requestCode == GALLERY_REQUEST) {
                    Uri imageUri = data.getData();
                    if (imageUri != null)
                        selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                }
                if (selectedBitmap != null)
                    imgPreview.setImageBitmap(selectedBitmap);
                else
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_LONG).show();
            }
        }
    }
}
