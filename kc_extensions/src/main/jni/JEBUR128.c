//
// Created by Karthick C on 07/05/18.
//
#include <android/log.h>
#include <string.h>
#include <stdlib.h>

#include "JEBUR128.h"
#include "libebur128/ebur128/ebur128.h"


void Java_com_karthick_android_kcextensions_EBUR128_method(JNIEnv* env, jobject thiz ) {
    ebur128_state* sts = NULL;
    sts = ebur128_init((unsigned) 2,
                       (unsigned) 16000,
                       EBUR128_MODE_I | EBUR128_MODE_HISTOGRAM);
    __android_log_write(ANDROID_LOG_DEBUG,"KC_DEBUG","Statement from JNI");
    __android_log_print(ANDROID_LOG_DEBUG,"KC_DEBUG","sts channel=%lu, samplerate=%lu, mode=%d",
                        sts->channels, sts->samplerate, sts->mode);
}