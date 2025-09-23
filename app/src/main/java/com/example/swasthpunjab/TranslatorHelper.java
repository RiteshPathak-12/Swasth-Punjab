package com.example.swasthpunjab;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.*;

public class TranslatorHelper {

    public static void translateAllText(Activity activity, String targetLang) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(targetLang)
                .build();

        Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    View rootView = activity.getWindow().getDecorView().getRootView();
                    translateViews(rootView, translator);
                })
                .addOnFailureListener(e -> {
                    // Handle model download failure
                });
    }

    private static void translateViews(View view, Translator translator) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            String originalText = tv.getText().toString();
            translator.translate(originalText)
                    .addOnSuccessListener(translated -> tv.setText(translated))
                    .addOnFailureListener(e -> {});
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                translateViews(group.getChildAt(i), translator);
            }
        }
    }
}
