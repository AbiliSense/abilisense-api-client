package com.abilisense.simpleclient;

import android.content.Intent;
import android.os.Bundle;

import com.abilisense.sdk.activity.AbilisenseBaseLoginActivity;
import com.abilisense.sdk.activity.AbilisenseLoginActivity;

public class LoginActivity extends AbilisenseBaseLoginActivity {

    @Override
    final protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runDispatch();
    }

    @Override
    protected Class<?> getTargetClass() {
        return MainActivity.class;
    }

    @Override
    protected Intent getParseLoginIntent() {
        return new Intent(this, AbilisenseLoginActivity.class);
    }

    @Override
    protected void startSyncService() {
        startService(new Intent(this, SimpleSoundRecognitionService.class));
    }
}
