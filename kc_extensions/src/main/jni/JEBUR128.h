//
// Created by Karthick C on 07/05/18.
//

#ifndef V2_JEBUR128_H
#define V2_JEBUR128_H
#include <jni.h>
#include "libebur128/ebur128/ebur128.h"

typedef struct{
    ebur128_state *sts;
    int encoding;
}ebur128_obj;
JNIEXPORT void Java_com_karthick_android_kcextensions_EBUR128_nativeinit(JNIEnv* env, jobject thiz,jint encoding, jint channels, jint sampleRate);
JNIEXPORT jint Java_com_karthick_android_kcextensions_EBUR128_nativeConfigure(JNIEnv* env, jobject thiz, jint channels, jint sampleRate);
JNIEXPORT jint Java_com_karthick_android_kcextensions_EBUR128_nativeSetMaxHistory(JNIEnv* env, jobject thiz, jlong historyInMilliseconds);
JNIEXPORT void Java_com_karthick_android_kcextensions_EBUR128_nativeDispose(JNIEnv* env, jobject thiz);
#endif //V2_JEBUR128_H
