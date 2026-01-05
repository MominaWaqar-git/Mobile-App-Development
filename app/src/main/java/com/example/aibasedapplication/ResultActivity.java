package com.example.aibasedapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    ImageView imgResult;
    TextView txtDisease, txtConfidence, txtTreatment, txtPrevention;
    Button btnBack;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imgResult = findViewById(R.id.imgResult);
        txtDisease = findViewById(R.id.txtDisease);
        txtConfidence = findViewById(R.id.txtConfidence);
        txtTreatment = findViewById(R.id.txtTreatment);
        txtPrevention = findViewById(R.id.txtPrevention);
        btnBack = findViewById(R.id.btnBack);

        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra("image");
        if (byteArray != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imgResult.setImageBitmap(bitmap);
        }

        String disease = intent.getStringExtra("disease");
        float confidence = intent.getFloatExtra("confidence", 0);
        String treatment = intent.getStringExtra("treatment");
        String prevention = intent.getStringExtra("prevention");

        txtDisease.setText("Disease: " + disease);
        txtConfidence.setText("Confidence: " + String.format("%.2f", confidence) + "%");
        txtTreatment.setText("Treatment: " + treatment);
        txtPrevention.setText("Prevention: " + prevention);

        // ✅ TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.9f); // Slightly faster but clear

                String speakText = "The predicted disease is " + disease +
                        " with confidence " + String.format("%.2f", confidence) + " percent." +
                        " Treatment: " + treatment + ". Prevention: " + prevention + ".";

                tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "tts1");
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
