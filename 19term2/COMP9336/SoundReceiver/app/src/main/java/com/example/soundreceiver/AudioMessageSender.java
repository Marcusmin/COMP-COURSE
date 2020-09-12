package com.example.soundreceiver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioMessageSender implements Runnable {
    private AudioTrack audioTrack;
    private final int SAMPLE_RATE = 44100;
    private short[] myBuffer;
    public AudioMessageSender(short[] buffer) {
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffer.length,
                AudioTrack.MODE_STATIC
        );
        myBuffer = buffer;
    }
    @Override
    public void run() {
        audioTrack.write(myBuffer, 0, myBuffer.length);
        audioTrack.play();
    }
}
