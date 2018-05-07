package com.karthick.android.kcextensions;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.DefaultAudioSink;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Nullable;

public class MediaCodecAudioRenderer extends com.google.android.exoplayer2.audio.MediaCodecAudioRenderer {
    private static final String TAG = "KC_" +MediaCodecAudioRenderer.class.getSimpleName();
    //We are interested only in decorating Audio Sink
    public MediaCodecAudioRenderer(MediaCodecSelector mediaCodecSelector,
                                   @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                   boolean playClearSamplesWithoutKeys, @Nullable Handler eventHandler,
                                   @Nullable AudioRendererEventListener eventListener,
                                   @Nullable AudioCapabilities audioCapabilities, AudioProcessor... audioProcessors) {
        this(mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener, new DecoratedDefaultAudioSink(audioCapabilities, audioProcessors));
    }


    protected MediaCodecAudioRenderer(MediaCodecSelector mediaCodecSelector,
                                   @Nullable DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                                   boolean playClearSamplesWithoutKeys, @Nullable Handler eventHandler,
                                   @Nullable AudioRendererEventListener eventListener, AudioSink audioSink) {
        super(mediaCodecSelector, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener, audioSink);
    }

    private static class DecoratedDefaultAudioSink implements AudioSink {

        private FileOutputStream  fileOutputStream = null;
        private ByteBuffer writtenByteBuffer = null;
        private byte[] copyBuffer;
        private final DefaultAudioSink defaultAudioSink;

        private DecoratedDefaultAudioSink(AudioCapabilities capabilities, AudioProcessor[] processors) {
            defaultAudioSink = new DefaultAudioSink(capabilities, processors);
        }

        @Override
        public void setListener(Listener listener) {
            defaultAudioSink.setListener(listener);
        }

        @Override
        public boolean isEncodingSupported(int encoding) {
            return defaultAudioSink.isEncodingSupported(encoding);
        }

        @Override
        public long getCurrentPositionUs(boolean sourceEnded) {
            return defaultAudioSink.getCurrentPositionUs(sourceEnded);
        }

        @Override
        public void configure(int inputEncoding, int inputChannelCount, int inputSampleRate, int specifiedBufferSize, int[] outputChannels, int trimStartSamples, int trimEndSamples) throws ConfigurationException {
            Log.i(TAG,String.format("configure_input: encoding=%d, channels=%d, sampleRate=%d",inputEncoding,inputChannelCount,specifiedBufferSize));
            defaultAudioSink.configure(inputEncoding,inputChannelCount,inputSampleRate,specifiedBufferSize,outputChannels,trimStartSamples,trimEndSamples);

            File file = new File("/sdcard/", String.format("pcm_%s_%s_%s", SystemClock.elapsedRealtime(),inputChannelCount,inputSampleRate));
            if(file.exists()) {
                file.delete();
            }
            try {
                fileOutputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                fileOutputStream = null;
            }
        }

        @Override
        public void play() {
            defaultAudioSink.play();
        }

        @Override
        public void handleDiscontinuity() {
            defaultAudioSink.handleDiscontinuity();
        }

        @Override
        public boolean handleBuffer(ByteBuffer buffer, long presentationTimeUs) throws InitializationException, WriteException {
            /**
             *  Default Audio Sink Drops Buffer in the following Scenarios :
             *  1) Empty ByteBuffer
             *  2) Frames-Per-Encoded-Sample is not Know in a NON-PCM Encoding
             *  3)
             * */
            if(fileOutputStream != null) {
                do {
                    if(buffer == writtenByteBuffer) {
                        //already written this buffer completely. Ignore
                        break;
                    }
                    try {
                        if (buffer.hasArray()) {
                            fileOutputStream.write(buffer.array(),buffer.position(),buffer.remaining());
                        } else {
                            int bytesRemaining = buffer.remaining();
                            if (copyBuffer == null || copyBuffer.length < bytesRemaining) {
                                copyBuffer = new byte[bytesRemaining];
                            }
                            int originalPosition = buffer.position();
                            buffer.get(copyBuffer, 0, bytesRemaining);
                            buffer.position(originalPosition);
                            fileOutputStream.write(copyBuffer,0,bytesRemaining);
                        }
                        writtenByteBuffer = buffer;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }while (false);
            }
            boolean isHandledCompletely = defaultAudioSink.handleBuffer(buffer,presentationTimeUs);
            return isHandledCompletely;
        }

        @Override
        public void playToEndOfStream() throws WriteException {
            defaultAudioSink.playToEndOfStream();
        }

        @Override
        public boolean isEnded() {
            return defaultAudioSink.isEnded();
        }

        @Override
        public boolean hasPendingData() {
            return defaultAudioSink.hasPendingData();
        }

        @Override
        public PlaybackParameters setPlaybackParameters(PlaybackParameters playbackParameters) {
            return defaultAudioSink.setPlaybackParameters(playbackParameters);
        }

        @Override
        public PlaybackParameters getPlaybackParameters() {
            return defaultAudioSink.getPlaybackParameters();
        }

        @Override
        public void setAudioAttributes(AudioAttributes audioAttributes) {
            defaultAudioSink.setAudioAttributes(audioAttributes);
        }

        @Override
        public void setAudioSessionId(int audioSessionId) {
            defaultAudioSink.setAudioSessionId(audioSessionId);
        }

        @Override
        public void enableTunnelingV21(int tunnelingAudioSessionId) {
            defaultAudioSink.enableTunnelingV21(tunnelingAudioSessionId);
        }

        @Override
        public void disableTunneling() {
            defaultAudioSink.disableTunneling();
        }

        @Override
        public void setVolume(float volume) {
            defaultAudioSink.setVolume(volume);
        }

        @Override
        public void pause() {
            defaultAudioSink.pause();
        }

        @Override
        public void reset() {
            defaultAudioSink.reset();
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    fileOutputStream = null;
                }

            }
        }

        @Override
        public void release() {
            defaultAudioSink.release();
            if(fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    fileOutputStream = null;
                }

            }
        }
    }
}
