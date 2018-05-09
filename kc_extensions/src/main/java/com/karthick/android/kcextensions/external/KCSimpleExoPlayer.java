package com.karthick.android.kcextensions.external;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Clock;
import com.karthick.android.kcextensions.KCMediaCodecAudioRenderer;

public class KCSimpleExoPlayer extends SimpleExoPlayer {

    public KCSimpleExoPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        this(renderersFactory, trackSelector, new DefaultLoadControl(), drmSessionManager);
    }

    protected KCSimpleExoPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        super(renderersFactory, trackSelector, loadControl, drmSessionManager);
    }

    public double getIntegratedLoudness() {
        for (Renderer renderer : renderers) {
            if (renderer instanceof KCMediaCodecAudioRenderer) {
                return ((KCMediaCodecAudioRenderer) renderer).getIntegratedLoudness();
            }
        }
        return Double.NEGATIVE_INFINITY;
    }
}
