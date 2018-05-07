package com.karthick.android.kcextensions.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Used to mark Native Object Handle corresponding to that Java Object
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface JNIVariable {
}
