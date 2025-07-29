package com.infernostats.view;

import com.infernostats.model.Wave;
import net.runelite.api.Constants;

import java.time.Duration;

public class TimeFormatting {
  public static final double SECONDS_PER_TICK = (double) Constants.GAME_TICK_LENGTH / 1_000;

  public static String getSplitTime(Wave wave) {
    return formatTicksShort(wave.getStart());
  }

  public static String getCurrentWaveTime(Wave wave) {
    return formatTicksShort(wave.getDuration());
  }

  public static String getCurrentTotalTime(Wave wave) {
    return formatTicks(wave.getStart() + wave.getDuration());
  }

  public static String getSplitDelta(Wave wave, Wave prev) {
    return (prev == null)
        ? formatTicksShort(wave.getStart())
        : formatDuration(wave.start().minus(prev.start()));
  }

  public static String formatTicks(long ticks) {
    double totalSeconds = ticks * SECONDS_PER_TICK;

    int minutes = (int) (totalSeconds / 60);
    int seconds = (int) (totalSeconds % 60);

    int tenths = (int) Math.round((totalSeconds - minutes * 60 - seconds) * 10);

    return String.format("%02d:%02d.%d", minutes, seconds, tenths);
  }

  public static String formatTicksShort(long ticks) {
    double totalSeconds = ticks * SECONDS_PER_TICK;

    int minutes = (int) (totalSeconds / 60);
    int seconds = (int) (totalSeconds % 60);

    return String.format("%02d:%02d", minutes, seconds);
  }

  public static String formatDuration(Duration duration) {
    long seconds = duration.toMillis() / 1000;
    return String.format("%02d:%02d", seconds / 60, seconds % 60);
  }
}
