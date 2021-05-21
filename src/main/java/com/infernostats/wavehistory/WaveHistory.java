package com.infernostats.wavehistory;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WaveHistory {
    public static List<Wave> waves;

    public WaveHistory()
    {
        waves = new ArrayList<>();
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

    public String ToCSV(boolean splitWaves)
    {
        StringBuilder csv = new StringBuilder();
        csv.append("wave,split,time\n");

        for (Wave wave : waves)
        {
            if (!splitWaves || (splitWaves && wave.IsSplit()))
            {
                csv.append("" + wave.id + "," + wave.SplitTimeString() + "," + wave.WaveTimeString() + "\n");
            }
        }

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
