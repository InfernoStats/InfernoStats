package com.infernostats.wavehistory;

import com.google.inject.Inject;
import com.infernostats.InfernoStatsConfig;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WaveHistory {
    private InfernoStatsConfig config;
    public static List<Wave> waves;

    @Inject
    public WaveHistory(InfernoStatsConfig config)
    {
        this.config = config;
        this.waves = new ArrayList<>();
    }

    public void NewWave(int id, Duration splitTime)
    {
        waves.add(new Wave(id, splitTime));
    }

    public Wave CurrentWave()
    {
        return waves.get(waves.size() - 1);
    }

    public void AddSpawn(WorldPoint tile, NPC npc)
    {
        Wave wave = CurrentWave();
        wave.AddSpawn(tile, npc);
    }

    public void ClearWaves()
    {
        waves.clear();
    }

    public int getTotalIdleTicks() {
        int totalIdleTicks = waves.stream()
                .map(wave -> wave.idleTicks)
                .reduce(0, (prevVal, newVal) -> prevVal + newVal);
        return totalIdleTicks;
    }

    public String ToCSV(boolean splitWaves)
    {
        StringBuilder csv = new StringBuilder();
        csv.append("wave,split,time");
        if (config.trackIdleTicks()) {
            csv.append(",idle");
        }
        csv.append("\n");

        for (Wave wave : waves)
        {
            if (!splitWaves || (splitWaves && wave.IsSplit()))
            {
                csv.append("" + wave.id + "," + wave.SplitTimeString() + "," + wave.WaveTimeString());
                if (config.trackIdleTicks()) {
                    csv.append("," + wave.idleTicks);
                }
                csv.append("\n");
            }
        }
        Wave lastWave = waves.get(waves.size() - 1);
        csv.append("end," + lastWave.SplitTimeString() + "," + lastWave.SplitTimeString());
        if(config.trackIdleTicks()) {
            csv.append("," + getTotalIdleTicks());
        }
        csv.append("\n");
        return csv.toString();
    }

    public String CSVName(boolean splitWaves)
    {
        Wave currWave = CurrentWave();
        String wavesText = splitWaves ? "Splits.csv" : "Full.csv";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        String timeText = formatter.format(LocalDateTime.now());

        if (currWave.failed)
        {
            return timeText + " Failed KC on Wave " + currWave.id + ", " + wavesText;
        }
        else
        {
            return timeText + " Successful KC on Wave " + currWave.id + ", " + wavesText;
        }
    }
}
