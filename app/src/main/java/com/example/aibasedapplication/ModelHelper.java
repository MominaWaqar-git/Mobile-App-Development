package com.example.aibasedapplication;

import android.content.Context;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ModelHelper {

    Interpreter interpreter;

    public ModelHelper(Context context){
        try{
            interpreter = new Interpreter(loadModel(context));
        }catch(Exception e){ e.printStackTrace(); }
    }

    private MappedByteBuffer loadModel(Context context) throws Exception{
        FileInputStream inputStream = new FileInputStream(context.getAssets().openFd("plant_disease_model.tflite").getFileDescriptor());
        FileChannel channel = inputStream.getChannel();
        long start = context.getAssets().openFd("plant_disease_model.tflite").getStartOffset();
        long length = context.getAssets().openFd("plant_disease_model.tflite").getDeclaredLength();
        return channel.map(FileChannel.MapMode.READ_ONLY,start,length);
    }

    public float[][] run(float[][][][] input){
        float[][] output = new float[1][38]; // number of classes
        interpreter.run(input, output);
        return output;
    }
}
