package com.karthick.android.kcextensions;

public final class EBUR128 {

    public native void method();


    static {
        System.loadLibrary("ebur128");
        System.loadLibrary("ebur128-jni");
    }
}
