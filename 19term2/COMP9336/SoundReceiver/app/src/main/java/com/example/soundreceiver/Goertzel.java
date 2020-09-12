package com.example.soundreceiver;

import android.util.Log;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.dom.DOMLocator;

public class Goertzel {
    public static int MAXFREQUENCY = 22000;
    public static int SAMPLEFREQUENCY = 44100;

    public static int distinguish(short[] data) {
        int N = data.length;
        int freq1 = 13000;
        int freq2 = 16000;
        int freq3 = 5000;
        Map<Integer, Double> records = new HashMap<>();
        int k = (int) (0.5+N*freq1/SAMPLEFREQUENCY);
        double w = (2*Math.PI/N)*k;
        double cosine = Math.cos(w);
        double sine = Math.sin(w);
        double coeff = 2 * cosine;
        double Q0 = 0;
        double Q1 = 0;
        double Q2 = 0;
        // loop
        for(int j = 0; j < N; j++) {
            Q0 = coeff * Q1 - Q2 + data[j];
            Q2 = Q1;
            Q1 = Q0;
        }
        double real = Q1 - Q2*cosine;
        double imag = Q2 * sine;
        double magnitude = real*real+imag*imag;
//        Log.e("freq1", ""+magnitude);
        records.put(freq1, magnitude);
        k = (int) (0.5+N*freq2/SAMPLEFREQUENCY);
        w = (2*Math.PI/N)*k;
        cosine = Math.cos(w);
        sine = Math.sin(w);
        coeff = 2 * cosine;
        Q0 = 0;
        Q1 = 0;
        Q2 = 0;
        // loop
        for(int j = 0; j < N; j++) {
            Q0 = coeff * Q1 - Q2 + data[j];
            Q2 = Q1;
            Q1 = Q0;
        }
        real = Q1 - Q2*cosine;
        imag = Q2 * sine;
        magnitude = real*real+imag*imag;
//        Log.e("freq2", ""+magnitude);
        records.put(freq2, magnitude);
        Log.e("magnitude", " "+magnitude);
//        k = (int) (0.5+N*freq3/SAMPLEFREQUENCY);
//        w = (2*Math.PI/N)*k;
//        cosine = Math.cos(w);
//        sine = Math.sin(w);
//        coeff = 2 * cosine;
//        Q0 = 0;
//        Q1 = 0;
//        Q2 = 0;
//        // loop
//        for(int j = 0; j < N; j++) {
//            Q0 = coeff * Q1 - Q2 + data[j];
//            Q2 = Q1;
//            Q1 = Q0;
//        }
//        real = Q1 - Q2*cosine;
//        imag = Q2 * sine;
//        magnitude = real*real+imag*imag;
//        if (magnitude > records.get(freq2) && magnitude > records.get(freq1)) {
//            return -1;
//        }
        if (records.get(freq1) > records.get(freq2)) {
            return freq1;
        } else {
            return freq2;
        }
    }
    public static int[] getUTopTwpFreq(short[] data) {
        final Map<Double, Integer> records = new TreeMap<Double, Integer>(new Comparator<Double>() {
            @Override
            public int compare(Double d1, Double d2) {
                return d2.compareTo(d1);
            }
        });
        int[] res = new int[2];
        int[] high_freqs = {17000, 18000, 19000};
        int[] low_freqs = {16970, 17700, 18520};
        int N = data.length;
        for(int i = 0; i < high_freqs.length; i++) {
            int targetFreq = high_freqs[i];
            int k = (int) (0.5+N*targetFreq/SAMPLEFREQUENCY);
            double w = (2*Math.PI/N)*k;
            double cosine = Math.cos(w);
            double sine = Math.sin(w);
            double coeff = 2 * cosine;
            double Q0 = 0;
            double Q1 = 0;
            double Q2 = 0;
            // loop
            for(int j = 0; j < N; j++) {
                Q0 = coeff * Q1 - Q2 + data[j];
                Q2 = Q1;
                Q1 = Q0;
            }
            double real = Q1 - Q2*cosine;
            double imag = Q2 * sine;
            double magnitude = real*real+imag*imag;
            records.put(magnitude, targetFreq);
        }
        int count = 0;
        for(Map.Entry<Double, Integer> entry: records.entrySet()) {
            if(count > 0) {
                break;
            }
            res[0] = entry.getValue();
            count++;
        }
        records.clear();
        for(int i = 0; i < low_freqs.length; i++) {
            int targetFreq = low_freqs[i];
            int k = (int) (0.5+N*targetFreq/SAMPLEFREQUENCY);
            double w = (2*Math.PI/N)*k;
            double cosine = Math.cos(w);
            double sine = Math.sin(w);
            double coeff = 2 * cosine;
            double Q0 = 0;
            double Q1 = 0;
            double Q2 = 0;
            // loop
            for(int j = 0; j < N; j++) {
                Q0 = coeff * Q1 - Q2 + data[j];
                Q2 = Q1;
                Q1 = Q0;
            }
            double real = Q1 - Q2*cosine;
            double imag = Q2 * sine;
            double magnitude = real*real+imag*imag;
            records.put(magnitude, targetFreq);
        }
        count = 0;
        for(Map.Entry<Double, Integer> entry: records.entrySet()) {
            if(count > 0) {
                break;
            }
            res[1] = entry.getValue();
            count++;
        }
        return res;
    }

    public static int[] getTopTwpFreq(short[] data) {
        final Map<Double, Integer> records = new TreeMap<Double, Integer>(new Comparator<Double>() {
            @Override
            public int compare(Double d1, Double d2) {
               return d2.compareTo(d1);
            }
        });
        int[] res = new int[2];
        int[] high_freqs = {1209, 1336, 1477};
        int[] low_freqs = {697, 770, 852};
        int N = data.length;
        for(int i = 0; i < high_freqs.length; i++) {
            int targetFreq = high_freqs[i];
            int k = (int) (0.5+N*targetFreq/SAMPLEFREQUENCY);
            double w = (2*Math.PI/N)*k;
            double cosine = Math.cos(w);
            double sine = Math.sin(w);
            double coeff = 2 * cosine;
            double Q0 = 0;
            double Q1 = 0;
            double Q2 = 0;
            // loop
            for(int j = 0; j < N; j++) {
                Q0 = coeff * Q1 - Q2 + data[j];
                Q2 = Q1;
                Q1 = Q0;
            }
            double real = Q1 - Q2*cosine;
            double imag = Q2 * sine;
            double magnitude = real*real+imag*imag;
            records.put(magnitude, targetFreq);
        }
        int count = 0;
        for(Map.Entry<Double, Integer> entry: records.entrySet()) {
            if(count > 0) {
                break;
            }
            res[0] = entry.getValue();
            count++;
        }
        records.clear();
        for(int i = 0; i < low_freqs.length; i++) {
            int targetFreq = low_freqs[i];
            int k = (int) (0.5+N*targetFreq/SAMPLEFREQUENCY);
            double w = (2*Math.PI/N)*k;
            double cosine = Math.cos(w);
            double sine = Math.sin(w);
            double coeff = 2 * cosine;
            double Q0 = 0;
            double Q1 = 0;
            double Q2 = 0;
            // loop
            for(int j = 0; j < N; j++) {
                Q0 = coeff * Q1 - Q2 + data[j];
                Q2 = Q1;
                Q1 = Q0;
            }
            double real = Q1 - Q2*cosine;
            double imag = Q2 * sine;
            double magnitude = real*real+imag*imag;
            records.put(magnitude, targetFreq);
        }
        count = 0;
        for(Map.Entry<Double, Integer> entry: records.entrySet()) {
            if(count > 0) {
                break;
            }
            res[1] = entry.getValue();
            count++;
        }
        return res;
    }


    public static double getFrequency(short[] data) {
        int targetFreq = 20;
        Map<Integer, Double> records = new HashMap<Integer, Double>();
        int N = data.length;
        while (targetFreq < MAXFREQUENCY) {
            // init factors
            int k = (int) (0.5+N*targetFreq/SAMPLEFREQUENCY);
            double w = (2*Math.PI/N)*k;
            double cosine = Math.cos(w);
            double sine = Math.sin(w);
            double coeff = 2 * cosine;
            double Q0 = 0;
            double Q1 = 0;
            double Q2 = 0;
            // loop
            for(int i = 0; i < N/50; i++) {
                Q0 = coeff * Q1 - Q2 + data[i];
                Q2 = Q1;
                Q1 = Q0;
            }
            double real = Q1 - Q2*cosine;
            double imag = Q2 * sine;
            double magnitude = real*real+imag*imag;
            records.put(targetFreq, magnitude);
            targetFreq+=10;
        }
        int maxFreq = 0;
        double maxMag = 0;
        for (Map.Entry<Integer, Double> record: records.entrySet()) {
            if (maxMag < record.getValue()) {
                maxMag = record.getValue();
                maxFreq = record.getKey();
            }
        }
        records.clear();
        for(targetFreq = maxFreq - 20; targetFreq < maxFreq + 20; targetFreq++) {
            // init factors
            int k = (int) (0.5+N*targetFreq/SAMPLEFREQUENCY);
            double w = (2*Math.PI/N)*k;
            double cosine = Math.cos(w);
            double sine = Math.sin(w);
            double coeff = 2 * cosine;
            double Q0 = 0;
            double Q1 = 0;
            double Q2 = 0;
            // loop
            for(int i = 0; i < N/5; i++) {
                Q0 = coeff * Q1 - Q2 + data[i];
                Q2 = Q1;
                Q1 = Q0;
            }
            double real = Q1 - Q2*cosine;
            double imag = Q2 * sine;
            double magnitude = real*real+imag*imag;
            records.put(targetFreq, magnitude);
            targetFreq+=10;
        }
        maxFreq = 0;
        maxMag = 0;
        for (Map.Entry<Integer, Double> record: records.entrySet()) {
            if (maxMag < record.getValue()) {
                maxMag = record.getValue();
                maxFreq = record.getKey();
            }
        }
        return maxFreq;
    }
}
