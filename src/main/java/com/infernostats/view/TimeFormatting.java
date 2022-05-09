package com.infernostats.view;

import com.infernostats.model.Wave;
import net.runelite.api.Constants;

import java.time.Duration;

public class TimeFormatting {
	public static String getCurrentWaveTime(Wave wave) {
		return formatGameTicks(wave.getDuration());
	}

	public static String getCurrentTotalTime(Wave wave) {
		return formatGameTicks((wave.getStart() + wave.getDuration()));
	}

	public static String getSplitTime(Wave wave) {
		return formatGameTicks(wave.getStart());
	}

	public static String getSplitDelta(Wave wave, Wave prev) {
		if (prev == null)
			return getSplitTime(wave);
		return formatDuration(wave.start().minus(prev.start()));
	}

	public static String getCurrentWaveTimeCSV(Wave wave) {
		return formatGameTicksCSV(wave.getDuration());
	}

	public static String getSplitTimeCSV(Wave wave) {
		return formatGameTicksCSV(wave.getStart());
	}

	public static String getSplitDeltaCSV(Wave wave, Wave prev) {
		if (prev == null)
			return getSplitTimeCSV(wave);
		return formatDurationCSV(wave.start().minus(prev.start()));
	}

	public static String formatGameTicks(long gameTicks) {
		long seconds = Duration.ofMillis(gameTicks * Constants.GAME_TICK_LENGTH).getSeconds();
		return String.format("%02d:%02d", seconds / 60, seconds % 60);
	}

	public static String formatDuration(Duration duration) {
		long seconds = duration.toMillis() / 1000;
		return String.format("%02d:%02d", seconds / 60, seconds % 60);
	}

	public static String formatGameTicksCSV(long gameTicks) {
		long s = Duration.ofMillis(gameTicks * Constants.GAME_TICK_LENGTH).getSeconds();
		return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
	}

	public static String formatDurationCSV(Duration duration) {
		long s = duration.toMillis() / 1000;
		return String.format("%02d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
	}
}
