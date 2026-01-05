package com.example.aibasedapplication;

public class PredictionResult {
    public String plant;
    public String disease;
    public float confidence;

    public PredictionResult(String plant, String disease, float confidence) {
       // this.plant = plant;
        this.disease = disease;
        this.confidence = confidence;
    }
}
