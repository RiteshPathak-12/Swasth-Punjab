package com.example.swasthpunjab;

import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import java.util.ArrayList;

public class ChatbotActivity extends AppCompatActivity {

    TextToSpeech tts;
    Spinner genderSpinner, durationSpinner;
    CheckBox feverBox, coughBox, headacheBox, stomachBox;
    SeekBar severityBar;
    EditText ageInput, locationInput, medicationInput;
    Button submitButton,Consult_a_Doctor;
    TextView resultText;
    Button micButton;
    SpeechRecognizer speechRecognizer;
    Intent speechIntent;
    EditText symptomInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

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
        Consult_a_Doctor=findViewById(R.id.Consult_a_Doctor);

        micButton = findViewById(R.id.micButton);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN"); // You can switch to "hi-IN" or "pa-IN"

        micButton.setOnClickListener(v -> {
            speechRecognizer.startListening(speechIntent);
        });
        Consult_a_Doctor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.jit.si/SwasthPunjabConsultRoom"));
                startActivity(intent);
            }
        });
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    symptomInput.setText(spokenText);
                    tts.speak("You said: " + spokenText, TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }

            // Optional: Handle other callbacks
            public void onReadyForSpeech(Bundle params) {}
            public void onBeginningOfSpeech() {}
            public void onRmsChanged(float rmsdB) {}
            public void onBufferReceived(byte[] buffer) {}
            public void onEndOfSpeech() {}
            public void onError(int error) {}
            public void onPartialResults(Bundle partialResults) {}
            public void onEvent(int eventType, Bundle params) {}
        });

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.ENGLISH); // You can switch to Hindi or Punjabi
            }
        });

        submitButton.setOnClickListener(v -> {
            String advice = getTriageAdvice();
            resultText.setText(advice);
            tts.speak(advice, TextToSpeech.QUEUE_FLUSH, null, null);
        });
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