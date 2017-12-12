package com.abilisense.simpleclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
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

import com.abilisense.sdk.service.BaseSoundRecognitionService;
import com.abilisense.sdk.utils.AbiliConstants;
import com.abilisense.simpleclient.util.BootTools;
import com.abilisense.simpleclient.util.SimpleUtils;

/**
 * Main app purpose is showing how you can work with Abilisense SDK. There are two possibilities,
 * via thread or service. So you can change one after another after button clicking
 */
public class MainActivity extends AppCompatActivity {

    public final static String FINISH_SERVICE_ACTION = "finish-service";
    private final static String API_KEY = "e87b4d0c-697e-4ad8-bb26-05ef60cd1efe";

    private TextView mStartText;
    private EditText textNumberPhone;
    private FloatingActionButton fab;
    private boolean mSoundServiceStarted = false;

    private ServiceFinishedReceiver serviceFinishedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Thread.setDefaultUncaughtExceptionHandler(new BootTools.ExceptionHandler(this));
        if (getIntent().getBooleanExtra("crash", false)) {
            Toast.makeText(this, "Restart after crash", Toast.LENGTH_SHORT).show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStartText = (TextView) findViewById(R.id.abilisense_title);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopService();
            }
        });

        serviceFinishedReceiver = new ServiceFinishedReceiver();
        registerReceiver(serviceFinishedReceiver, new IntentFilter(FINISH_SERVICE_ACTION));
        checkSMSPermission();
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            final int level = intent.getIntExtra("level", -1);
            final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            if (level != -1 && level <= SimpleUtils.CHECK_BATTERY_LEVEL
                    && status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                SimpleUtils.showBatteryNotification(context, level);
                context.unregisterReceiver(this);
            }
            Log.i(AbiliConstants.LOG_TAG, String.valueOf(level) + "%");
        }
    };

    private void startStopService() {
        if (!checkPermission()) {
            return;
        }

        if (!mSoundServiceStarted) {
            mStartText.setText(R.string.start_service_message);
            if (!BaseSoundRecognitionService.isServiceActive()) {
                startSoundRecognitionService();
                fab.setImageResource(android.R.drawable.ic_media_pause);
            }
        } else {
            fab.setImageResource(android.R.drawable.ic_media_play);
            stopSoundRecognitionService();
            mStartText.setText(R.string.stop_service_message);
        }
    }

    public void getContactIntent() {
        if (checkContactsPermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri uriContact = data.getData();
            retrieveContactNumber(getContactId(uriContact));
        }
    }

    private void retrieveContactNumber(String contactId) {
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, new String[]{contactId}, null);

        String contactNumber = null;
        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        textNumberPhone.setText(contactNumber);
        cursorPhone.close();
    }

    private String getContactId(Uri uriContact) {
        String contactId = "";
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {
            contactId = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }
        cursorID.close();
        return contactId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        textNumberPhone = new EditText(this);
        textNumberPhone.setHint("click for open contacts");
        textNumberPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getContactIntent();
            }
        });
        final EditText editText = new EditText(this);

        final SharedPreferences pref = getSharedPreferences(SimpleUtils.PREFERENCE_NAME,
                Context.MODE_PRIVATE);

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.edit_sms_recipient:
                textNumberPhone.setText(pref.getString(SimpleUtils.RECIPIENT_PHONE_NUMBER_FIELD_NAME, ""));
                new AlertDialog.Builder(this)
                        .setTitle("Recipient phone number")
                        .setMessage("Please enter recipient phone number")
                        .setView(textNumberPhone)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String number = textNumberPhone.getText().toString();
                                pref.edit().putString(SimpleUtils.RECIPIENT_PHONE_NUMBER_FIELD_NAME, number).apply();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
                return true;
            case R.id.edit_device_name:
                editText.setText(pref.getString(SimpleUtils.DEVICE_NAME_FIELD_NAME, ""));
                new AlertDialog.Builder(this)
                        .setTitle("Device Name")
                        .setMessage("Please enter device name")
                        .setView(editText)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String number = editText.getText().toString();
                                pref.edit().putString(SimpleUtils.DEVICE_NAME_FIELD_NAME, number).apply();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }).show();
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

    private boolean checkContactsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (SimpleUtils.isContactsPermissionGranted(this)) {
                // Permission Granted Already
                return true;
            }
            // Request Permission
            SimpleUtils.requestContactsPermissionActivity(this);
        } else {
            return true;
        }
        return false;
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (SimpleUtils.isAudioPermissionGranted(this)) {
                // Permission Granted Already
                return true;
            }
            // Request Permission
            SimpleUtils.requestAudioPermissionActivity(this);
        } else {
            return true;
        }
        return false;
    }

    private boolean checkSMSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (SimpleUtils.isSendSMSPermissionGranted(this)) {
                // Permission Granted Already
                return true;
            }
            // Request Permission
            SimpleUtils.requestSendSMSPermissionActivity(this);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case SimpleUtils.PERMISSION_REQUEST_CODE_AUDIO_ACTIVITY:
                if (SimpleUtils.isAudioPermissionGranted(this)) {
                    startStopService();
                } else {
                    handleNotGrantedPermission();
                }
                break;
            case SimpleUtils.PERMISSION_REQUEST_CODE_SEND_SMS:
                if (SimpleUtils.isSendSMSPermissionGranted(this)) {
                    Toast.makeText(this, "Send SMS permission granted!", Toast.LENGTH_LONG).show();
                } else {
                    handleNotGrantedPermission();
                }
                break;
            case SimpleUtils.PERMISSION_REQUEST_CODE_READ_CONTACT:
                if (SimpleUtils.isContactsPermissionGranted(this)) {
                    getContactIntent();
                } else {
                    handleNotGrantedPermission();
                }
                break;
        }
    }

    private void handleNotGrantedPermission() {
        if (SimpleUtils.checkShouldShowRequestPermission(this)) {
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
                startStopService();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.mBatInfoReceiver);
    }
}
