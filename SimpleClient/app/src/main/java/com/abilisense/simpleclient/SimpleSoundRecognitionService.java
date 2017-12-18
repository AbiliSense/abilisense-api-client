package com.abilisense.simpleclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.abilisense.sdk.service.BaseSoundRecognitionService;
import com.abilisense.sdk.utils.AbiliConstants;
import com.abilisense.sdk.utils.AbilisenseUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SimpleSoundRecognitionService extends BaseSoundRecognitionService {

    private SharedPreferences pref;
    private static long lastActivationTime = 0;

    private static RecognitionEventObserver recognitionEventObserver;

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        pref = getSharedPreferences(ClientConstants.PREFERENCE_NAME, Context.MODE_PRIVATE);
        super.onCreate();
    }

    public static void setRecognitionEventObserver(RecognitionEventObserver observer) {
        recognitionEventObserver = observer;
    }

    /**
     * Handling the recognition event, you can notify user here
     * by showing activity on screen
     * or send notification to third party
     * @param msg full event message information
     * @param tag detected event tag, e.g. smoke_detector or baby_cry
     * @param fileId reserved for future use
     */
    @Override
    protected void startAlertActivity(Message msg, String tag, String fileId) {

        recognitionEventObserver.onNext(new AudioEvent(tag, msg.getWhen()));
        final String str = "=================================" +
                "\nYou have new message from Abilisense" +
                "\nMessage = " + msg.toString() +
                "\nTag = " + tag +
                "\nField = " + fileId +
                "\n=================================";
        Log.e(AbiliConstants.LOG_TAG, str);
//        showToast(str);
//        Intent i = new Intent("android.intent.action.soskeydown");
//        sendBroadcast(i);
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String deviceName = pref.getString(ClientConstants.DEVICE_NAME_FIELD_NAME, "");
        long now = System.currentTimeMillis();
        if(now - TimeUnit.SECONDS.toMillis(15) > lastActivationTime) {
            lastActivationTime = now;
            if (PermissionUtil.isSendSMSPermissionGranted(getApplicationContext())) {
                sendSMS(String.format("Event detected by AbiliSense device: %s, at: %s, event: %s", deviceName, formatter.format(today), tag));
            }
        }
    }

    private boolean sendSMS(String message) {
        String phoneNumber = pref.getString(ClientConstants.RECIPIENT_PHONE_NUMBER_FIELD_NAME, "");
        if(!phoneNumber.isEmpty()) {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null,
                    parts, null, null);
        }
        return true;
    }

    private void showToast(final String str) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Defines the context for notification that created with detection start
     * e.g. MainActivity.class
     * @return instance of Context.class
     */
    @Override
    protected Class getNotificationActivity() {
        return MainActivity.class;
    }

    /**
     * Minimal volume level for detection in -db
     * e.g. if you wnat -45db return 45 here
     * @return
     */
    @Override
    protected int getMicrophoneVolumeSensitivity() {
        return AbilisenseUtils.DEFAULT_MICROPHONE_SENSITIVITY;
    }

    /**
     * Defines if recognition results aggregation is enabled
     * @return
     */
    @Override
    protected boolean isAggregationEnabled() {
        return false;
    }

    /**
     * Defines minimal recognition coefficient 0 - 100
     * 0 wider range, 100 more exact match
     * @return int value between 0 and 100
     */
    @Override
    protected int getMinCorrelation() {
        return 60;
    }

    /**
     * Lifecycle handling when service is started/connected
     */
    @Override
    protected void onConnected() {
        Log.e(AbiliConstants.LOG_TAG, "Simple service: service is connected");
    }

    /**
     * Lifecycle handling when service is destroyed/disconnected
     */
    @Override
    protected void onDisconnected() {
        sendBroadcast(new Intent(MainActivity.FINISH_SERVICE_ACTION));
    }
}
