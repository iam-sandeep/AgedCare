package agedcare.com.agedcare;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.Request;
import com.github.kittinunf.fuel.core.Response;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tv_text;
    private ImageView img_bot;
    private AppCompatSpinner spinner;
    private EditText userInput;
    private TextView conversation;
    private ImageView health;
    private ImageView young;

    SpeechRecognizer mSpeechRecognizer;
    Intent mSpeechRecognizerIntent;
    private TextToSpeech mTTS;
    private Button mButtonSpeak;
    private SeekBar mSeekBarPitch;
    private SeekBar mSeekBarSpeed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonSpeak = findViewById(R.id.button_speak);
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.GERMAN);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        mButtonSpeak.setEnabled(true);
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        conversation = findViewById(R.id.conversation);
        conversation.setVisibility(View.GONE);
        mSeekBarPitch = findViewById(R.id.seek_bar_pitch);
        mSeekBarSpeed = findViewById(R.id.seek_bar_speed);



        final TextView conversation = (TextView) findViewById(R.id.conversation);
        checkPermission();
        userInput = (EditText) findViewById(R.id.user_input);
        tv_text = findViewById(R.id.tv_text);
        img_bot = findViewById(R.id.heart_img);



        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null) {
                    userInput.setText(matches.get(0));
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
        findViewById(R.id.button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mSpeechRecognizer.stopListening();
                        userInput.setHint("Find");
                        break;
                    case MotionEvent.ACTION_DOWN:
                        userInput.setText("");
                        userInput.setHint("Listening.....");
                        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
                        break;
                }
                return false;
            }
        });



        final ConversationService myConversationService =
                new ConversationService(
                        "2018-05-25",
                        getString(R.string.username),
                        getString(R.string.password)
                );

        userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int action, KeyEvent keyEvent) {
                tv_text.setVisibility(View.GONE);
                img_bot.setVisibility(View.GONE);
                conversation.setVisibility(View.VISIBLE);


                if (action == EditorInfo.IME_ACTION_SEND) {
                    final String inputText = userInput.getText().toString();

                    conversation.append(Html.fromHtml("<p>You: " + inputText + "</p>"));
                    userInput.setText("");

                    MessageRequest request = new MessageRequest.Builder()
                            .inputText(inputText)
                            .build();

                    myConversationService
                            .message(getString(R.string.workspace), request)
                            .enqueue(new ServiceCallback<MessageResponse>() {
                                @Override
                                public void onResponse(MessageResponse response) {

                                    final String outputText = response.getText().get(0);


                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            conversation.append(
                                                    Html.fromHtml("<p>Advicer:<i> " + outputText + "</i></p>")
                                            );
                                        }
                                    });


                                    if (response.getIntents().get(0).getIntent()
                                            .endsWith("RequestQuote")) {
                                        String quotesURL =
                                                "https://api.forismatic.com/api/1.0/" + "?method=getQuote&format=text&lang=en";
                                        Fuel.get(quotesURL)
                                                .responseString(new Handler<String>() {
                                                    @Override
                                                    public void success(Request request,
                                                                        Response response, String quote) {
                                                        conversation.append(
                                                                Html.fromHtml("<p> " +
                                                                        quote + "</p>")
                                                        );

                                                    }


                                                    @Override
                                                    public void failure(Request request,
                                                                        Response response,
                                                                        FuelError fuelError) {
                                                    }
                                                });
                                    }
                                    mButtonSpeak.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String text = outputText.toString();
                                            float pitch = (float) mSeekBarPitch.getProgress() / 50;
                                            if (pitch < 0.1) pitch = 0.1f;
                                            float speed = (float) mSeekBarSpeed.getProgress() / 50;
                                            if (speed < 0.1) speed = 0.1f;

                                            mTTS.setPitch(pitch);
                                            mTTS.setSpeechRate(speed);

                                            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });


                }

                return false;
            }

        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.call) {
            Intent call = new Intent(Intent.ACTION_DIAL);
            call.setData(Uri.parse("tel:061530752"));
            startActivity(call);
        }

        return true;
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        String item = adapterView.getItemAtPosition(position).toString();
        userInput.setText(item);
    }


    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }

}
