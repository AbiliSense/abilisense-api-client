package com.abilisense.simpleclient;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.abilisense.sdk.soundrecognizer.AudioController;
import com.abilisense.sdk.soundrecognizer.RecorderInterface;


public class ExampleRecorder extends Thread implements RecorderInterface {
    private AudioRecord audioRecord;
    private boolean isRecording;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int sampleRate = 44100;

    protected int frameByteSize = 65536;

    public ExampleRecorder() {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, channelConfiguration, audioEncoding, frameByteSize);
    }

    @Override
    public boolean isRecording() {
        return this.isAlive() && isRecording;
    }

    @Override
    public void startRecording() {
        start();
        try {
            audioRecord.startRecording();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
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

    @Override
    public int getFrameByteSize() {
        return frameByteSize;
    }

    @Override
    public byte[] getBytesFromAudioSource() {
        byte[] tmpBuffer = new byte[frameByteSize];

        int N = 4;
        int pos = 0;
        for (int i = 0; i < N; ++i) {
            pos = i * frameByteSize / N;
            audioRecord.read(tmpBuffer, pos, frameByteSize / N);

            short sample = 0;
            double sum = 0;
            for (int j = 0; j < frameByteSize / N; j += 2) {
                sample = (short) ((tmpBuffer[pos + j]) | tmpBuffer[pos + j + 1] << 8);
                sum += Math.abs(sample);
            }
            AudioController.nextValue(sum / (frameByteSize / 2));
        }
        AudioController.nextDataChunk(tmpBuffer);
        return tmpBuffer;
    }
}
