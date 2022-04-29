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

	public static String formatGameTicks(long gameTicks) {
		long seconds = Duration.ofMillis(gameTicks * Constants.GAME_TICK_LENGTH).getSeconds();
		return String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60);
	}

	public static String formatDuration(Duration duration) {
		long seconds = duration.toMillis() / 1000;
		return String.format("%02d", seconds / 60) + ":" + String.format("%02d", seconds % 60);
	}
}
