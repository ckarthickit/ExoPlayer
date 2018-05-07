package com.karthick.android.kcextensions;

import android.util.Log;

import com.karthick.android.kcextensions.annotation.JNIVariable;

import java.io.File;

public final class EBUR128 {

    /**
     *  Corresponds to enums in libebur128/ebur128/ebur128.g
     */
    enum error {
        EBUR128_SUCCESS,
        EBUR128_ERROR_NOMEM,
        EBUR128_ERROR_INVALID_MODE,
        EBUR128_ERROR_INVALID_CHANNEL_INDEX,
        EBUR128_ERROR_NO_CHANGE
    }

    private static final String TAG = "EBUR128";
    private boolean isInitialized = false;
    private int channels;
    private int sampleRate;
    private int encoding;
    public EBUR128(final int encoding, final int channels, final int sampleRate) {
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.encoding = encoding;
    }

    private void checkInit(){
        if (!isInitialized) {
            synchronized (this) {
                if (!isInitialized) {
                    nativeinit(encoding, channels, sampleRate);
                    if(nativeHandle == 0) {
                        throw new RuntimeException("nativeHandle is null");
                    }
                    isInitialized = true;
                }
            }
        }
    }

    public void configure(final int channels, final int sampleRate) {
        checkInit();
        if(this.channels == channels && this.sampleRate == sampleRate) {
            //do nothing
            return;
        }
        nativeConfigure(channels,sampleRate);
    }

    public void setMaxHistory(long historyInMilliseconds) {
        checkInit();
        nativeSetMaxHistory(historyInMilliseconds);
    }


    public void dispose()  {
        if (nativeHandle == 0) {
            return;
        }
        Log.d(TAG, "dispose()");
        nativeDispose();
    }

    @Override
    protected void finalize() throws Throwable {
        if(nativeHandle != 0) {
            Log.e(TAG, "MediaDownloader not closed - warning");
            dispose();
        }
    }

    //native interfaces
    @JNIVariable
    private long nativeHandle;

    private native void nativeinit(int encoding, int channels, int sampleRate);
    private native int nativeConfigure(int channels, int sampleRate);
    private native int nativeSetMaxHistory(long historyInMilliseconds);
    private native void nativeDispose();


    static {
        System.loadLibrary("ebur128");
        System.loadLibrary("ebur128-jni");
    }


}
