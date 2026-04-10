package com.example.spendsmart;

import android.graphics.Bitmap;
import android.util.Log;
import androidx.annotation.NonNull;

// ה-Import שמקשר למפתח ה-API שלך מה-build.gradle
import com.example.spendsmart.BuildConfig;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.ImagePart;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.TextPart;
import java.util.ArrayList;
import java.util.List;

import kotlin.Result;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

public class GeminiManager {
    private static GeminiManager instance;
    private GenerativeModel gemini;
    private final String TAG = "GeminiManager";

    // בתוך GeminiManager.java
    private GeminiManager() {
        // שינוי השם מ-gemini-pro-vision ל-gemini-1.5-flash
        gemini = new GenerativeModel(
                "gemini-2.5-flash",
                BuildConfig.GEMINI_API_KEY
        );
    }

    public static GeminiManager getInstance() {
        if (instance == null) {
            instance = new GeminiManager();
        }
        return instance;
    }

    /**
     * פונקציה לשליחת טקסט ותמונה ל-Gemini
     */
    public void sendTextWithPhotoPrompt(String prompt, Bitmap photo, GeminiCallBack callback) {
        // יצירת חלקי התוכן (טקסט ותמונה)
        List<Part> parts = new ArrayList<>();
        parts.add(new TextPart(prompt));
        parts.add(new ImagePart(photo));

        Content[] content = new Content[1];
        content[0] = new Content(parts);

        // שליחת הבקשה בצורה אסינכרונית
        gemini.generateContent(content,
                new Continuation<GenerateContentResponse>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return EmptyCoroutineContext.INSTANCE;
                    }

                    @Override
                    public void resumeWith(@NonNull Object result) {
                        if (result instanceof Result.Failure) {
                            Throwable exception = ((Result.Failure) result).exception;
                            Log.e(TAG, "Gemini Error: " + exception.getMessage());
                            callback.onFailure(exception);
                        } else {
                            GenerateContentResponse response = (GenerateContentResponse) result;
                            String responseText = response.getText();
                            Log.d(TAG, "Gemini Success: " + responseText);
                            callback.onSuccess(responseText);
                        }
                    }
                });
    }
}