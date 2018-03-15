package com.karhick.android.kcextensions;

import android.content.Context;

import com.facebook.stetho.Stetho;
import com.google.android.exoplayer2.util.Log;

public class DemoUtils {

    public static void initStetho(Context application) {
        Log.d(application.getPackageName(), "Enter Stetho");
        Stetho.initializeWithDefaults(application);
        // Create an InitializerBuilder
        Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(application);
        // Enable Chrome DevTools
        initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(application));
        // Enable command line interface
        initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(application));
        // Use the InitializerBuilder to generate an Initializer
        Stetho.Initializer initializer = initializerBuilder.build();
        // Initialize Stetho with the Initializer
        Stetho.initialize(initializer);
    }


}
