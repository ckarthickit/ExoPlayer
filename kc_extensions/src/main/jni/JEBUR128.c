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

static int numberOfBytePerSample(audio_pcm_format format);

void
Java_com_karthick_android_kcextensions_EBUR128_nativeinit(JNIEnv *env, jobject thiz,
                                                          jint audioPCMFormat,
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
        ebur128Obj->audio_pcm_format = audioPCMFormat;
        ebur128Obj->cache_buffer = NULL;
        ebur128Obj->cache_size = 0;
        setHandle(env, thiz, ebur128Obj);
#if DEBUG
        __android_log_print(ANDROID_LOG_DEBUG, TAG,
                            "creating object= %p, sts channel=%u, samplerate=%lu, mode=%d",
                            ebur128Obj, ebur128Obj->sts->channels, ebur128Obj->sts->samplerate,
                            ebur128Obj->sts->mode);
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

jint Java_com_karthick_android_kcextensions_EBUR128_nativeAddFrames(JNIEnv *env, jobject thiz,
                                                                    jbyteArray pcmData,
                                                                    jint readIndex,
                                                                    jint availableSizeInBytes) {
    ebur128_obj *ebur128Obj = getHandle(env, thiz);

    //Check Cache for Size , If size not enough re-create it
    if (ebur128Obj->cache_size < availableSizeInBytes) {
        if (ebur128Obj->cache_buffer != NULL) {
            free(ebur128Obj->cache_buffer);
            ebur128Obj->cache_buffer = NULL;
            ebur128Obj->cache_size = -1L;
        }
        ebur128Obj->cache_buffer = (uint8_t *) malloc(sizeof(uint8_t) * availableSizeInBytes);
        if (ebur128Obj->cache_buffer == NULL) {
            return EBUR128_ERROR_NOMEM;
        }
        ebur128Obj->cache_size = availableSizeInBytes;
    }
    //copy bytes from java byte array to native byte array
    uint8_t *buffer = ebur128Obj->cache_buffer;
    (*env)->GetByteArrayRegion(env, pcmData, readIndex, availableSizeInBytes, (jbyte *) buffer);
    if ((*env)->ExceptionCheck(env)) {
        (*env)->ExceptionDescribe(env);
    }
    int single_frames_size_in_bytes = (ebur128Obj->sts->channels *
                                       numberOfBytePerSample(ebur128Obj->audio_pcm_format));
    int incomplete_frame_bytes = availableSizeInBytes % single_frames_size_in_bytes;
    if (incomplete_frame_bytes != 0) {
        __android_log_print(ANDROID_LOG_WARN, TAG,
                            "passed buffer size not in multiples of frames");
    }
    if (ebur128Obj->audio_pcm_format == ENCODING_16_BIT) {
        if (ebur128_add_frames_short(ebur128Obj->sts,(short*) buffer,
                                     availableSizeInBytes / single_frames_size_in_bytes) ==
            EBUR128_SUCCESS) {
            return availableSizeInBytes - incomplete_frame_bytes;
        }

    } else if (ebur128Obj->audio_pcm_format == ENCODING_32_BIT) {
        if (ebur128_add_frames_int(ebur128Obj->sts,(int*) buffer,
                                   availableSizeInBytes / single_frames_size_in_bytes) ==
            EBUR128_SUCCESS) {
            return availableSizeInBytes - incomplete_frame_bytes;
        }
    } else {
        __android_log_print(ANDROID_LOG_WARN, TAG,
                            "Unhandled encoding format = %d", ebur128Obj->audio_pcm_format);
    }
    return -1;
}


jdouble Java_com_karthick_android_kcextensions_EBUR128_nativeGetIntegratedLoudness(JNIEnv *env,
                                                                                   jobject thiz) {
    ebur128_obj *ebur128Obj = getHandle(env, thiz);
    jdouble loudness = -INT64_MIN;
    ebur128_loudness_global(ebur128Obj->sts, &loudness);
    return loudness;
}

void Java_com_karthick_android_kcextensions_EBUR128_nativeDispose(JNIEnv *env, jobject thiz) {
#if DEBUG
    __android_log_print(ANDROID_LOG_DEBUG, TAG,
                        "Disposing object %p", getHandle(env, thiz));
#endif
    ebur128_obj *ebur128Obj = getHandle(env, thiz);
    if (ebur128Obj != NULL) {

        //destroy ebur128 state
        ebur128_destroy(&(ebur128Obj->sts));

        //destroy ebur128 cache
        if (ebur128Obj->cache_buffer != NULL) {
            free(ebur128Obj->cache_buffer);
            ebur128Obj->cache_buffer = NULL;
            ebur128Obj->cache_size = -1L;
        }

        //destroy ebur128
        free(ebur128Obj);
        setHandle(env, thiz, NULL);
    }
}


static int numberOfBytePerSample(audio_pcm_format format) {
    if (format == ENCODING_8_BIT) {
        return sizeof(uint8_t);
    } else if (format == ENCODING_16_BIT) {
        return sizeof(uint16_t);
    } else if (format == ENCODING_32_BIT) {
        return sizeof(uint32_t);
    } else if (format == ENCODING_FLOAT) {
        return sizeof(uint32_t);
    }
    return sizeof(uint16_t);
}


