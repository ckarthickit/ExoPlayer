package com.karthick.android.kcextensions;

import android.os.SystemClock;
import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioCapabilities;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.DefaultAudioSink;
import com.google.android.exoplayer2.util.Util;

import java.nio.ByteBuffer;

/**
 * Decorated Audio Sink
 */
//TODO : Create a Backgruound Thread to pass Frames to EBUR128

public final class DecoratedAudioSink implements AudioSink {

    private static final String TAG = "DecoratedAudioSink";
    private final DefaultAudioSink defaultAudioSink;

    private ByteBuffer writtenByteBuffer = null;
    private byte[] copyBuffer;

    private EBUR128 ebur128;
    private long longLastLoudnessTime = -1L;

    public DecoratedAudioSink(AudioCapabilities capabilities, AudioProcessor[] processors) {
        defaultAudioSink = new DefaultAudioSink(capabilities, processors);
    }

    @Override
    public void setListener(AudioSink.Listener listener) {
        defaultAudioSink.setListener(listener);
    }

    @Override
    public boolean isEncodingSupported(int encoding) {
        return Util.isEncodingPcm(encoding); //Support only PCM as we need to calculate loudness
        //return defaultAudioSink.isEncodingSupported(encoding);
    }

    @Override
    public long getCurrentPositionUs(boolean sourceEnded) {
        return defaultAudioSink.getCurrentPositionUs(sourceEnded);
    }

    @Override
    public void configure(int inputEncoding, int inputChannelCount, int inputSampleRate, int specifiedBufferSize, int[] outputChannels, int trimStartSamples, int trimEndSamples) throws ConfigurationException {
        Log.i(TAG, String.format("configure_input: encoding=%d, channels=%d, sampleRate=%d", inputEncoding, inputChannelCount, specifiedBufferSize));
        defaultAudioSink.configure(inputEncoding, inputChannelCount, inputSampleRate, specifiedBufferSize, outputChannels, trimStartSamples, trimEndSamples);
        if (ebur128 != null) {
            ebur128.dispose();
            ebur128 = null;
        }
        if (inputEncoding == C.ENCODING_PCM_16BIT || inputEncoding == C.ENCODING_PCM_32BIT) {
            ebur128 = new EBUR128(exoToEBUR128AudioEncoding(inputEncoding), inputChannelCount, inputSampleRate);
            ebur128.initialize();
        } else {
            Log.w(TAG, "Unsupported PCM Encoding " + inputEncoding);
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
         * */
        do {
            if (buffer == writtenByteBuffer) {
                //already written this buffer completely. Ignore
                break;
            }
            if (ebur128 == null) {
                break;
            }
            if (buffer.hasArray()) {
                ebur128.addFrames(buffer.array(), buffer.position(), buffer.remaining());
            } else {
                int bytesRemaining = buffer.remaining();
                if (copyBuffer == null || copyBuffer.length < bytesRemaining) {
                    copyBuffer = new byte[bytesRemaining];
                }
                int originalPosition = buffer.position();
                buffer.get(copyBuffer, 0, bytesRemaining);
                buffer.position(originalPosition);
                ebur128.addFrames(copyBuffer, 0, bytesRemaining);
            }
            writtenByteBuffer = buffer;
            if (longLastLoudnessTime == -1L) {
                longLastLoudnessTime = SystemClock.elapsedRealtime();
            }
            if (SystemClock.elapsedRealtime() - longLastLoudnessTime > 1000) {
                if (ebur128 != null) {
                    double loudness = ebur128.getIntegratedLoudness();
                    Log.i(TAG, "loudness = " + loudness);
                    longLastLoudnessTime = SystemClock.elapsedRealtime();
                }
            }
        } while (false);

        return defaultAudioSink.handleBuffer(buffer, presentationTimeUs);
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
        Log.i(TAG, "reset()");
        defaultAudioSink.reset();
    }

    @Override
    public void release() {
        Log.i(TAG, "release()");
        defaultAudioSink.release();
        dispose();
    }

    private void dispose() {
        Log.i(TAG, "dispose()");
        writtenByteBuffer = null;
        copyBuffer = null;
        longLastLoudnessTime = -1;
        if (ebur128 != null) {
            double loudness = ebur128.getIntegratedLoudness();
            Log.i(TAG, "loudness = " + loudness);
            ebur128.dispose();
            ebur128 = null;
        }
    }

    public double getIntegratedLoudness() {
        if(ebur128 != null) {
            return ebur128.getIntegratedLoudness();
        }else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    private static final EBUR128.AudioPcmFormat exoToEBUR128AudioEncoding(int exoAudioEncoding) {
        if (exoAudioEncoding == C.ENCODING_PCM_16BIT) {
            return EBUR128.AudioPcmFormat.ENCODING_16_BIT;
        } else if (exoAudioEncoding == C.ENCODING_PCM_32BIT) {
            return EBUR128.AudioPcmFormat.ENCODING_32_BIT;
        }
        return null;
    }
}
