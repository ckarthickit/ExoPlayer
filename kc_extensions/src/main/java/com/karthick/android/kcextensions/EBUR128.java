package com.karthick.android.kcextensions;

import android.util.Log;

import com.karthick.android.kcextensions.annotation.JNIVariable;


public final class EBUR128 {

    /**
     * Corresponds to enums in libebur128/ebur128/ebur128.g
     */
    enum Error {
        EBUR128_SUCCESS,
        EBUR128_ERROR_NOMEM,
        EBUR128_ERROR_INVALID_MODE,
        EBUR128_ERROR_INVALID_CHANNEL_INDEX,
        EBUR128_ERROR_NO_CHANGE
    }

    /**
     * Corresponds to enums in JEBUR128.h
     */
    enum AudioPcmFormat {
        ENCODING_8_BIT,
        ENCODING_16_BIT,
        ENCODING_32_BIT,
        ENCODING_FLOAT
    }

    private static final String TAG = "EBUR128";
    private int channels;
    private int sampleRate;
    private AudioPcmFormat audioPcmFormat;
    private double lastLoudnessValue = Double.NEGATIVE_INFINITY;

    public EBUR128(final AudioPcmFormat audioPcmFormat, final int channels, final int sampleRate) {
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.audioPcmFormat = audioPcmFormat;
    }

    public void initialize() {
        checkInit();
    }

    public void configure(final int channels, final int sampleRate) {
        if (!isValid()) {
            Log.w(TAG, "Native Object not valid");
            return;
        }
        if (this.channels == channels && this.sampleRate == sampleRate) {
            //do nothing
            return;
        }
        nativeConfigure(channels, sampleRate);
    }

    public void setMaxHistory(long historyInMilliseconds) {
        if (!isValid()) {
            Log.w(TAG, "Native Object not valid");
            return;
        }
        nativeSetMaxHistory(historyInMilliseconds);
    }

    /**
     * Reads Complete Frames and accumulates the loudness value in it's internal data structure .
     * This API assumes that start of the buffer is start of a new Frame.
     * This API ignores reading any bytes at the end of the buffer that are part of an incomplete frame.
     * <p>
     * <h4>Note:</h3>
     * Care should be taken not to pass incomplete frames to this API.
     *
     * @param pcmData
     * @param readIndex
     * @param availableSizeInBytes
     * @return the number of bytes read , -1 if any error
     */
    public int addFrames(byte[] pcmData, int readIndex, int availableSizeInBytes) {
        if (!isValid()) {
            Log.w(TAG, "Native Object not valid");
            return -1;
        }
        return nativeAddFrames(pcmData, readIndex, availableSizeInBytes);
    }

    public double getIntegratedLoudness() {
        if (!isValid()) {
            Log.w(TAG, "Native Object not valid");
            return lastLoudnessValue;
        }
        lastLoudnessValue = nativeGetIntegratedLoudness();
        return lastLoudnessValue;
    }


    public void dispose() {
        if (nativeHandle != 0L)
            synchronized (this) {
                if (nativeHandle != 0L) {
                    Log.d(TAG, "dispose()");
                    nativeDispose();
                    if (nativeHandle != 0L) {
                        Log.e(TAG, "dispose of nativeHandle failed. Memory Leaked :/");
                    }
                }
            }
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativeHandle != 0) {
            Log.e(TAG, "MediaDownloader not closed - warning");
            dispose();
        }
    }

    private void checkInit() {
        if (nativeHandle == 0L) {
            synchronized (this) {
                if (nativeHandle == 0L) {
                    Log.d(TAG, "init()");
                    nativeinit(audioPcmFormat.ordinal(), channels, sampleRate);
                    if (nativeHandle == 0L) {
                        throw new RuntimeException("nativeHandle is null");
                    }
                }
            }
        }
    }

    private boolean isValid() {
        return (nativeHandle != 0);
    }

    //native interfaces
    @JNIVariable
    private long nativeHandle;

    private native void nativeinit(int encodingBytes, int channels, int sampleRate);

    private native int nativeConfigure(int channels, int sampleRate);

    private native int nativeSetMaxHistory(long historyInMilliseconds);


    private native int nativeAddFrames(byte[] pcmData, int readIndex, int availableSize);

    private native double nativeGetIntegratedLoudness();

    private native void nativeDispose();


    static {
        System.loadLibrary("ebur128");
        System.loadLibrary("ebur128-jni");
    }


}
