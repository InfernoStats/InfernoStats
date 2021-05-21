package com.infernostats.wavehistory;

import com.infernostats.InfernoStatsPlugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

public class WaveTimer extends InfoBox {
    public static final Pattern WAVE_MESSAGE = Pattern.compile("Wave: (\\d+)");
    public static final Pattern WAVE_COMPLETE_MESSAGE = Pattern.compile("Wave completed!");
    public static final Pattern DEFEATED_MESSAGE = Pattern.compile("You have been defeated!");
    public static final Pattern COMPLETE_MESSAGE = Pattern.compile("Your (?:TzTok-Jad|TzKal-Zuk) kill count is:");
    public static final Pattern PAUSED_MESSAGE = Pattern.compile("The (?:Inferno|Fight Cave) has been paused. You may now log out.");

    public static Instant startTime;
    public static Instant lastTime;

    // Creates a timer that counts up if lastTime is null, or a paused timer if lastTime is defined
    public WaveTimer(BufferedImage image, InfernoStatsPlugin plugin, Instant startTime, Instant lastTime)
    {
        super(image, plugin);
        this.startTime = startTime;
        this.lastTime = lastTime;
    }

    @Override
    public String getText()
    {
        return GetTime();
    }

    @Override
    public Color getTextColor()
    {
        return Color.WHITE;
    }

    @Override
    public String getTooltip()
    {
        return "Elapsed time: " + GetTime();
    }

    public String GetTime()
    {
        long seconds;

        if (startTime == null)
        {
            return null;
        }

        seconds = Duration.between(startTime, lastTime == null ? Instant.now() : lastTime).getSeconds();

        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }

    public Duration SplitTime()
    {
        if (startTime == null)
        {
            return null;
        }

        return Duration.between(startTime, lastTime == null ? Instant.now() : lastTime);
    }

    public void Pause()
    {
        this.lastTime = Instant.now();
    }

    public boolean IsPaused()
    {
        return this.lastTime != null;
    }

    public void Resume()
    {
        this.startTime = this.startTime.plus(
                Duration.between(this.lastTime, Instant.now())
        );

        this.lastTime = null;
    }
}
