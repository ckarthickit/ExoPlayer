/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.demo;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.karthick.android.kcextensions.external.DefaultHttpDataSourceFactory;

/**
 * Placeholder application to facilitate overriding Application methods for debugging and testing.
 */
public class DemoApplication extends Application {

  protected String userAgent;

  @Override
  public void onCreate() {
    super.onCreate();
    userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
    initStetho();
  }

  private void initStetho(){
      Log.d(getPackageName(),"Enter Stetho");
      Stetho.initializeWithDefaults(this);
      // Create an InitializerBuilder
      Stetho.InitializerBuilder initializerBuilder = Stetho.newInitializerBuilder(this);
      // Enable Chrome DevTools
      initializerBuilder.enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this));
      // Enable command line interface
      initializerBuilder.enableDumpapp(Stetho.defaultDumperPluginsProvider(this));
      // Use the InitializerBuilder to generate an Initializer
      Stetho.Initializer initializer = initializerBuilder.build();
      // Initialize Stetho with the Initializer
      Stetho.initialize(initializer);
    }

  /** Returns a {@link DataSource.Factory}. */
  public DataSource.Factory buildDataSourceFactory(TransferListener<? super DataSource> listener) {
    return new DefaultDataSourceFactory(this, listener, buildHttpDataSourceFactory(listener));
  }

  /** Returns a {@link HttpDataSource.Factory}. */
  public HttpDataSource.Factory buildHttpDataSourceFactory(
      TransferListener<? super DataSource> listener) {
    return new DefaultHttpDataSourceFactory(userAgent, listener);
  }

  public boolean useExtensionRenderers() {
    return BuildConfig.FLAVOR.equals("withExtensions");
  }

}
