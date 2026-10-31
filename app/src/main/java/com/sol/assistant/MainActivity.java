package com.sol.assistant;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.BatteryManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private LinearLayout root;
    private TextView statusText;
    private TextView responseText;
    private TextView core;
    private Button listenButton;

    private TextToSpeech tts;
    private SpeechRecognizer recognizer;

    private boolean ttsReady = false;
    private boolean listening = false;

    private static final int AUDIO_PERMISSION = 100;

    private final int RED = Color.rgb(255, 35, 65);
    private final int BG = Color.rgb(8, 8, 11);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        createInterface();
        initializeVoice();

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_PERMISSION
            );
        }
    }

    private void createInterface() {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 35, 28, 30);
        root.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText("SOL");
        title.setTextColor(RED);
        title.setTextSize(38);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("PERSONAL AI ASSISTANT");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(12);
        subtitle.setGravity(Gravity.CENTER);

        core = new TextView(this);
        core.setText("S");
        core.setTextColor(Color.WHITE);
        core.setTextSize(65);
        core.setTypeface(Typeface.DEFAULT_BOLD);
        core.setGravity(Gravity.CENTER);

        GradientDrawable glow = new GradientDrawable();
        glow.setShape(GradientDrawable.OVAL);
        glow.setColor(Color.rgb(35, 5, 13));
        glow.setStroke(4, RED);

        core.setBackground(glow);

        LinearLayout.LayoutParams coreParams =
                new LinearLayout.LayoutParams(230, 230);
        coreParams.setMargins(0, 65, 0, 45);

        statusText = new TextView(this);
        statusText.setText("سول آماده است");
        statusText.setTextColor(RED);
        statusText.setTextSize(17);
        statusText.setGravity(Gravity.CENTER);

        responseText = new TextView(this);
        responseText.setText("سلام! من سول هستم.");
        responseText.setTextColor(Color.WHITE);
        responseText.setTextSize(16);
        responseText.setGravity(Gravity.CENTER);
        responseText.setPadding(10, 25, 10, 25);

        listenButton = createButton("🎙 شروع مکالمه");

        listenButton.setOnClickListener(v -> startListening());

        Button stopButton = createButton("توقف صدا");

        stopButton.setOnClickListener(v -> {
            stopListening();
            if (tts != null) {
                tts.stop();
            }
            statusText.setText("مکالمه متوقف شد");
        });

        Button repeatButton = createButton("تکرار پاسخ");

        repeatButton.setOnClickListener(v -> {
            String text = responseText.getText().toString();
            speak(text);
        });

        root.addView(title);
        root.addView(subtitle);
        root.addView(core, coreParams);
        root.addView(statusText);
        root.addView(responseText);
        root.addView(listenButton);
        root.addView(stopButton);
        root.addView(repeatButton);

        setContentView(root);
    }

    private Button createButton(String text) {

        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(35, 12, 19));
        background.setCornerRadius(35);
        background.setStroke(2, RED);

        button.setBackground(background);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        58
                );

        params.setMargins(0, 8, 0, 8);
        button.setLayoutParams(params);

        return button;
    }

    private void initializeVoice() {

        tts = new TextToSpeech(this, result -> {

            if (result == TextToSpeech.SUCCESS) {

                int languageResult =
                        tts.setLanguage(new Locale("fa", "IR"));

                ttsReady = languageResult !=
                        TextToSpeech.LANG_MISSING_DATA &&
                        languageResult !=
                        TextToSpeech.LANG_NOT_SUPPORTED;

            }

        });

    }

    private void startListening() {

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    AUDIO_PERMISSION
            );

            return;
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            respond("سرویس تشخیص گفتار روی گوشی در دسترس نیست.");
            return;
        }

        stopListening();

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                listening = true;
                statusText.setText("دارم گوش می‌کنم...");
                core.setText("...");
            }

            @Override
            public void onBeginningOfSpeech() {
                statusText.setText("صدات رو شنیدم");
            }

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                listening = false;
                statusText.setText("در حال پردازش...");
            }

            @Override
            public void onError(int error) {
                listening = false;
                core.setText("S");
                statusText.setText("آماده مکالمه");
            }

            @Override
            public void onResults(Bundle results) {

                listening = false;
                core.setText("S");

                ArrayList<String> matches =
                        results.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                        );

                if (matches != null && !matches.isEmpty()) {
                    processCommand(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}

        });

        Intent intent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "fa-IR"
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
        );

        recognizer.startListening(intent);
    }

    private void stopListening() {

        listening = false;

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }

        core.setText("S");
    }

    private void processCommand(String command) {

        String text = command.toLowerCase(Locale.ROOT);

        if (text.contains("سلام")) {
            respond("سلام! من سول هستم. چطور می‌تونم کمکت کنم؟");
        }

        else if (text.contains("ساعت")) {

            String time = new SimpleDateFormat(
                    "HH:mm",
                    Locale.getDefault()
            ).format(new Date());

            respond("الان ساعت " + time است.");
        }

        else if (text.contains("تاریخ")) {

            String date = new SimpleDateFormat(
                    "yyyy/MM/dd",
                    Locale.getDefault()
            ).format(new Date());

            respond("تاریخ امروز " + date است.");
        }

        else if (text.contains("باتری")) {

            BatteryManager batteryManager =
                    (BatteryManager) getSystemService(
                            Context.BATTERY_SERVICE
                    );

            int battery = batteryManager.getIntProperty(
                    BatteryManager.BATTERY_PROPERTY_CAPACITY
            );

            respond("شارژ باتری " + battery + " درصد است.");
        }

        else if (text.contains("اینستاگرام")) {
            openApp("com.instagram.android", "اینستاگرام");
        }

        else if (text.contains("تلگرام")) {
            openApp("org.telegram.messenger", "تلگرام");
        }

        else if (text.contains("واتساپ")) {
            openApp("com.whatsapp", "واتساپ");
        }

        else if (text.contains("صدای مردانه")) {
            respond("برای تغییر صدا باید موتور گفتار گوشی صدای مردانه فارسی داشته باشد.");
        }

        else if (text.contains("صدای زنانه")) {
            respond("برای تغییر صدا باید موتور گفتار گوشی صدای زنانه فارسی داشته باشد.");
        }

        else if (text.contains("خداحافظ")) {
            respond("فعلاً خداحافظ!");
        }

        else {
            respond("متوجه منظورت نشدم. دوباره بگو.");
        }

    }

    private void openApp(String packageName, String appName) {

        Intent launchIntent =
                getPackageManager().getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            startActivity(launchIntent);
            respond(appName + " رو باز کردم.");
        } else {
            respond(appName + " روی گوشی نصب نیست.");
        }

    }

    private void respond(String message) {

        responseText.setText(message);
        statusText.setText("SOL آماده است");
        core.setText("S");

        speak(message);
    }

    private void speak(String message) {

        if (ttsReady && tts != null) {

            tts.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "SOL_RESPONSE"
            );

        }

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == AUDIO_PERMISSION) {

            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                statusText.setText("مجوز میکروفون فعال شد");

            } else {

                statusText.setText("مجوز میکروفون داده نشد");

            }
        }
    }

    @Override
    protected void onDestroy() {

        stopListening();

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        super.onDestroy();
    }
          }
