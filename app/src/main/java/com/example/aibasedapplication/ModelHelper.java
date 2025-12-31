package com.example.aibasedapplication;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class ModelHelper {

    private Interpreter interpreter;
    private List<String> labels;
    private static final int IMG_SIZE = 224;

    public ModelHelper(Context context) throws IOException {
        interpreter = new Interpreter(loadModel(context));
        labels = loadLabels(context);
    }

    private MappedByteBuffer loadModel(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("plant_disease_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel channel = inputStream.getChannel();
        return channel.map(FileChannel.MapMode.READ_ONLY,
                fileDescriptor.getStartOffset(),
                fileDescriptor.getDeclaredLength());
    }

    private List<String> loadLabels(Context context) throws IOException {
        List<String> list = new ArrayList<>();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("labelsss.txt"))
        );
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty()) {
                list.add(line.trim());
            }
        }
        reader.close();
        return list;
    }

    // ✅ NEW: Plant presence check (background rejection)
    private boolean isPlantPresent(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int greenCount = 0;

        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xFF;
            int g = (pixel >> 8) & 0xFF;
            int b = pixel & 0xFF;

            if (g > r && g > b && g > 80) {
                greenCount++;
            }
        }

        float greenRatio = (float) greenCount / pixels.length;
        return greenRatio > 0.15f; // 15% green required
    }

    public PredictionResult predict(Bitmap bitmap) throws Exception {
        if (bitmap == null) throw new Exception("Bitmap is null");

        // ✅ STEP 1: Background check
        if (!isPlantPresent(bitmap)) {
            return new PredictionResult(
                    "No Plant",
                    "Background Image",
                    0f
            );
        }

        Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMG_SIZE, IMG_SIZE, true);

        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * IMG_SIZE * IMG_SIZE * 3);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[IMG_SIZE * IMG_SIZE];
        resized.getPixels(pixels, 0, IMG_SIZE, 0, 0, IMG_SIZE, IMG_SIZE);

        for (int pixel : pixels) {
            float r = ((pixel >> 16) & 0xFF) / 255.0f;
            float g = ((pixel >> 8) & 0xFF) / 255.0f;
            float b = (pixel & 0xFF) / 255.0f;

            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }
        inputBuffer.rewind();

        float[][] output = new float[1][labels.size()];
        interpreter.run(inputBuffer, output);

        int maxIndex = 0;
        float maxProb = output[0][0];
        for (int i = 1; i < output[0].length; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIndex = i;
            }
        }

        float confidence = maxProb * 100f;

        // ✅ STEP 2: Confidence threshold
        if (confidence < 60f) {
            return new PredictionResult(
                    "No Plant",
                    "Background Image",
                    confidence
            );
        }

        String rawLabel = labels.get(maxIndex);
        String plant;
        String disease;

        if (rawLabel.contains("___")) {
            String[] parts = rawLabel.split("___", 2);
            plant = parts[0].replace("_", " ");
            disease = parts[1].replace("_", " ");
        } else if (rawLabel.contains("_")) {
            String[] parts = rawLabel.split("_", 2);
            plant = parts[0];
            disease = parts[1].replace("_", " ");
        } else {
            plant = rawLabel;
            disease = "healthy";
        }

        return new PredictionResult(plant, disease, confidence);
    }
}
