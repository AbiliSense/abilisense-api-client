package com.abilisense.simpleclient;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtil {

    public static final int PERMISSION_REQUEST_CODE_AUDIO_ACTIVITY = 1101;

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

    public static boolean checkShouldShowRequestPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                return true;
            }
        }
        return false;
    }
}
