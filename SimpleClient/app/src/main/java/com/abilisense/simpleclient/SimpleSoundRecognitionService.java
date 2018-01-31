package com.abilisense.simpleclient;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.abilisense.sdk.service.BaseSoundRecognitionService;
import com.abilisense.sdk.utils.AbiliConstants;
import com.abilisense.sdk.utils.AbilisenseUtils;
import com.abilisense.simpleclient.util.SharedHelper;
import com.abilisense.simpleclient.util.SimpleUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SimpleSoundRecognitionService extends BaseSoundRecognitionService {

    private static long lastActivationTime = 0;
    private Map<String, SoundDataHolder> mDetection = new HashMap<>();
    private SharedHelper sharedHelper;

    /**
     * Handling the recognition event, you can notify user here
     * by showing activity on screen
     * or send notification to third party
     *
     * @param msg    full event message information
     * @param tag    detected event tag, e.g. smoke_detector or baby_cry
     * @param fileId reserved for future use
     */
    @Override
    protected void startAlertActivity(Message msg, String tag, String fileId) {
        final String str = "=================================" +
                "\nYou have new message from Abilisense" +
                "\nMessage = " + msg.toString() +
                "\nTag = " + tag +
                "\nField = " + fileId +
                "\n=================================";
        Log.e(AbiliConstants.LOG_TAG, str);

        sharedHelper = new SharedHelper(getApplicationContext());

        String deviceName = sharedHelper.getDeviceName();
        String phoneNumberAll = sharedHelper.getPhones();
        long now = System.currentTimeMillis();
        if (now - TimeUnit.SECONDS.toMillis(15) > lastActivationTime && detectEvent(tag)) {
            lastActivationTime = now;
            if (SimpleUtils.isSendSMSPermissionGranted(getApplicationContext())) {
                Date today = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String smsBody = String.format("Event detected by AbiliSense device: %s, at: %s, event: %s",
                        deviceName, formatter.format(today), tag);
                String phones[] = phoneNumberAll.split("; ");
                for (String phone : phones) {
                    sendSMS(phone, smsBody);
                }
                // FOR TEST: showToast("SMS sent");
            }
        }
    }

    private boolean detectEvent(String tag) {
        long currTime = new Date().getTime();
        SoundDataHolder holder = mDetection.get(tag);
        if (holder != null) {
            long timeFuture = holder.time + SimpleUtils.X_DETECT_TIME_DURATION_MS;
            if (timeFuture <= currTime) {
                mDetection.remove(tag);
                return false;
            }

            int countDetect = sharedHelper.getDetectorThreshold();
            if (holder.count >= countDetect) {
                mDetection.remove(tag);
                return true;
            }
            holder.count++;
        } else {
            mDetection.put(tag, new SoundDataHolder(tag, currTime, 1));
        }
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return Service.START_STICKY;
    }

    private void sendSMS(String phoneNumber, String message) {
        if (!phoneNumber.isEmpty()) {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null,
                    parts, null, null);
        }
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
     *
     * @return instance of Context.class
     */
    @Override
    protected Class getNotificationActivity() {
        return MainActivity.class;
    }

    /**
     * Minimal volume level for detection in -db
     * e.g. if you wnat -45db return 45 here
     *
     * @return volume level
     */
    @Override
    protected int getMicrophoneVolumeSensitivity() {
        return AbilisenseUtils.DEFAULT_MICROPHONE_SENSITIVITY;
    }

    /**
     * Defines if recognition results aggregation is enabled
     *
     * @return true, if enabled
     */
    @Override
    protected boolean isAggregationEnabled() {
        return false;
    }

    /**
     * Defines minimal recognition coefficient 0 - 100
     * 0 wider range, 100 more exact match
     *
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
        sendBroadcast(new Intent(SimpleUtils.FINISH_SERVICE_ACTION));
    }

    private static class SoundDataHolder {
        long time;
        int count;
        String tag;

        SoundDataHolder(String tag, long time, int count) {
            this.tag = tag;
            this.time = time;
            this.count = count;
        }

        @Override
        public String toString() {
            return "Catch sound = {" +
                    "time=" + time +
                    ", count=" + count +
                    ", tag='" + tag + '\'' +
                    '}';
        }
    }
}
