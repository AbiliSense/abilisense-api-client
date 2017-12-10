package com.abilisense.simpleclient;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import rx.Observable;

/**
 * Created by anatolyt on 10/12/2017.
 */

public class RecordingThreadWithObserver extends Thread {
    private AudioRecord audioRecord;
    private boolean isRecording = true;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 44100;
    private int frameByteSize = 65536;
    byte[] buffer = new byte[frameByteSize];
    private AudioStreamObserver observer;

    public RecordingThreadWithObserver(AudioStreamObserver observer) {
        super();
        this.observer = observer;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, channelConfiguration, audioEncoding, frameByteSize);
    }

    @Override
    public synchronized void start() {
        try {
            audioRecord.startRecording();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.start();
    }

    @Override
    public void run() {
        while (isRecording) {
            audioRecord.read(buffer, 0, frameByteSize);
            observer.onNext(buffer);
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRecording() {
        try {
            //  record = false;
            audioRecord.stop();
            audioRecord.release();
            isRecording = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getFrameByteSize() {
        return frameByteSize;
    }
}
