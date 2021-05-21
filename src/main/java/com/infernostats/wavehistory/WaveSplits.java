package com.infernostats.wavehistory;

import com.infernostats.InfernoStatsConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WaveSplits {
    static InfernoStatsConfig config;
    static ArrayList<String> targetSplits;

    public WaveSplits(InfernoStatsConfig config) {
        this.config = config;
        UpdateTargetSplits();
    }

    public Duration PredictedTime(Wave wave) {
        Float percentComplete = percentOfAvgTime(wave, checkPace(wave));
        return Duration.ofMillis((long) (wave.splitTime.toMillis() * (1 / percentComplete)));
    }

    private Float percentOfAvgTime(Wave wave, Pace pace) {
        WaveSplit split = SplitMap.get(wave.id);
        return (
                (float) (splits[split.ordinal()][pace.ordinal()].toMillis()) /
                        splits[WaveSplit.FINISH.ordinal()][pace.ordinal()].toMillis()
        );
    }

    private Pace checkPace(Wave wave) {
        WaveSplit waveSplit = SplitMap.get(wave.id);
        for (Pace pace : Pace.values()) {
            Duration wavePace = splits[waveSplit.ordinal()][pace.ordinal()];
            if (wave.splitTime.compareTo(wavePace) > 0) {
                return pace;
            }
        }
        return Pace.SUB50;
    }

    public Duration ParseConfigSplit(WaveSplit waveSplit)
    {
        String targetSplit = targetSplits.get(waveSplit.ordinal());
        if (targetSplit == "")
        {
            return null;
        }

        String[] time = targetSplit.split(":");
        if (time.length != 2)
        {
            return null;
        }

        // At most we want 9999 minutes and seconds should always be %02d
        if (time[0].length() > 4 || time[1].length() != 2)
        {
            return null;
        }

        int seconds, minutes;
        try
        {
            minutes = Integer.parseInt(time[0]);
            seconds = Integer.parseInt(time[1]);
        }
        catch (NumberFormatException e)
        {
            return null;
        }

        return Duration.ofMinutes(minutes).plusSeconds(seconds);
    }

    public String GoalDifference(Wave wave)
    {
        WaveSplit waveSplit = SplitMap.get(wave.id);

        Duration configSplit = ParseConfigSplit(waveSplit);
        if (configSplit == null)
        {
            return "";
        }

        long configSplitSeconds = configSplit.toMillis() / 1000;
        long waveSplitSeconds = wave.splitTime.toMillis() / 1000;

        int comparison = configSplit.compareTo(wave.splitTime);
        if (comparison > 0)
        {
            long difference = configSplitSeconds - waveSplitSeconds;
            String minutes = String.valueOf(difference / 60);
            String seconds = String.format("%02d", difference % 60);
            return " (-" + minutes + ":" + seconds + ")";
        }
        else if (comparison < 0)
        {
            long difference = waveSplitSeconds - configSplitSeconds;
            String minutes = String.valueOf(difference / 60);
            String seconds = String.format("%02d", difference % 60);
            return " (+" + minutes + ":" + seconds + ")";
        }

        return " (-0)";
    }

    public void UpdateTargetSplits()
    {
        targetSplits = new ArrayList<String>() {{
            add(config.targetWave9Split());
            add(config.targetWave18Split());
            add(config.targetWave25Split());
            add(config.targetWave35Split());
            add(config.targetWave42Split());
            add(config.targetWave50Split());
            add(config.targetWave57Split());
            add(config.targetWave60Split());
            add(config.targetWave63Split());
            add(config.targetWave66Split());
            add(config.targetWave67Split());
            add(config.targetWave68Split());
            add(config.targetWave69Split());
        }};
    }

    static Map<Integer, WaveSplit> SplitMap = new HashMap<Integer, WaveSplit>() {{
        put(9, WaveSplit.SPLIT9);
        put(18, WaveSplit.SPLIT18);
        put(25, WaveSplit.SPLIT25);
        put(35, WaveSplit.SPLIT35);
        put(42, WaveSplit.SPLIT42);
        put(50, WaveSplit.SPLIT50);
        put(57, WaveSplit.SPLIT57);
        put(60, WaveSplit.SPLIT60);
        put(63, WaveSplit.SPLIT63);
        put(66, WaveSplit.SPLIT66);
        put(67, WaveSplit.SPLIT67);
        put(68, WaveSplit.SPLIT68);
        put(69, WaveSplit.SPLIT69);
    }};

    enum WaveSplit {
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

    enum Pace {
        SUB75,
        SUB60,
        SUB55,
        SUB50
    }

    private static Duration splitTime(int minutes, int seconds) {
        return Duration.ofMinutes(minutes).plus(Duration.ofSeconds(seconds));
    }

    private static Duration[][] splits = {
            { // Wave 9
                    splitTime(3, 12),
                    splitTime(2, 48),
                    splitTime(2, 38),
                    splitTime(2, 34),
            },
            { // Wave 18
                    splitTime(8, 6),
                    splitTime(7, 9),
                    splitTime(6, 32),
                    splitTime(6, 21),
            },
            { // Wave 25
                    splitTime(12, 38),
                    splitTime(11, 10),
                    splitTime(10, 20),
                    splitTime(9, 59),
            },
            { // Wave 35
                    splitTime(19, 59),
                    splitTime(17, 51),
                    splitTime(16, 18),
                    splitTime(15, 49),
            },
            { // Wave 42
                    splitTime(26, 4),
                    splitTime(23, 13),
                    splitTime(21, 10),
                    splitTime(20, 31),
            },
            { // Wave 50
                    splitTime(34, 44),
                    splitTime(30, 42),
                    splitTime(27, 57),
                    splitTime(27, 00),
            },
            { // Wave 57
                    splitTime(43, 6),
                    splitTime(37, 53),
                    splitTime(34, 21),
                    splitTime(33, 5),
            },
            { // Wave 60
                    splitTime(46, 49),
                    splitTime(41, 5),
                    splitTime(37, 11),
                    splitTime(35, 53),
            },
            { // Wave 63
                    splitTime(51, 29),
                    splitTime(44, 57),
                    splitTime(40, 36),
                    splitTime(39, 2),
            },
            { // Wave 66
                    splitTime(56, 2),
                    splitTime(48, 36),
                    splitTime(43, 53),
                    splitTime(42, 6),
            },
            { // Wave 67
                    splitTime(56, 52),
                    splitTime(49, 27),
                    splitTime(44, 40),
                    splitTime(42, 51),
            },
            { // Wave 68
                    splitTime(58, 6),
                    splitTime(50, 28),
                    splitTime(45, 37),
                    splitTime(43, 43),
            },
            { // Wave 69
                    splitTime(61, 14),
                    splitTime(53, 3),
                    splitTime(48, 11),
                    splitTime(46, 9),
            },
            { // Finish
                    splitTime(67, 6),
                    splitTime(57, 39),
                    splitTime(51, 42),
                    splitTime(49, 15),
            },
    };
}
