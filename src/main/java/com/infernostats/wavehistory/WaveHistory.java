package com.infernostats.wavehistory;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WaveHistory {
    public List<Wave> waves;

    public WaveHistory()
    {
        waves = new ArrayList<>();
    }

    public void NewWave(int id, String splitTime)
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
}
