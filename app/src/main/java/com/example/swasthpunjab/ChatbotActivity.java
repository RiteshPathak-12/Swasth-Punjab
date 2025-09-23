package com.example.swasthpunjab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class ChatbotActivity extends AppCompatActivity {

    TextToSpeech tts;
    Spinner genderSpinner, durationSpinner;
    CheckBox feverBox, coughBox, headacheBox, stomachBox;
    SeekBar severityBar;
    EditText ageInput, locationInput, medicationInput, symptomInput;
    Button submitButton, Consult_a_Doctor, micButton;
    TextView resultText;
    SpeechRecognizer speechRecognizer;
    Intent speechIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        // Initialize views
        ageInput = findViewById(R.id.ageInput);
        genderSpinner = findViewById(R.id.genderSpinner);
        durationSpinner = findViewById(R.id.durationSpinner);
        feverBox = findViewById(R.id.feverBox);
        coughBox = findViewById(R.id.coughBox);
        headacheBox = findViewById(R.id.headacheBox);
        stomachBox = findViewById(R.id.stomachBox);
        locationInput = findViewById(R.id.locationInput);
        severityBar = findViewById(R.id.severityBar);
        medicationInput = findViewById(R.id.medicationInput);
        submitButton = findViewById(R.id.submitButton);
        resultText = findViewById(R.id.resultText);
        symptomInput = findViewById(R.id.symptomInput);
        Consult_a_Doctor = findViewById(R.id.Consult_a_Doctor);
        micButton = findViewById(R.id.micButton);

        // Load saved language
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String targetLang = prefs.getString("language", "en");

        // Setup translator
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.fromLanguageTag(targetLang))
                .build();

        Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();

        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(unused -> {
                    View root = findViewById(android.R.id.content);
                    translateViewText(root, translator);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Translation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        // Text-to-Speech setup
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale(targetLang));
            }
        });

        // Speech recognition setup
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "en-IN");

        micButton.setOnClickListener(v -> speechRecognizer.startListening(speechIntent));

        speechRecognizer.setRecognitionListener(new android.speech.RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    symptomInput.setText(spokenText);
                    tts.speak("You said: " + spokenText, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }

            public void onReadyForSpeech(Bundle params) {}
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float rmsdB) {}
            public void onBufferReceived(byte[] buffer) {}
            public void onEndOfSpeech() {}
            public void onError(int error) {}
            public void onPartialResults(Bundle partialResults) {}
            public void onEvent(int eventType, Bundle params) {}
        });

        // Submit button logic
        submitButton.setOnClickListener(v -> {
            String advice = getTriageAdvice();
            resultText.setText(advice);
            tts.speak(advice, TextToSpeech.QUEUE_FLUSH, null, null);
        });

        // Doctor consult button
        Consult_a_Doctor.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.jit.si/SwasthPunjabConsultRoom"));
            startActivity(intent);
        });
    }

    private void translateViewText(View view, Translator translator) {
        if (view instanceof TextView) {
            TextView tv = (TextView) view;
            translator.translate(tv.getText().toString())
                    .addOnSuccessListener(tv::setText);
        } else if (view instanceof Button) {
            Button btn = (Button) view;
            translator.translate(btn.getText().toString())
                    .addOnSuccessListener(btn::setText);
        } else if (view instanceof CheckBox) {
            CheckBox cb = (CheckBox) view;
            translator.translate(cb.getText().toString())
                    .addOnSuccessListener(cb::setText);
        } else if (view instanceof EditText) {
            EditText et = (EditText) view;
            if (et.getHint() != null) {
                translator.translate(et.getHint().toString())
                        .addOnSuccessListener(et::setHint);
            }
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                translateViewText(group.getChildAt(i), translator);
            }
        }
    }

    private String getTriageAdvice() {
        boolean hasFever = feverBox.isChecked();
        boolean hasCough = coughBox.isChecked();
        boolean hasHeadache = headacheBox.isChecked();
        boolean hasStomachPain = stomachBox.isChecked();
        int severity = severityBar.getProgress();

        if (hasFever && hasCough && severity >= 7) {
            return "You may have a serious infection. Please consult a doctor immediately.";
        } else if (hasFever && hasHeadache) {
            return "This may be a mild viral illness. Rest and stay hydrated.";
        } else if (hasStomachPain && severity >= 5) {
            return "You may have a digestive issue. Monitor symptoms and consult if they worsen.";
        } else {
            return "Symptoms are not critical. Please rest and monitor your condition.";
        }
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