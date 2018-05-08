package com.karthick.android.kcextensions;

import android.util.Log;

import com.karthick.android.kcextensions.annotation.JNIVariable;


public final class EBUR128 {

    /**
     * Corresponds to enums in libebur128/ebur128/ebur128.g
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
    private int encodingBytes;

    public EBUR128(final int encodingBytes, final int channels, final int sampleRate) {
        this.channels = channels;
        this.sampleRate = sampleRate;
        this.encodingBytes = encodingBytes;
    }

    private void checkInit() {
        if (!isInitialized) {
            synchronized (this) {
                if (!isInitialized) {
                    nativeinit(encodingBytes, channels, sampleRate);
                    if (nativeHandle == 0) {
                        throw new RuntimeException("nativeHandle is null");
                    }
                    isInitialized = true;
                }
            }
        }
    }

    public void configure(final int channels, final int sampleRate) {
        checkInit();
        if (this.channels == channels && this.sampleRate == sampleRate) {
            //do nothing
            return;
        }
        nativeConfigure(channels, sampleRate);
    }

    public void setMaxHistory(long historyInMilliseconds) {
        checkInit();
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
        checkInit();
        return nativeAddFrames(pcmData, readIndex, availableSizeInBytes);
    }

    public double getIntegratedLoudness() {
        checkInit();
        return nativeGetIntegratedLoudness();
    }


    public void dispose() {
        if (nativeHandle == 0) {
            return;
        }
        Log.d(TAG, "dispose()");
        nativeDispose();
    }

    @Override
    protected void finalize() throws Throwable {
        if (nativeHandle != 0) {
            Log.e(TAG, "MediaDownloader not closed - warning");
            dispose();
        }
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
