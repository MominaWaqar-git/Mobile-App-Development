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
    TextView txtPlant, txtDisease, txtConfidence;
    Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imgResult = findViewById(R.id.imgResult);
       // txtPlant = findViewById(R.id.txtPlant);
        txtDisease = findViewById(R.id.txtDisease);
        txtConfidence = findViewById(R.id.txtConfidence);
        btnBack = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra("image");
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imgResult.setImageBitmap(bitmap);
        }

       // txtPlant.setText("Plant: " + intent.getStringExtra("plant"));
        txtDisease.setText("Disease: " + intent.getStringExtra("disease"));
        txtConfidence.setText("Confidence: " + String.format("%.2f", intent.getFloatExtra("confidence", 0)) + "%");
        btnBack.setOnClickListener(v -> finish());
    }
}
