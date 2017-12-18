package com.abilisense.simpleclient;

/**
 * Created by anatolyt on 18/12/2017.
 */

public class AudioEvent {

    private String tag;

    public AudioEvent(String tag, long timestamp) {
        this.tag = tag;
        this.timestamp = timestamp;
    }

    private long timestamp;

    public String getTag() {
        return tag;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
