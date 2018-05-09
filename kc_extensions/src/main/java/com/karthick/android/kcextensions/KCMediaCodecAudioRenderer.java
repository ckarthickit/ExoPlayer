package com.karthick.android.kcextensions;

import android.os.Handler;

import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

import javax.annotation.Nullable;

public class KCMediaCodecAudioRenderer extends com.google.android.exoplayer2.audio.MediaCodecAudioRenderer {
    private static final String TAG = "CustomMediaCodecAudioRenderer";
    private final DecoratedAudioSink audioSink;
    //We are interested only in decorating Audio Sink
    public KCMediaCodecAudioRenderer(MediaCodecSelector mediaCodecSelector,
                                     @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                     boolean playClearSamplesWithoutKeys, @Nullable Handler eventHandler,
                                     @Nullable AudioRendererEventListener eventListener,
                                     @Nullable AudioCapabilities audioCapabilities, AudioProcessor... audioProcessors) {
        this(mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener, new DecoratedAudioSink(audioCapabilities, audioProcessors));
    }


    protected KCMediaCodecAudioRenderer(MediaCodecSelector mediaCodecSelector,
                                        @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                        boolean playClearSamplesWithoutKeys, @Nullable Handler eventHandler,
                                        @Nullable AudioRendererEventListener eventListener, DecoratedAudioSink audioSink) {
        super(mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener, audioSink);
        this.audioSink = audioSink;
    }

    public double getIntegratedLoudness() {
        return audioSink.getIntegratedLoudness();
    }

    /*@Override
    protected void onEnabled(boolean joining) throws ExoPlaybackException {
        super.onEnabled(joining);
    }

    @Override
    protected void onStarted() {
        super.onStarted();
    }

    @Override
    protected void onStopped() {
        super.onStopped();
    }

    @Override
    protected void onDisabled() {
        super.onDisabled();
    }*/
}
