package com.karthick.android.kcextensions.external;

import android.content.Context;
import android.os.Handler;

import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.karthick.android.kcextensions.KCMediaCodecAudioRenderer;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class DefaultRenderersFactory extends com.google.android.exoplayer2.DefaultRenderersFactory {

    public DefaultRenderersFactory(Context context,
                                   @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                   @com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode) {
        super(context, drmSessionManager, extensionRendererMode, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
    }

    @Override
    protected void buildAudioRenderers(Context context,
                                       @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                       AudioProcessor[] audioProcessors, Handler eventHandler,
                                       AudioRendererEventListener eventListener, @com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode,
                                       ArrayList<Renderer> out) {
        super.buildAudioRenderers(context, drmSessionManager, audioProcessors, eventHandler, eventListener, extensionRendererMode, out);
        for (int index = 0; index < out.size(); index++) {
            if (out.get(index) instanceof MediaCodecAudioRenderer) {
                MediaCodecAudioRenderer audioRenderer = (MediaCodecAudioRenderer) out.get(index);
                Renderer overrideAudioRenderer = new KCMediaCodecAudioRenderer(MediaCodecSelector.DEFAULT, drmSessionManager, true,
                        eventHandler, eventListener, AudioCapabilities.getCapabilities(context), audioProcessors);
                out.remove(index);
                out.add(index, overrideAudioRenderer);
            }
            //TODO :Add Code to Decorate SimpleDecoderAudioRenderer as well
        }
    }

}
