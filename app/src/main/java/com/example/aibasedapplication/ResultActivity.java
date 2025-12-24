package com.example.aibasedapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    ImageView imgResult;
    TextView txtDisease, txtConfidence;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imgResult = findViewById(R.id.imgResult);
        txtDisease = findViewById(R.id.txtDisease);
        txtConfidence = findViewById(R.id.txtConfidence);
        btnBack = findViewById(R.id.btnBack);

        // Get Data
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        imgResult.setImageBitmap(bitmap);

        String disease = getIntent().getStringExtra("disease");
        float confidence = getIntent().getFloatExtra("confidence", 0);

        txtDisease.setText("Disease: " + disease);
        txtConfidence.setText("Confidence: " + String.format("%.2f", confidence) + "%");

        btnBack.setOnClickListener(v -> {
            Intent i = new Intent(ResultActivity.this, Upload_Image_Activity.class);
            startActivity(i);
        });
    }
}
