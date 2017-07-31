package com.abilisense.simpleclient;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.abilisense.sdk.soundrecognizer.DetectorThread;

public class MainActivity extends AppCompatActivity {

    private final static int ABILISENSE_LOAD_THREAD = 1;
    private final static int ABILISENSE_LOAD_SERVICE = 2;

    private int[] mAllVariants = {ABILISENSE_LOAD_THREAD, ABILISENSE_LOAD_SERVICE};
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
        mAbilisenseThread = new DetectorThread(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case 1:
                        break;
                }
            }
        });
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
