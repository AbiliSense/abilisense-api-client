package com.abilisense.simpleclient.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedHelper {

    private static final String PREFERENCES_NAME = "SimpleClientAbilisense";

    private static final String PREFERENCES_RECIPIENT_PHONE_NUMBER = "RecipientPhoneNumber";
    private static final String PREFERENCES_DEVICE_NAME = "DeviceName";
    private static final String PREFERENCES_DETECTION_COUNT = "detect_threshold";

    private final SharedPreferences pref;

    public SharedHelper(Context context) {
        pref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void setPhones(String phones) {
        pref.edit().putString(PREFERENCES_RECIPIENT_PHONE_NUMBER, phones).apply();
    }

    public String getPhones() {
        return pref.getString(PREFERENCES_RECIPIENT_PHONE_NUMBER, "");
    }

    public void setDetectorThreshold(int detectorThreshold) {
        pref.edit().putInt(PREFERENCES_DETECTION_COUNT, detectorThreshold).apply();
    }

    public int getDetectorThreshold() {
        return pref.getInt(PREFERENCES_DETECTION_COUNT, 1);
    }

    public void setDeviceName(String name) {
        pref.edit().putString(PREFERENCES_DEVICE_NAME, name).apply();
    }

    public String getDeviceName() {
        return pref.getString(PREFERENCES_DEVICE_NAME, "");
    }
}
