package com.example.aibasedapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApi {

    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    Call<GeminiResponse> sendMessage(
            @Body GeminiRequest request,
            @Query("key") String apiKey
    );
}