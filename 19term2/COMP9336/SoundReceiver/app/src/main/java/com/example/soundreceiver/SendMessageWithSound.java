package com.example.soundreceiver;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class SendMessageWithSound extends AppCompatActivity {
    private int SAMPLE_RATE = 44100;
    private AudioTrack audioTrack;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task4_sender);
        //
        int bufferSize = AudioTrack.getMinBufferSize(44100,
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
        Button sendBtn = findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String message = getMessageFromText(view);
                        sendTextByTone(message);
                    }
                }
        );
    }

    private String getMessageFromText(View view) {
        String text;
        EditText editText = (EditText) findViewById(R.id.editText);
        text = editText.getText().toString();
        return text;
    }

    private void sendTextByTone(String text) {
        int singleBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        Log.e("sender buffer", " "+singleBufferSize);
        List<short[]> outputBuffers = new ArrayList<>();
        for(int i = 0; i < text.length(); i++) {
//            char preamble = 0b11111111;
//            for(int j = 0; j <= 7; j++) {
//                short[] buffer = new short[singleBufferSize];
//                if (GETBIT(preamble, j) == 1) {
//                    fillBuffer(buffer, 13000);
//                } else {
//                    fillBuffer(buffer, 17000);
//                }
//                outputBuffers.add(buffer);
//            }
            for(int j = 0; j <= 7; j++) {
                short[] buffer = new short[singleBufferSize];
                if (GETBIT(text.charAt(i), j) == 1) {
                    fillBuffer(buffer, 13000);
                } else {
                    fillBuffer(buffer, 17000);
                }
                outputBuffers.add(buffer);
            }
        }
        while (true) {
            try{
                audioTrack.play();
                break;
            }catch (IllegalStateException e) {
                Log.e("AudioTrack", "Illegal State Exception");
            }
        }
        for(short[] buffer: outputBuffers) {
            audioTrack.write(buffer,0,buffer.length);
        }
        audioTrack.stop();
//        audioTrack.release();
    }
    private void fillBuffer(short[] buffer, int freq) {
        double ph = 0.0;
        for(int i = 0; i < buffer.length; i++) {
            buffer[i] = (short) (Short.MAX_VALUE*Math.sin(ph));
            ph+=8*Math.atan(1.0)*freq/44100;
        }
    }
    private int GETBIT(char b, int pos) {
        // 0b10000000
        return b>>(7-pos)&1;
    }
}
