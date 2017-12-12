package com.abilisense.simpleclient.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.abilisense.simpleclient.MainActivity;
import com.abilisense.simpleclient.AcraApp;
import com.abilisense.simpleclient.SimpleSoundRecognitionService;

public class BootTools {

    public static class BootReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent service = new Intent(context, SimpleSoundRecognitionService.class);
            startWakefulService(context, service);
        }
    }

    public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        private Activity mExceptionActivity;

        public ExceptionHandler(Activity activity) {
            this.mExceptionActivity = activity;
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            Intent intent = new Intent(mExceptionActivity, SimpleSoundRecognitionService.class);
            intent.putExtra("crash", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent =
                    PendingIntent.getService(AcraApp.getInstance().getApplicationContext(),
                            0, intent,
                            PendingIntent.FLAG_ONE_SHOT);

            AlarmManager alarmManager = (AlarmManager) AcraApp
                    .getInstance()
                    .getApplicationContext()
                    .getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent);

            mExceptionActivity.finish();
            System.exit(2);
        }
    }
}
