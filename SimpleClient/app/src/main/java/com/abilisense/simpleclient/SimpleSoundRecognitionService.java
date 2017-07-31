package com.abilisense.simpleclient;

import android.os.Message;
import android.util.Log;

import com.abilisense.sdk.service.BaseSoundRecognitionService;
import com.abilisense.sdk.utils.AbiliConstants;
import com.abilisense.sdk.utils.AbilisenseUtils;


public class SimpleSoundRecognitionService extends BaseSoundRecognitionService {
    @Override
    protected void startAlertActivity(Message msg, String tag, String fileId) {
        Log.e(AbiliConstants.LOG_TAG, "====================================");
        Log.e(AbiliConstants.LOG_TAG, "You have new message from Abilisense");
        Log.e(AbiliConstants.LOG_TAG, "Message = " + msg.toString());
        Log.e(AbiliConstants.LOG_TAG, "Tag = " + tag);
        Log.e(AbiliConstants.LOG_TAG, "Field = " + fileId);
        Log.e(AbiliConstants.LOG_TAG, "====================================");
    }

    @Override
    protected Class getNotificationActivity() {
        return MainActivity.class;
    }

    @Override
    protected int getMicrophoneVolumeSensitivity() {
        return AbilisenseUtils.DEFAULT_MICROPHONE_SENSITIVITY;
    }
}
