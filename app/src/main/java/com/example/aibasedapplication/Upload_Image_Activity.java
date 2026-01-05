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
import java.util.HashMap;

public class Upload_Image_Activity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1;
    private static final int GALLERY_REQUEST = 2;

    ImageView imgPreview;
    Button btnChooseImage, btnPredict;
    Bitmap selectedBitmap;
    ModelHelper modelHelper;

    // ✅ Mapping user-friendly name → full class name
    HashMap<String, String> displayToClass;

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
            Toast.makeText(this, "Model load failed", Toast.LENGTH_LONG).show();
        }

        initDisplayMap();

        btnChooseImage.setOnClickListener(v -> showImagePickerDialog());

        btnPredict.setOnClickListener(v -> {
            if (selectedBitmap == null) {
                Toast.makeText(this, "Please select image first", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                PredictionResult result = modelHelper.predict(selectedBitmap);

                // ✅ Map short name to internal class name
                String displayName = result.disease; // short readable name
                String className = displayToClass.getOrDefault(displayName, "Unknown");

                String[] info = DiseaseInfo.getInfo(className);
                String treatment = info[0];
                String prevention = info[1];

                // Compress image
                Bitmap resized = Bitmap.createScaledBitmap(selectedBitmap, 224, 224, true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 70, stream);

                // Start ResultActivity
                Intent intent = new Intent(this, ResultActivity.class);
                intent.putExtra("image", stream.toByteArray());
                intent.putExtra("disease", displayName); // short name
                intent.putExtra("confidence", result.confidence);
                intent.putExtra("treatment", treatment);
                intent.putExtra("prevention", prevention);

                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Prediction failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initDisplayMap() {
        displayToClass = new HashMap<>();

        // ✅ Pepper Bell
        displayToClass.put("Pepper Bacterial Spot", "Pepper__bell___Bacterial_spot");
        displayToClass.put("Pepper Healthy", "Pepper__bell___healthy");

        // ✅ Potato
        displayToClass.put("Early blight", "Potato___Early_blight");
        displayToClass.put("Late blight", "Potato___Late_blight");
        displayToClass.put("Potato Healthy", "Potato___healthy");

        // ✅ Tomato
        displayToClass.put("Bacterial spot", "Tomato_Bacterial_spot");
        displayToClass.put("Early blight", "Tomato_Early_blight");
        displayToClass.put("Late blight", "Tomato_Late_blight");
        displayToClass.put("Leaf Mold", "Tomato_Leaf_Mold");
        displayToClass.put("Septoria leaf spot", "Tomato_Septoria_leaf_spot");
        displayToClass.put("Spider mites", "Tomato_Spider_mites_Two_spotted_spider_mite");
        displayToClass.put("Target Spot", "Tomato__Target_Spot");
        displayToClass.put("YellowLeaf Curl Virus", "Tomato__Tomato_YellowLeaf__Curl_Virus");
        displayToClass.put("Mosaic virus", "Tomato__Tomato_mosaic_virus");
        displayToClass.put("Healthy", "Tomato_healthy");
    }

    private void showImagePickerDialog() {
        String[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image")
                .setItems(options, (d, i) -> {
                    if (i == 0) openCamera();
                    else openGallery();
                }).show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        } else {
            startActivityForResult(
                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE), CAMERA_REQUEST);
        }
    }

    private void openGallery() {
        String permission = Build.VERSION.SDK_INT >= 33 ?
                Manifest.permission.READ_MEDIA_IMAGES :
                Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission}, GALLERY_REQUEST);
        } else {
            startActivityForResult(
                    new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                    GALLERY_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (res == RESULT_OK && data != null) {
            try {
                if (req == CAMERA_REQUEST && data.getExtras() != null) {
                    selectedBitmap = (Bitmap) data.getExtras().get("data");
                } else if (req == GALLERY_REQUEST) {
                    Uri uri = data.getData();
                    selectedBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), uri);
                }
                imgPreview.setImageBitmap(selectedBitmap);
            } catch (Exception e) {
                Toast.makeText(this, "Image load error", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
