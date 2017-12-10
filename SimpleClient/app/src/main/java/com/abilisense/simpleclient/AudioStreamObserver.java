package com.abilisense.simpleclient;

import rx.Observer;

/**
 * Created by anatolyt on 10/12/2017.
 */

public class AudioStreamObserver implements Observer<byte[]> {

    ExampleRecorderWithObserver recorder;

    public AudioStreamObserver(ExampleRecorderWithObserver recorder) {
        this.recorder = recorder;
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(byte[] bytes) {
        recorder.setBuffer(bytes);
    }
}
