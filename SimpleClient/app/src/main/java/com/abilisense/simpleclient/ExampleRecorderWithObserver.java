package com.abilisense.simpleclient;

import com.abilisense.sdk.soundrecognizer.RecorderInterface;

import rx.Observer;

/**
 * Created by anatolyt on 06/12/2017.
 */

public class ExampleRecorderWithObserver implements RecorderInterface {

    private int frameByteSize = 65536;

    private byte[] buffer = null;

    @Override
    public boolean isRecording() {
        return true;
    }

    @Override
    public void startRecording() {
    }

    @Override
    public void stopRecording() {
    }

    @Override
    public int getFrameByteSize() {
        return frameByteSize;
    }

    public void setFrameByteSize(int frameByteSize) {
        this.frameByteSize = frameByteSize;
    }

    @Override
    public byte[] getBytesFromAudioSource() {
        byte[] tmpBuffer = buffer;
        buffer = null;
        return tmpBuffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
