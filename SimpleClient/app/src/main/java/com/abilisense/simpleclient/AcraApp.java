package com.abilisense.simpleclient;

import android.app.Application;
import android.content.Context;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(
        formUri = "https://abilisense.cloudant.com/acra-abilisense/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin = "disevilverestshourndlewa",
        formUriBasicAuthPassword = "4484eb5c5ea0ae8afbcbf5e010cb74325e24689e")
public class AcraApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ACRA.init(this);
    }
}
