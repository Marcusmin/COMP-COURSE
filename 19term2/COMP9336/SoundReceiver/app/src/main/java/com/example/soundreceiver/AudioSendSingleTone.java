package com.example.soundreceiver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AudioSendSingleTone implements Runnable {
    private int freq;
    private AudioTrack audioTrack;
    private int bufferSize;
    private boolean isSinging;
    public AudioSendSingleTone(int freq) {
        this.freq = freq;
        isSinging = true;
    }

    @Override
    public void run() {
        bufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
        );
        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
        );
        while (true) {
            try {
                audioTrack.play();
                break;
            } catch (IllegalStateException e) {
                Log.e("UNEXPECTED", "play() trigger illegal state exception");
            }
        }
        double ph = 0.0;
        short[] buffer = new short[bufferSize];
        while (isSinging) {
            for(int i = 0; i <  bufferSize; i++) {
                buffer[i] = (short)(Short.MAX_VALUE*Math.sin(ph));
                ph += 8*Math.atan(1.0)*freq/44100;
            }
            audioTrack.write(buffer, 0, buffer.length);
        }
        audioTrack.stop();
        audioTrack.release();
    }
    public void stopThread() {
        isSinging = false;
    }
}
