package com.automusic.automotivemusicplayer.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;

public class VoiceCommandManager {

    private final SpeechRecognizer speechRecognizer;
    private final Intent speechRecognizerIntent;
    private final VoiceCallback callback;

    // Interface để gửi kết quả văn bản về cho PlayerActivity
    public interface VoiceCallback {
        void onCommandRecognized(String command);
        void onError(String error);
    }

    public VoiceCommandManager(Context context, VoiceCallback callback) {
        this.callback = callback;
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);

        // Cài đặt ngôn ngữ tiếng Việt
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}
            @Override
            public void onBeginningOfSpeech() {}
            @Override
            public void onRmsChanged(float rmsdB) {}
            @Override
            public void onBufferReceived(byte[] buffer) {}
            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                callback.onError("Lỗi nhận diện: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                // Lấy danh sách các câu Android nghe được
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    // Lấy kết quả chính xác nhất (đứng đầu danh sách)
                    String bestMatch = matches.get(0).toLowerCase();
                    callback.onCommandRecognized(bestMatch);
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}
            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    public void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}