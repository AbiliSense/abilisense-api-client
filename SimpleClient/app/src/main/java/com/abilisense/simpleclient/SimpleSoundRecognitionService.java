package com.abilisense.simpleclient;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.abilisense.sdk.service.BaseSoundRecognitionService;
import com.abilisense.sdk.utils.AbiliConstants;
import com.abilisense.sdk.utils.AbilisenseUtils;


public class SimpleSoundRecognitionService extends BaseSoundRecognitionService {

    @Override
    protected void startAlertActivity(Message msg, String tag, String fileId) {
        final String str = "=================================" +
                "\nYou have new message from Abilisense" +
                "\nMessage = " + msg.toString() +
                "\nTag = " + tag +
                "\nField = " + fileId +
                "\n=================================";
        Log.e(AbiliConstants.LOG_TAG, str);
        showToast(str);
    }

    private void showToast(final String str) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected Class getNotificationActivity() {
        return MainActivity.class;
    }

    @Override
    protected int getMicrophoneVolumeSensitivity() {
        return AbilisenseUtils.DEFAULT_MICROPHONE_SENSITIVITY;
    }

    @Override
    protected void onConnected() {
        Log.e(AbiliConstants.LOG_TAG, "Simple service: service is connected");
    }

    @Override
    protected void onDisconnected() {
        sendBroadcast(new Intent(MainActivity.FINISH_SERVICE_ACTION));
    }
}
