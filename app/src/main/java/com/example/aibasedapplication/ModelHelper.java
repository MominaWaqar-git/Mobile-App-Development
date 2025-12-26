package com.example.aibasedapplication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ModelHelper {

    private Interpreter interpreter;
    private List<String> labels;

    private static final float CONFIDENCE_THRESHOLD = 0.70f;

    public ModelHelper(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
        labels = loadLabels(context);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fd = context.getAssets().openFd("model.tflite");
        FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
        FileChannel channel = fis.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(context.getAssets().open("labelss.txt"))
        );
        String line;
        while ((line = br.readLine()) != null) {
            list.add(line.trim().toLowerCase());
        }
        br.close();
        return list;
    }

    public PredictionResult predict(float[][][][] input) {

        float[][] output = new float[1][labels.size()];
        interpreter.run(input, output);

        int maxIndex = 0;
        float maxProb = output[0][0];

        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIndex = i;
            }
        }

        if (maxProb < CONFIDENCE_THRESHOLD) {
            return new PredictionResult("Unknown", "Not a leaf image", maxProb * 100);
        }

        String label = labels.get(maxIndex);

        if (label.contains("background")) {
            return new PredictionResult("Unknown", "Unsupported image", maxProb * 100);
        }

        // ✅ correct parsing for your labels
        String[] words = label.split(" ");

        String plant = capitalize(words[0]);
        String disease;

        if (words.length <= 2) {
            disease = "Healthy";
        } else {
            disease = capitalizeWords(label.replace(words[0], "").trim());
        }

        return new PredictionResult(plant, disease, maxProb * 100);
    }

    private String capitalize(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    private String capitalizeWords(String text) {
        StringBuilder sb = new StringBuilder();
        for (String w : text.split(" ")) {
            sb.append(capitalize(w)).append(" ");
        }
        return sb.toString().trim();
    }
}
