//
// Created by Karthick C on 07/05/18.
//

#ifndef V2_JNI_HANDLE_H
#define V2_JNI_HANDLE_H

#include<jni.h>

static jfieldID getHandleField(JNIEnv *env, jobject obj)
{
    jclass c = (*env)->GetObjectClass(env,obj);
    // J is the type signature for long:
    return (*env)->GetFieldID(env,c, "nativeHandle", "J");
}


void *getHandle(JNIEnv *env, jobject obj)
{
    jlong handle = (*env)->GetLongField(env, obj, getHandleField(env, obj));
    return (void*)handle;
}
void setHandle(JNIEnv *env, jobject obj, void *t)
{
    jlong handle = (jlong) t;
    (*env)->SetLongField(env, obj, getHandleField(env, obj), handle);
}
#endif //V2_JNI_HANDLE_H
