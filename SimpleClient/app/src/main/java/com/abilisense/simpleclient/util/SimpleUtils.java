package com.abilisense.simpleclient.util;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.abilisense.simpleclient.MainActivity;
import com.abilisense.simpleclient.R;


public class SimpleUtils {

    public static final int PERMISSION_REQUEST_CODE_AUDIO_ACTIVITY = 1101;
    public static final int PERMISSION_REQUEST_CODE_READ_CONTACT = 1103;
    public static final int PERMISSION_REQUEST_CODE_SEND_SMS = 1102;
    public static final String PREFERENCE_NAME = "Abilisence";
    public static final String RECIPIENT_PHONE_NUMBER_FIELD_NAME = "RecipientPhoneNumber";
    public static final String DEVICE_NAME_FIELD_NAME = "DeviceName";
    public static final int CHECK_BATTERY_LEVEL = 88;
    public static final int X_DETEXT_COUNT = 4;
    public static final long X_DETECT_TIME_DURATION_MS = 60 * 1000;
    public static final String ACTION_START_CHACKING = "ts.abilisense.action.START_CHECKING";

    public static void requestAudioPermissionActivity(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE_AUDIO_ACTIVITY);
    }

    public static void requestSendSMSPermissionActivity(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.SEND_SMS},
                PERMISSION_REQUEST_CODE_SEND_SMS);
    }

    public static void requestContactsPermissionActivity(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS},
                PERMISSION_REQUEST_CODE_READ_CONTACT);
    }

    public static boolean isAudioPermissionGranted(Activity activity) {

        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isContactsPermissionGranted(Context context) {

        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSendSMSPermissionGranted(Context context) {

        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS);
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

    public static void showBatteryNotification(Context context, int level) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.mipmap.ic_launcher))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("Battery notification")
                .setContentText("Battery level equals " + level + " %");

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }
}
