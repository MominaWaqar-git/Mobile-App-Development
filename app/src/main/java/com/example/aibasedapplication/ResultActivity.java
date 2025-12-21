package com.example.aibasedapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class ResultActivity extends AppCompatActivity {

    ImageView resultImage;
    TextView predictedLabel, predictionConfidence, treatmentText, precautionsText;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Bind Views
        resultImage = findViewById(R.id.result_image);
        predictedLabel = findViewById(R.id.predicted_label);
        predictionConfidence = findViewById(R.id.prediction_confidence);
        treatmentText = findViewById(R.id.treatment_text);
        precautionsText = findViewById(R.id.precautions_text);
        backButton = findViewById(R.id.back_button);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Receive data from Home_Page
        String label = getIntent().getStringExtra("label");
        float confidence = getIntent().getFloatExtra("confidence", 0f);
        Bitmap image = getIntent().getParcelableExtra("image");

        // Show image if available
        if(image != null){
            resultImage.setImageBitmap(image);
        }

        // Show disease and confidence
        predictedLabel.setText("Disease: " + (label != null ? label : "---"));
        predictionConfidence.setText(String.format("Confidence: %.2f%%", confidence * 100));

        // PlantVillage disease info mapping
        HashMap<String, String[]> diseaseInfo = new HashMap<>();
        diseaseInfo.put("Apple___Apple_scab", new String[]{
                "Remove infected leaves and apply appropriate fungicide.",
                "Avoid overhead watering and sanitize tools."
        });
        diseaseInfo.put("Apple___Healthy", new String[]{
                "No treatment required.",
                "Maintain good plant care practices."
        });
        diseaseInfo.put("Tomato___Early_blight", new String[]{
                "Remove affected leaves and apply fungicide.",
                "Avoid overhead watering and rotate crops."
        });
        diseaseInfo.put("Potato___Late_blight", new String[]{
                "Apply fungicides and remove infected plants.",
                "Ensure good ventilation."
        });
        // Add all other labels as needed

        // Set Treatment & Precautions
        if (label != null && diseaseInfo.containsKey(label)) {
            String[] info = diseaseInfo.get(label);
            treatmentText.setText(info[0]);
            precautionsText.setText(info[1]);
        } else {
            treatmentText.setText("No treatment information available.");
            precautionsText.setText("Please consult an expert.");
        }
    }
}
