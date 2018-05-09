//
// Created by Karthick C on 07/05/18.
//

#ifndef V2_JEBUR128_H
#define V2_JEBUR128_H

#include <jni.h>
#include "libebur128/ebur128/ebur128.h"

typedef enum {
    ENCODING_8_BIT,
    ENCODING_16_BIT,
    ENCODING_32_BIT,
    ENCODING_FLOAT
} audio_pcm_format;

typedef struct {
    ebur128_state *sts;
    int audio_pcm_format;
    uint8_t *cache_buffer;
    size_t cache_size;
} ebur128_obj;

JNIEXPORT void Java_com_karthick_android_kcextensions_EBUR128_nativeinit(JNIEnv *env, jobject thiz,
                                                                         jint audioPCMFormat,
                                                                         jint channels,
                                                                         jint sampleRate);

JNIEXPORT jint
Java_com_karthick_android_kcextensions_EBUR128_nativeConfigure(JNIEnv *env, jobject thiz,
                                                               jint channels, jint sampleRate);

JNIEXPORT jint
Java_com_karthick_android_kcextensions_EBUR128_nativeSetMaxHistory(JNIEnv *env, jobject thiz,
                                                                   jlong historyInMilliseconds);

JNIEXPORT jint
Java_com_karthick_android_kcextensions_EBUR128_nativeAddFrames(JNIEnv *env, jobject thiz,
                                                               jbyteArray pcmData, jint readIndex,
                                                               jint availableSizeInBytes);

JNIEXPORT jdouble
Java_com_karthick_android_kcextensions_EBUR128_nativeGetIntegratedLoudness(JNIEnv *env,
                                                                           jobject thiz);

JNIEXPORT void
Java_com_karthick_android_kcextensions_EBUR128_nativeDispose(JNIEnv *env, jobject thiz);

#endif //V2_JEBUR128_H
