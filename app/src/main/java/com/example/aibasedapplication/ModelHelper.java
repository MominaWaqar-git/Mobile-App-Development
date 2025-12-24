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

    public ModelHelper(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
        labels = loadLabels(context);
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("plant_disease_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel channel = inputStream.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) labelList.add(line);
        reader.close();
        return labelList;
    }

    public PredictionResult predict(float[][][][] input) {
        float[][] output = new float[1][labels.size()];
        interpreter.run(input, output);

        int maxIndex = 0;
        float maxProb = 0f;
        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIndex = i;
            }
        }

        if (maxProb < 0.6f) return new PredictionResult("Unknown", "Unsupported leaf", maxProb * 100);

        String fullLabel = labels.get(maxIndex); // e.g., Tomato___Early_blight
        String[] parts = fullLabel.split("___");
        String plant = parts[0].replace("_", " ");
        String disease = parts.length > 1 ? parts[1].replace("_", " ") : parts[0].replace("_", " ");

        return new PredictionResult(plant, disease, maxProb * 100f);
    }
}
