package com.karthick.android.kcextensions.external;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Clock;
import com.karthick.android.kcextensions.KCMediaCodecAudioRenderer;

public class KCSimpleExoPlayer extends SimpleExoPlayer {

    public KCSimpleExoPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector) {
        this(renderersFactory, trackSelector, new DefaultLoadControl());
    }
    protected KCSimpleExoPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl) {
        super(renderersFactory, trackSelector, loadControl);
    }

    protected KCSimpleExoPlayer(RenderersFactory renderersFactory, TrackSelector trackSelector, LoadControl loadControl, Clock clock) {
        super(renderersFactory, trackSelector, loadControl, clock);
    }

    public double getIntegratedLoudness() {
        for(Renderer renderer: renderers) {
            if(renderer instanceof KCMediaCodecAudioRenderer) {
                return ((KCMediaCodecAudioRenderer)renderer).getIntegratedLoudness();
            }
        }
        return Double.NEGATIVE_INFINITY;
    }
}
