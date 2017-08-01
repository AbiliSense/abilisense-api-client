package com.abilisense.simpleclient;

import android.app.Application;

import com.abilisense.sdk.manager.AbilisenseManager;

public class SimpleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AbilisenseManager.init(getApplicationContext());
    }
}
