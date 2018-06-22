package com.karthick.android.kcextensions.external;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer;
import com.google.android.exoplayer2.audio.SimpleDecoderAudioRenderer;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;
import com.google.android.exoplayer2.util.TimestampAdjuster;
import com.karthick.android.kcextensions.DecoratedAudioSink;
import com.karthick.android.kcextensions.KCMediaCodecAudioRenderer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class KCDefaultRenderersFactory extends com.google.android.exoplayer2.DefaultRenderersFactory {
    private static final String TAG = "KCRenderersFactory";
    public KCDefaultRenderersFactory(Context context,
                                     @com.google.android.exoplayer2.DefaultRenderersFactory.ExtensionRendererMode int extensionRendererMode) {
        super(context, extensionRendererMode, DEFAULT_ALLOWED_VIDEO_JOINING_TIME_MS);
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
                Renderer overrideAudioRenderer = new KCMediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, drmSessionManager, true,
                        eventHandler, eventListener, AudioCapabilities.getCapabilities(context), audioProcessors);
                out.remove(index);
                out.add(index, overrideAudioRenderer);
            }
            /******************** REFLECTIVELY FIND FFPMPEG RENDERER **********************/
            Class<?> ffmpegAudioRenderer = null;
            Constructor<?> ffmpegAudioRendererConstructor = null;
            try {
                // Full class names used for constructor args so the LINT rule triggers if any of them move.
                // LINT.IfChange
                ffmpegAudioRenderer =
                        Class.forName("com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer");
                /*ffmpegAudioRendererConstructor =
                        ffmpegAudioRenderer.getConstructor(
                                android.os.Handler.class,
                                com.google.android.exoplayer2.audio.AudioRendererEventListener.class,
                                com.google.android.exoplayer2.audio.AudioProcessor[].class);
                Renderer renderer =
                        (Renderer) ffmpegAudioRendererConstructor.newInstance(eventHandler, eventListener, audioProcessors);*/

                ffmpegAudioRendererConstructor =
                        ffmpegAudioRenderer.getConstructor(
                                android.os.Handler.class,
                                com.google.android.exoplayer2.audio.AudioRendererEventListener.class,
                                com.google.android.exoplayer2.audio.AudioSink.class,
                                boolean.class);
                // LINT.ThenChange(../../../../../../../proguard-rules.txt)

            } catch (ClassNotFoundException e) {
                // Expected if the app was built without the extension.
            } catch (Exception e) {
                // The extension is present, but instantiation failed.
                throw new RuntimeException("Error instantiating FFmpeg extension", e);
            }
            /*****************************************************************************/
            if (out.get(index).getClass().equals(ffmpegAudioRenderer)) {
                try {
                    Renderer overrideAudioRenderer = (Renderer) ffmpegAudioRendererConstructor.newInstance(
                            eventHandler,
                            eventListener,
                            new DecoratedAudioSink(null,audioProcessors),
                            false);
                    out.remove(index);
                    out.add(index, overrideAudioRenderer);
                    Log.i(TAG,"Successfully Overridden ffmpeg Audio Renderer!");
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            //TODO :Add Code to Decorate SimpleDecoderAudioRenderer as well
        }
    }

}
