package com.java.sunxun.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import com.iflytek.cloud.*;
import com.java.sunxun.R;

public class SpeechRecognition {
    private static final String PERMISSION = Manifest.permission.RECORD_AUDIO;

    public static void bindViewToSpeechRecognizer(@NonNull Activity activity, @NonNull View view, boolean asrPtt, @NonNull SpeechRecognitionCallback callback) {
        if (activity.checkSelfPermission(PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.voice_input_request_title)
                    .setMessage(R.string.voice_input_request_message)
                    .setOnDismissListener(v -> activity.requestPermissions(new String[]{PERMISSION}, 0x0010))
                    .show();
        }

        try {
            SpeechRecognizer recognizer = SpeechRecognizer.createRecognizer(activity.getApplicationContext(), i -> {
            });

            view.setOnTouchListener((v, event) -> {
                try {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            if (activity.checkSelfPermission(PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                                Snackbar.make(v, R.string.voice_input_request_denied, Snackbar.LENGTH_SHORT).show();
                                return false;
                            }

                            try {
                                recognizer.setParameter(SpeechConstant.CLOUD_GRAMMAR, null);
                                recognizer.setParameter(SpeechConstant.SUBJECT, null);
                                recognizer.setParameter(SpeechConstant.RESULT_TYPE, "plain");
                                recognizer.setParameter(SpeechConstant.ENGINE_TYPE, "cloud");
                                recognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
                                recognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
                                recognizer.setParameter(SpeechConstant.VAD_BOS, "4000");
                                recognizer.setParameter(SpeechConstant.VAD_EOS, "1000");
                                recognizer.setParameter(SpeechConstant.ASR_PTT, asrPtt ? "1" : "0");
                                recognizer.startListening(new RecognizerListener() {
                                    @Override
                                    public void onVolumeChanged(int i, byte[] bytes) {

                                    }

                                    @Override
                                    public void onBeginOfSpeech() {

                                    }

                                    @Override
                                    public void onEndOfSpeech() {

                                    }

                                    @Override
                                    public void onResult(RecognizerResult recognizerResult, boolean b) {
                                        try {
                                            callback.onNewText(recognizerResult.getResultString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    @Override
                                    public void onError(SpeechError speechError) {
                                        Snackbar.make(v, speechError.getErrorDescription(), Snackbar.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onEvent(int i, int i1, int i2, Bundle bundle) {

                                    }
                                });
                            } catch (Exception e) {
                                Snackbar.make(v, R.string.voice_input_module_broken, Snackbar.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                            break;
                        }
                        case MotionEvent.ACTION_UP: {
                            if (recognizer.isListening()) recognizer.stopListening();
                            v.performClick();
                            break;
                        }
                    }
                } catch (Exception e) {
                    Snackbar.make(view, R.string.voice_input_module_broken, Snackbar.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                return false;
            });

            view.setOnClickListener(v -> {
            });
        } catch (Exception e) {
            Snackbar.make(view, R.string.voice_input_module_broken, Snackbar.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public interface SpeechRecognitionCallback {
        void onNewText(String text);
    }
}
