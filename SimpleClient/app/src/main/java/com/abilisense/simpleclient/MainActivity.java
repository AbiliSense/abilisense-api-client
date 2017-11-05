package com.abilisense.simpleclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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

    private SharedPreferences pref;

    public final static String FINISH_SERVICE_ACTION = "finish-service";
    private final static String API_KEY = "e87b4d0c-697e-4ad8-bb26-05ef60cd1efe";

    private final static int ABILISENSE_LOAD_THREAD = 1;
    private final static int ABILISENSE_LOAD_SERVICE = 2;

    private int[] mAllVariants = {ABILISENSE_LOAD_THREAD, ABILISENSE_LOAD_SERVICE};
    private int mVariant = ABILISENSE_LOAD_THREAD;

    private TextView mStartText;

    private DetectorThread mAbilisenseThread;
    private MqttManager mqttManager;

    private boolean mSoundServiceStarted = false;

    private ServiceFinishedReceiver serviceFinishedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mStartText = (TextView) findViewById(R.id.abilisense_title);

        pref = getSharedPreferences(ClientConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mVariant++;
                if(mSoundServiceStarted) {
                    fab.setImageResource(android.R.drawable.ic_media_play);
                    mVariant = ABILISENSE_LOAD_THREAD;
                } else {
                    fab.setImageResource(android.R.drawable.ic_media_pause);
                    mVariant = ABILISENSE_LOAD_SERVICE;
                }
                startAction();
            }
        });

        serviceFinishedReceiver = new ServiceFinishedReceiver();
        registerReceiver(serviceFinishedReceiver, new IntentFilter(FINISH_SERVICE_ACTION));
        checkSMSPermission();
    }

    private void startAction() {
        if (!checkPermission()) {
            return;
        }

        switch (mVariant) {
            case ABILISENSE_LOAD_SERVICE:
//                stopThread();
                mStartText.setText(R.string.start_service_message);
                if (!BaseSoundRecognitionService.isServiceActive())
                    startSoundRecognitionService();
                break;
            case ABILISENSE_LOAD_THREAD:
                stopSoundRecognitionService();
                mStartText.setText(R.string.stop_service_message);
                /* We can't start thread right after stopping service, because service need some
                 * time for closing (going through own lifecycle). So we use {@link BroadcastReceiver}
                 * for this purpose
                 * // startThread();
                 */
//                if (!BaseSoundRecognitionService.isServiceActive())
//                    startThread();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final EditText editText = new EditText(this);
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.edit_sms_recipient:
                editText.setText(pref.getString(ClientConstants.RECIPIENT_PHONE_NUMBER_FIELD_NAME, ""));
                new AlertDialog.Builder(this)
                        .setTitle("Recipient phone number")
                        .setMessage("Please enter recipient phone number")
                        .setView(editText)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String number = editText.getText().toString();
                                pref.edit().putString(ClientConstants.RECIPIENT_PHONE_NUMBER_FIELD_NAME, number).apply();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return true;
            case R.id.edit_device_name:
                editText.setText(pref.getString(ClientConstants.DEVICE_NAME_FIELD_NAME, ""));
                new AlertDialog.Builder(this)
                        .setTitle("Device Name")
                        .setMessage("Please enter device name")
                        .setView(editText)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String number = editText.getText().toString();
                                pref.edit().putString(ClientConstants.DEVICE_NAME_FIELD_NAME, number).apply();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
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

    private boolean checkSMSPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (PermissionUtil.isSendSMSPermissionGranted(this)) {
                // Permission Granted Already
                return true;
            }
            // Request Permission
            PermissionUtil.requestSendSMSPermissionActivity(this);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionUtil.PERMISSION_REQUEST_CODE_AUDIO_ACTIVITY:
                if(PermissionUtil.isAudioPermissionGranted(this)) {
                    startAction();
                } else {
                    handleNotGrantedPermission();
                }
                break;
            case PermissionUtil.PERMISSION_REQUEST_CODE_SEND_SMS:
                if(PermissionUtil.isSendSMSPermissionGranted(this)) {
                    Toast.makeText(this, "Send SMS permission granted!", Toast.LENGTH_LONG).show();
                } else {
                    handleNotGrantedPermission();
                }
                break;
        }
    }

    private void handleNotGrantedPermission() {
        if (PermissionUtil.checkShouldShowRequestPermission(this)) {
            Toast.makeText(this, "Permission Deferred", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
        }
    }

    private void startSoundRecognitionService() {
        Intent i = new Intent(this, SimpleSoundRecognitionService.class);
        i.putExtra(AbiliConstants.API_KEY, API_KEY);
        startService(i);
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
    protected void onDestroy() {
        stopSoundRecognitionService();
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
        try {
            unregisterReceiver(serviceFinishedReceiver);
        } catch (Exception e) {
            Log.d(this.getClass().getName(), e.getMessage());
        }
//        stopThread();
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
//                startThread();
                Log.i(AbiliConstants.LOG_TAG, "ServiceFinishedReceiver");
            }
        }
    }
}
