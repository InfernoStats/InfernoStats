package com.infernostats.model;

import com.google.common.collect.ImmutableMap;
import com.infernostats.InfernoStatsConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Constants;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
public final class WaveSplit {
	private static ArrayList<String> targetSplits;

	public static void UpdateTargetSplits(InfernoStatsConfig config) {
		targetSplits = new ArrayList<>(Arrays.asList(
				config.targetWave9Split(),
				config.targetWave18Split(),
				config.targetWave25Split(),
				config.targetWave35Split(),
				config.targetWave42Split(),
				config.targetWave50Split(),
				config.targetWave57Split(),
				config.targetWave60Split(),
				config.targetWave63Split(),
				config.targetWave66Split(),
				config.targetWave67Split(),
				config.targetWave68Split(),
				config.targetWave69Split()
		));
	}

	private static Duration ParseConfigSplit(Split split) {
		String targetSplit = targetSplits.get(split.ordinal());
		if (targetSplit.equals(""))
			return null;

		String[] time = targetSplit.split(":");
		if (time.length != 2)
			return null;

		// At most we want 9999 minutes and seconds should always be %02d
		if (time[0].length() > 4 || time[1].length() != 2)
			return null;

		int seconds, minutes;
		try {
			minutes = Integer.parseInt(time[0]);
			seconds = Integer.parseInt(time[1]);
		} catch (NumberFormatException e) {
			return null;
		}

		return Duration.ofMinutes(minutes).plusSeconds(seconds);
	}

	public static String GoalDifference(Wave wave) {
		Split split = WaveSplitMap.get(wave.getId());

		Duration configSplit = ParseConfigSplit(split);
		if (configSplit == null)
			return "";

		// Duration.toSeconds() is supported in Java 9
		long waveSplitSeconds = wave.start().toMillis() / 1000;
		long configSplitSeconds = configSplit.toMillis() / 1000;

		int comparison = configSplit.compareTo(wave.start());
		if (comparison > 0) {
			long difference = configSplitSeconds - waveSplitSeconds;
			String minutes = String.valueOf(difference / 60);
			String seconds = String.format("%02d", difference % 60);
			return " (-" + minutes + ":" + seconds + ")";
		} else if (comparison < 0) {
			long difference = waveSplitSeconds - configSplitSeconds;
			String minutes = String.valueOf(difference / 60);
			String seconds = String.format("%02d", difference % 60);
			return " (+" + minutes + ":" + seconds + ")";
		}

		return " (-0)";
	}

	public static Duration PredictedTime(Wave wave) {
		long waveStartMillis = wave.getStart() * Constants.GAME_TICK_LENGTH;
		float percentComplete = percentOfAvgTime(wave, checkPace(wave));
		return Duration.ofMillis((long) (waveStartMillis * (1 / percentComplete)));
	}

	private static Pace checkPace(Wave wave) {
		Split split = WaveSplitMap.get(wave.getId());
		for (Pace pace : Pace.values()) {
			Duration wavePace = splits[split.ordinal()][pace.ordinal()];
			if (wave.start().compareTo(wavePace) > 0) {
				return pace;
			}
		}
		return Pace.SUB45;
	}

	private static float percentOfAvgTime(Wave wave, Pace pace) {
		Split split = WaveSplitMap.get(wave.getId());
		return (float) splits[split.ordinal()][pace.ordinal()].toMillis() /
				splits[Split.FINISH.ordinal()][pace.ordinal()].toMillis();
	}

	public static final ImmutableMap<Integer, Split> WaveSplitMap = ImmutableMap.<Integer, Split>builder()
			.put(9, Split.SPLIT9)
			.put(18, Split.SPLIT18)
			.put(25, Split.SPLIT25)
			.put(35, Split.SPLIT35)
			.put(42, Split.SPLIT42)
			.put(50, Split.SPLIT50)
			.put(57, Split.SPLIT57)
			.put(60, Split.SPLIT60)
			.put(63, Split.SPLIT63)
			.put(66, Split.SPLIT66)
			.put(67, Split.SPLIT67)
			.put(68, Split.SPLIT68)
			.put(69, Split.SPLIT69)
			.build();

	private enum Split {
		SPLIT9,
		SPLIT18,
		SPLIT25,
		SPLIT35,
		SPLIT42,
		SPLIT50,
		SPLIT57,
		SPLIT60,
		SPLIT63,
		SPLIT66,
		SPLIT67,
		SPLIT68,
		SPLIT69,
		FINISH
	}

	private enum Pace {
		SUB75,
		SUB60,
		SUB55,
		SUB50,
		SUB45
	}

	// The spreadsheet represents milliseconds as a fraction of 60.
	// E.g. 3:12:12 = 3 minutes, 12 seconds, 12 fractional milliseconds
	//              = 3 minutes, 12 seconds, 200 milliseconds
	// as (12 / 60) * 1000 = 200
	private static Duration splitTime(int minutes, int seconds, int fractMillis) {
		int milliseconds = fractMillis / 60 * 1000;
		return Duration.ofMinutes(minutes).plusSeconds(seconds).plusMillis(milliseconds);
	}

	private static final Duration[][] splits = {
			{ // Wave 9
					splitTime(3, 12, 57),
					splitTime(2, 48, 45),
					splitTime(2, 38, 42),
					splitTime(2, 34, 59),
					splitTime(2, 21, 0),
			},
			{ // Wave 18
					splitTime(8, 6, 12),
					splitTime(7, 9, 58),
					splitTime(6, 32, 1),
					splitTime(6, 21, 27),
					splitTime(5, 43, 0),
			},
			{ // Wave 25
					splitTime(12, 38, 5),
					splitTime(11, 10, 54),
					splitTime(10, 20, 25),
					splitTime(9, 59, 32),
					splitTime(8, 51, 0),
			},
			{ // Wave 35
					splitTime(19, 59, 27),
					splitTime(17, 51, 11),
					splitTime(16, 18, 36),
					splitTime(15, 49, 34),
					splitTime(14, 1, 0),
			},
			{ // Wave 42
					splitTime(26, 4, 23),
					splitTime(23, 13, 13),
					splitTime(21, 10, 40),
					splitTime(20, 31, 8),
					splitTime(18, 9, 0),
			},
			{ // Wave 50
					splitTime(34, 44, 58),
					splitTime(30, 42, 4),
					splitTime(27, 57, 20),
					splitTime(27, 0, 28),
					splitTime(23, 50, 0),
			},
			{ // Wave 57
					splitTime(43, 6, 49),
					splitTime(37, 53, 26),
					splitTime(34, 21, 23),
					splitTime(33, 5, 17),
					splitTime(29, 8, 0),
			},
			{ // Wave 60
					splitTime(46, 49, 1),
					splitTime(41, 5, 58),
					splitTime(37, 11, 41),
					splitTime(35, 53, 1),
					splitTime(31, 35, 0),
			},
			{ // Wave 63
					splitTime(51, 29, 49),
					splitTime(44, 57, 13),
					splitTime(40, 36, 18),
					splitTime(39, 2, 40),
					splitTime(34, 28, 0),
			},
			{ // Wave 66
					splitTime(56, 2, 37),
					splitTime(48, 36, 32),
					splitTime(43, 53, 21),
					splitTime(42, 6, 38),
					splitTime(37, 13, 0),
			},
			{ // Wave 67
					splitTime(56, 52, 39),
					splitTime(49, 27, 4),
					splitTime(44, 40, 34),
					splitTime(42, 51, 37),
					splitTime(37, 56, 0),
			},
			{ // Wave 68
					splitTime(58, 6, 13),
					splitTime(50, 28, 47),
					splitTime(45, 37, 21),
					splitTime(43, 43, 28),
					splitTime(38, 41, 0),
			},
			{ // Wave 69
					splitTime(61, 14, 23),
					splitTime(53, 3, 33),
					splitTime(48, 11, 44),
					splitTime(46, 9, 11),
					splitTime(40, 53, 0),
			},
			{ // Finish
					splitTime(67, 6, 6),
					splitTime(57, 39, 56),
					splitTime(51, 42, 17),
					splitTime(49, 15, 59),
					splitTime(43, 43, 0),
			},
	};
}
