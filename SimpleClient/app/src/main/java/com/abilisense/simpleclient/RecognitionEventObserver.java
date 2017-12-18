package com.abilisense.simpleclient;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import rx.Observer;

/**
 * Created by anatolyt on 10/12/2017.
 */

public class RecognitionEventObserver implements Observer<AudioEvent> {

    private Context context;

    public RecognitionEventObserver(Context context) {
        this.context = context;
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onNext(AudioEvent audioEvent) {
        Toast.makeText(context, audioEvent.getTag(), Toast.LENGTH_SHORT).show();
    }

}
