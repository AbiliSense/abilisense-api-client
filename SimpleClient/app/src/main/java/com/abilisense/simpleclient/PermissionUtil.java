package com.abilisense.simpleclient;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtil {

    private static final int PERMISSION_REQUEST_CODE_AUDIO_ACTIVITY = 11;

    public static void requestAudioPermissionActivity(Activity activity) {

        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE_AUDIO_ACTIVITY);
    }

    public static boolean isAudioPermissionGranted(Activity activity) {

        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }
}
