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
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

    private final static String DEFAULT_API_KEY = "e87b4d0c-697e-4ad8-bb26-05ef60cd1efe";
    private static final int INTENT_CONTACT_REQUEST_CODE = 1010;

    private TextView mStartText;
    private FloatingActionButton fab;

    private TextView phonesTextView;

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
        if (SimpleSoundRecognitionService.isServiceActive()) {
            fab.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            fab.setImageResource(android.R.drawable.ic_media_play);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStopService();
            }
        });

        serviceFinishedReceiver = new ServiceFinishedReceiver();
        registerReceiver(serviceFinishedReceiver, new IntentFilter(SimpleUtils.FINISH_SERVICE_ACTION));
        checkSMSPermission();
    }

    private void startStopService() {
        if (!checkAudioPermission()) {
            return;
        }

        if (!BaseSoundRecognitionService.isServiceActive()) {
            mStartText.setText(R.string.start_service_message);
            startSoundRecognitionService();
            fab.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            mStartText.setText(R.string.stop_service_message);
            stopSoundRecognitionService();
            fab.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uriContact = data.getData();
            retrieveContactNumber(getContactId(uriContact));
        }
    }

    private void retrieveContactNumber(String contactId) {
        Cursor cursorPhone = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                new String[]{contactId}, null);

        String contactNumber = null;
        if (cursorPhone != null && cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        addPhoneNumber(contactNumber);
        if (cursorPhone != null) {
            cursorPhone.close();
        }
    }

    private void addPhoneNumber(String phone) {
        String allPhones = phonesTextView.getText().toString();
        if (allPhones.length() > 0) {
            allPhones += "; " + phone;
        } else {
            allPhones = phone;
        }
        phonesTextView.setText(allPhones);
    }

    private String getContactId(Uri uriContact) {
        String contactId = "";
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID != null) {
            if (cursorID.moveToFirst()) {
                contactId = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
            }
            cursorID.close();
        }
        return contactId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SharedPreferences pref = getSharedPreferences(SimpleUtils.PREFERENCE_NAME,
                Context.MODE_PRIVATE);

        switch (item.getItemId()) {
            case R.id.edit_phones_sms:
                createPhonesDialog();
                return true;
            case R.id.edit_device_name:
                createDeviceNameDialog(pref);
                return true;
            case R.id.edit_detection_threshold:
                createDetectionThresholdDialog(pref);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createDetectionThresholdDialog(final SharedPreferences pref) {
        final TextInputEditText inputEditText = new TextInputEditText(this);
        inputEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputEditText.setText(pref.getString(SimpleUtils.DETECTION_THRESHOLD_COUNT, ""));
        new AlertDialog.Builder(this)
                .setTitle(R.string.detection_threshold)
                .setMessage(R.string.detection_threshold_dialog_message)
                .setView(inputEditText)
                .setPositiveButton(R.string.detection_threshold_save_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String number = inputEditText.getText().toString();
                        pref.edit().putString(SimpleUtils.DETECTION_THRESHOLD_COUNT, number).apply();
                    }
                })
                .setNegativeButton(R.string.detection_threshold_cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }

    private void createDeviceNameDialog(final SharedPreferences pref) {
        final EditText editText = new EditText(this);
        editText.setText(pref.getString(SimpleUtils.DEVICE_NAME_FIELD_NAME, ""));
        new AlertDialog.Builder(this)
                .setTitle(R.string.device_name_dialog_title)
                .setMessage(R.string.device_name_dialog_message)
                .setView(editText)
                .setPositiveButton(R.string.device_name_dialog_save_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String number = editText.getText().toString();
                        pref.edit().putString(SimpleUtils.DEVICE_NAME_FIELD_NAME, number).apply();
                    }
                })
                .setNegativeButton(R.string.device_name_dialog_cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }

    private void createPhonesDialog() {
        LinearLayout phonesLayout = new LinearLayout(this);

        final AlertDialog.Builder build = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.phones_dialog_title)
                .setMessage(R.string.phones_dialog_message)
                .setView(phonesLayout);
        final AlertDialog dialog = build.show();

        final SharedPreferences pref = getSharedPreferences(SimpleUtils.PREFERENCE_NAME,
                Context.MODE_PRIVATE);

        View view = LayoutInflater.from(this).inflate(R.layout.item_contact, phonesLayout);
        phonesTextView = view.findViewById(R.id.text_phones);
        TextView textClean = view.findViewById(R.id.text_cleat_btn);
        TextView textContact = view.findViewById(R.id.text_contact_btn);
        TextView textSave = view.findViewById(R.id.text_save_btn);

        textContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendContactIntent();
            }
        });
        textClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phonesTextView.setText("");
            }
        });
        textSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = phonesTextView.getText().toString();
                pref.edit().putString(SimpleUtils.RECIPIENT_PHONE_NUMBER_FIELD_NAME, number).apply();
                dialog.dismiss();
            }
        });
        phonesTextView.setText(pref.getString(SimpleUtils.RECIPIENT_PHONE_NUMBER_FIELD_NAME, ""));
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

    private boolean checkAudioPermission() {
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

    private void checkSMSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (SimpleUtils.isSendSMSPermissionGranted(this)) {
                // Permission Granted Already
                return;
            }
            // Request Permission
            SimpleUtils.requestSendSMSPermissionActivity(this);
        }
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
                    sendContactIntent();
                } else {
                    handleNotGrantedPermission();
                }
                break;
        }
    }

    private void sendContactIntent() {
        if (checkContactsPermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, INTENT_CONTACT_REQUEST_CODE);
        }
    }

    private void handleNotGrantedPermission() {
        if (SimpleUtils.checkShouldShowRequestPermission(this)) {
            Toast.makeText(this, R.string.permissions_deffered, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_LONG).show();
        }
    }

    private void startSoundRecognitionService() {
        ExampleRecorder recorder = new ExampleRecorder();
        SimpleSoundRecognitionService.setRecorder(recorder);

        Intent intent = new Intent(this, SimpleSoundRecognitionService.class);
        intent.putExtra(AbiliConstants.API_KEY, DEFAULT_API_KEY);
        startService(intent);
    }

    private void stopSoundRecognitionService() {
        if (BaseSoundRecognitionService.isServiceActive()) {
            stopService(new Intent(this, SimpleSoundRecognitionService.class));
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

    /**
     * Service needs some time to stop. After that we want to start our thread
     * because of this app purpose.
     */
    private class ServiceFinishedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SimpleUtils.FINISH_SERVICE_ACTION.equals(intent.getAction())) {
                startStopService();
            }
        }
    }
}
