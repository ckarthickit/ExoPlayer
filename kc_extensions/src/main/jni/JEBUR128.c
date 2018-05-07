//
// Created by Karthick C on 07/05/18.
//

#include <android/log.h>
#include <string.h>
#include <stdlib.h>

#include "JEBUR128.h"
#include "jni_handle.h"
#include "libebur128/ebur128/ebur128.h"

#define TAG "JEBUR128"
#define DEBUG 1

void
Java_com_karthick_android_kcextensions_EBUR128_nativeinit(JNIEnv *env, jobject thiz,
                                                          jint encoding,
                                                          jint channels,
                                                          jint sampleRate) {
    ebur128_obj *ebur128Obj = getHandle(env, thiz);
    if (ebur128Obj == NULL) {
        ebur128Obj = (ebur128_obj *) malloc(sizeof(ebur128_obj));

        ebur128_state *sts = ebur128_init((unsigned) channels,
                                          (unsigned) sampleRate,
                                          EBUR128_MODE_I | EBUR128_MODE_HISTOGRAM);

        if (channels == 5) {
            ebur128_set_channel(sts, 0, EBUR128_LEFT);
            ebur128_set_channel(sts, 1, EBUR128_RIGHT);
            ebur128_set_channel(sts, 2, EBUR128_CENTER);
            ebur128_set_channel(sts, 3, EBUR128_LEFT_SURROUND);
            ebur128_set_channel(sts, 4, EBUR128_RIGHT_SURROUND);
        }
        ebur128Obj->sts = sts;
        ebur128Obj->encoding = encoding;
        setHandle(env, thiz, ebur128Obj);
#if DEBUG
        __android_log_print(ANDROID_LOG_DEBUG, TAG,
                            "sts channel=%u, samplerate=%lu, mode=%d",
                            sts->channels, sts->samplerate, sts->mode);
#endif
    }
}

jint Java_com_karthick_android_kcextensions_EBUR128_nativeConfigure(JNIEnv *env, jobject thiz,
                                                                    jint channels,
                                                                    jint sampleRate) {
    ebur128_obj *ebur128Obj = getHandle(env, thiz);
    int retVal = ebur128_change_parameters(ebur128Obj->sts, channels, sampleRate);
    if (retVal != EBUR128_SUCCESS) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to configure %d", retVal);
    }
    return retVal;
}

jint Java_com_karthick_android_kcextensions_EBUR128_nativeSetMaxHistory(JNIEnv *env, jobject thiz,
                                                                        jlong historyInMilliseconds) {
    ebur128_obj *ebur128Obj = getHandle(env, thiz);
    int retVal = ebur128_set_max_history(ebur128Obj->sts, historyInMilliseconds);
    if (retVal != EBUR128_SUCCESS) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to set max history %d", retVal);
    }
    return retVal;
}

void Java_com_karthick_android_kcextensions_EBUR128_nativeDispose(JNIEnv *env, jobject thiz) {
#if DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, TAG,
                        "Disposing object %p", getHandle(env, thiz));
#endif
    ebur128_obj *ebur128Obj = getHandle(env, thiz);
    if (ebur128Obj != NULL) {
        ebur128_destroy(ebur128Obj->sts);
        free(ebur128Obj);
        setHandle(env, thiz, NULL);
    }
}

