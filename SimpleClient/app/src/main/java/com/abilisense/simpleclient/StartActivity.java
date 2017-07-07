package com.abilisense.simpleclient;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.abilisense.mqtt.MqttManager;
import com.abilisense.soundrecognizer.DetectorThread;
import com.abilisense.soundrecognizer.RecorderThread;

public class StartActivity extends AppCompatActivity {

    private final static int ABILISENSE_LOAD_THREAD = 2;
    private final static int ABILISENSE_LOAD_SERVICE = 1;

    private int[] mAllVariants = {ABILISENSE_LOAD_SERVICE, ABILISENSE_LOAD_THREAD};
    private int mVariant;

    private DetectorThread mAbilisenseThread;
    private boolean mSoundServiceStarted;

    private TextView mStartText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mStartText = (TextView) findViewById(R.id.abilisense_title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVariant++;
                if (mVariant > mAllVariants.length) {
                    mVariant = mAllVariants[0];
                }
                switch (mVariant) {
                    case ABILISENSE_LOAD_SERVICE:
                        stopThread();
                        mStartText.setText(R.string.start_service_message);
                        startSerice();
                        break;
                    case ABILISENSE_LOAD_THREAD:
                        stopSoundRecognitionService();
                        mStartText.setText(R.string.start_thread_message);
                        startThread();
                        break;
                }
            }
        });
    }

    private void startThread() {
        // You can record sound with this thread. See more in docs
        RecorderThread recorderThread = new RecorderThread();
        // You can provide your own manager for outputting our result
        MqttManager mqttManager = new MqttManager(this);

        mAbilisenseThread = new DetectorThread(recorderThread, mqttManager);
        mAbilisenseThread.start();
    }

    private void startSerice() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (PermissionUtil.isAudioPermissionGranted(this)) {
                //Permission Granted Already
                startSoundRecognitionService();
            } else {
                //Request Permission
                PermissionUtil.requestAudioPermissionActivity(this);
            }
        } else {
            startSoundRecognitionService();
        }
    }

    private void startSoundRecognitionService() {
        startService(new Intent(this, SimpleSoundRecognitionService.class));
        mSoundServiceStarted = true;
    }

    private void stopSoundRecognitionService() {
        if (mSoundServiceStarted) {
            stopService(new Intent(this, SimpleSoundRecognitionService.class));
        }
    }

    private void stopThread() {
        if (mAbilisenseThread != null) {
            mAbilisenseThread.stopDetection();
            mAbilisenseThread = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopThread();
        stopSoundRecognitionService();
    }

}
