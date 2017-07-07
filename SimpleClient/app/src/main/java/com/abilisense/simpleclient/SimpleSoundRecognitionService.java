package com.abilisense.simpleclient;

import android.os.Message;
import android.util.Log;

import com.abilisense.service.BaseSoundRecognitionService;
import com.abilisense.utils.AbiliConstants;
import com.abilisense.utils.AbilisenseUtils;

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
    protected Class getActivity() {
        return StartActivity.class;
    }

    @Override
    protected int getMicrophoneVolumeSensitivity() {
        return AbilisenseUtils.DEFAULT_MICROPHONE_SENSITIVITY;
    }
}
