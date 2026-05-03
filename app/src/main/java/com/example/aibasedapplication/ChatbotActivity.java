package com.example.aibasedapplication;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.*;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.*;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatbotActivity extends AppCompatActivity {

    LinearLayout chatContainer;
    EditText edtMessage;
    Button btnSend;
    ScrollView scrollChat;
    TextToSpeech tts;
    GeminiApi api;

    String API_KEY = "Your API KEY Here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        chatContainer = findViewById(R.id.chatContainer);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        scrollChat = findViewById(R.id.scrollChat);

        // TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(1.0f);
            }
        });

        // Logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        api = retrofit.create(GeminiApi.class);

        btnSend.setOnClickListener(v -> {
            String userMsg = edtMessage.getText().toString().trim();
            if (userMsg.isEmpty()) return;

            addMessage(userMsg, true);
            edtMessage.setText("");

            sendMessageToGemini(userMsg);
        });
    }

    private void sendMessageToGemini(String msg) {

        // 🌿 Plant expert prompt
        String prompt = "You are a plant expert. Answer briefly: " + msg;

        List<GeminiRequest.Part> parts = new ArrayList<>();
        parts.add(new GeminiRequest.Part(prompt));

        List<GeminiRequest.Content> contents = new ArrayList<>();
        contents.add(new GeminiRequest.Content(parts));

        GeminiRequest request = new GeminiRequest(contents);

        api.sendMessage(request, API_KEY).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {

                if (response.isSuccessful() && response.body() != null
                        && response.body().candidates != null
                        && !response.body().candidates.isEmpty()
                        && response.body().candidates.get(0).content != null
                        && response.body().candidates.get(0).content.parts != null
                        && !response.body().candidates.get(0).content.parts.isEmpty()) {

                    String reply = response.body().candidates
                            .get(0).content.parts.get(0).text;

                    addMessage(reply, false);
                    tts.speak(reply, TextToSpeech.QUEUE_FLUSH, null, "tts1");

                } else {
                    Toast.makeText(ChatbotActivity.this,
                            "Empty response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage());
                Toast.makeText(ChatbotActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessage(String msg, boolean isUser) {

        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(16f);
        tv.setPadding(24, 16, 24, 16);

        tv.setTextColor(isUser ? 0xFFFFFFFF : 0xFF1B5E20);
        tv.setBackgroundResource(isUser ? R.drawable.user_bubble : R.drawable.bot_bubble);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        params.setMargins(16, 8, 16, 8);
        params.gravity = isUser ? Gravity.END : Gravity.START;

        tv.setLayoutParams(params);

        chatContainer.addView(tv);

        scrollChat.post(() -> scrollChat.fullScroll(ScrollView.FOCUS_DOWN));
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