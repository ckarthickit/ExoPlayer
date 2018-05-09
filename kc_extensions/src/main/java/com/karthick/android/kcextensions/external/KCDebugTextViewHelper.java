package com.karthick.android.kcextensions.external;

import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;

import java.text.DecimalFormat;

public class KCDebugTextViewHelper extends DebugTextViewHelper {
    private static final DecimalFormat m2DecimalPlaces = new DecimalFormat("");
    private final KCSimpleExoPlayer player;
    /**
     * @param player   The {@link SimpleExoPlayer} from which debug information should be obtained.
     * @param textView The {@link TextView} that should be updated to display the information.
     */
    public KCDebugTextViewHelper(KCSimpleExoPlayer player, TextView textView) {
        super(player, textView);
        this.player= player;
    }

    /** Returns a string containing audio debugging information. */
    protected String getAudioString() {
        String audioString = super.getAudioString();

        return audioString +
                "\n" +"( " + "audio_loudness= " + String.format("%.2f",player.getIntegratedLoudness())  + " LUFS )";
     }
}
