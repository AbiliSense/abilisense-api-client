package com.abilisense.simpleclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.abilisense.sdk.entity.Audio;
import com.abilisense.sdk.mqtt.MqttManager;
import com.abilisense.sdk.service.BaseSoundRecognitionService;
import com.abilisense.sdk.soundrecognizer.DetectorThread;
import com.abilisense.sdk.utils.AbiliConstants;

import java.util.List;

/**
 * Main app purpose is showing how you can work with Abilisense SDK. There are two possibilities,
 * via thread or service. So you can change one after another after button clicking
 */
public class MainActivity extends AppCompatActivity {

    public final static String FINISH_SERVICE_ACTION = "finish-service";
    private final static String API_KEY = "18306df7-3fde-49a1-9bef-6ad7a9d83e7f";

    private final static int ABILISENSE_LOAD_THREAD = 1;
    private final static int ABILISENSE_LOAD_SERVICE = 2;

    private int[] mAllVariants = {ABILISENSE_LOAD_THREAD, ABILISENSE_LOAD_SERVICE};
    private int mVariant;

    private TextView mStartText;

    private DetectorThread mAbilisenseThread;
    private MqttManager mqttManager;

    private boolean mSoundServiceStarted;

    private ServiceFinishedReceiver serviceFinishedReceiver;

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
                startAction();
            }
        });

        serviceFinishedReceiver = new ServiceFinishedReceiver();
        registerReceiver(serviceFinishedReceiver, new IntentFilter(FINISH_SERVICE_ACTION));
    }

    private void startAction() {
        if (!checkPermission()) {
            return;
        }

        switch (mVariant) {
            case ABILISENSE_LOAD_SERVICE:
                stopThread();
                mStartText.setText(R.string.start_service_message);
                startSoundRecognitionService();
                break;
            case ABILISENSE_LOAD_THREAD:
                stopSoundRecognitionService();
                mStartText.setText(R.string.start_thread_message);
                /* We can't start thread right after stopping service, because service need some
                 * time for closing (going through own lifecycle). So we use {@link BroadcastReceiver}
                 * for this purpose
                 * // startThread();
                 */
                if (!BaseSoundRecognitionService.isServiceActive())
                    startThread();
                break;
        }
    }

    private void startThread() {
        mAbilisenseThread = new DetectorThread(mainHandler);
        mAbilisenseThread.start();

        if (mqttManager != null) {
            mqttManager = null;
        }
        mqttManager = new MqttManager(MainActivity.this, mainHandler, API_KEY);
        mqttManager.connect();
    }

    @NonNull
    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseSoundRecognitionService.DETECTION_SEND_RESPONSE:
                    List<Audio> audios = (List<Audio>) msg.obj;
                    mqttManager.send(audios);
                    break;
                case BaseSoundRecognitionService.DETECTION_LOCALE_EVENT:
                    String str = (String) msg.obj;
                    Log.i(AbiliConstants.LOG_TAG, "Locale event: " + str);
                    showToast("Locale event: " + str);
                    break;
            }
        }
    };

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (PermissionUtil.isAudioPermissionGranted(this)) {
                // Permission Granted Already
                return true;
            }
            // Request Permission
            PermissionUtil.requestAudioPermissionActivity(this);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CODE_AUDIO_ACTIVITY
                && PermissionUtil.isAudioPermissionGranted(this)) {
            startAction();
        } else {
            if (PermissionUtil.checkShouldShowRequestPermission(this)) {
                Toast.makeText(this, "Permission Deferred", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startSoundRecognitionService() {
        startService(new Intent(this, SimpleSoundRecognitionService.class));
        mSoundServiceStarted = true;
    }

    private void stopSoundRecognitionService() {
        if (mSoundServiceStarted) {
            stopService(new Intent(this, SimpleSoundRecognitionService.class));
            mSoundServiceStarted = false;
        }
    }

    private void stopThread() {
        if (mAbilisenseThread != null) {
            mAbilisenseThread.stopDetection();
            mAbilisenseThread = null;
        }
        if (mqttManager != null) {
            mqttManager.disconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(serviceFinishedReceiver);
        stopThread();
        stopSoundRecognitionService();
    }

    private synchronized void showToast(final String str) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Service needs some time to stop. After that we want to start our thread
     * because of this app purpose.
     */
    private class ServiceFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FINISH_SERVICE_ACTION)) {
                startThread();
                Log.i(AbiliConstants.LOG_TAG, "ServiceFinishedReceiver");
            }
        }
    }
}
