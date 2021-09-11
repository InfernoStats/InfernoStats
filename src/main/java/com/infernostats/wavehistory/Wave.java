package com.infernostats.wavehistory;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Spawn {
    public final String name;
    public final WorldPoint tile;
    public Spawn(WorldPoint tile, NPC npc) {
        this.name = npc.getName();
        this.tile = tile;
    }

    @Override
    public String toString()
    {
        return this.name + " (" + this.tile.getRegionX() + ", " + this.tile.getRegionY() + ")";
    }
}

@Slf4j
public class Wave {
    public int id;
    public List<Spawn> NPCSpawns;
    public int damageTaken = 0;
    public int damageDealt = 0;
    public int prayerDrain = 0;
    public boolean failed = false;
    public Instant startTime;
    public Instant stopTime;
    public Duration splitTime;
    public Duration predictedTime;
    public Duration waveEndSplit;
    public boolean forceReset;
    public int idleTicks = 0;

    private static final List<Integer> SPLIT_WAVES = new ArrayList<Integer>() {{
        add(9);
        add(18);
        add(25);
        add(35);
        add(42);
        add(50);
        add(57);
        add(60);
        add(63);
        add(66);
        add(67);
        add(68);
        add(69);
    }};

    public Wave(int id, Duration splitTime)
    {
        this.id = id;
        this.NPCSpawns = new ArrayList<>();
        this.startTime = Instant.now();
        this.splitTime = splitTime;
        this.predictedTime = null;
    }

    public void AddSpawn(WorldPoint tile, NPC npc)
    {
        NPCSpawns.add(new Spawn(tile, npc));
    }

    public void ReinitWave()
    {
        this.startTime = this.startTime.plus(
                Duration.between(this.stopTime, Instant.now())
        );

        this.stopTime = null;
        this.forceReset = false;
    }

    public void Finished(Duration waveEndSplit, boolean failed)
    {
        this.waveEndSplit = waveEndSplit;
        this.failed = failed;
        stopTime = Instant.now();
    }

    public void Pause()
    {
        this.forceReset = true;
        this.stopTime = Instant.now();
    }

    public boolean IsSplit()
    {
        return SPLIT_WAVES.contains(id);
    }

    public String SplitTimeString()
    {
        return formatTime(splitTime.toMillis());
    }

    public String PredictedTimeString()
    {
        return formatTime(predictedTime.toMillis());
    }

    public long WaveTime()
    {
        if (stopTime == null)
        {
            return Duration.between(startTime, Instant.now()).toMillis();
        }
        else
        {
            return Duration.between(startTime, stopTime).toMillis();
        }
    }

    public String WaveTimeString()
    {
        return formatTime(WaveTime());
    }

    public long CurrentTime()
    {
        if (stopTime == null)
        {
            return Duration.between(startTime, Instant.now()).plus(splitTime).toMillis();
        }
        else
        {
            return Duration.between(startTime, stopTime).plus(splitTime).toMillis();
        }
    }

    public String CurrentTimeString()
    {
        return formatTime(CurrentTime());
    }

    private String formatTime(long msTime)
    {
        long seconds = Duration.ofMillis(msTime).getSeconds();
        return seconds / 60 + ":" + String.format("%02d", seconds % 60);
    }

    public Map<String, ArrayList<ArrayList<Integer>>> RebasedNPCs()
    {
        Map<String, ArrayList<ArrayList<Integer>>> mobs = new HashMap<>();

        for (Spawn spawn : NPCSpawns)
        {
            // The SW-most corner region tile is 17,17
            // The SW-most corner website tile is 0, 29
            final int xOffset = 17;

            // The NW-most corner region tile is 17,46
            // The NW-most corner website tile is 0, 0
            final int yOffset = 46;

            final int x = spawn.tile.getRegionX()-xOffset;
            final int y = yOffset-spawn.tile.getRegionY();

            mobs.computeIfAbsent(spawn.name, k -> new ArrayList<ArrayList<Integer>>());
            mobs.get(spawn.name).add(new ArrayList<Integer>(){{add(x); add(y);}});
        }

        return mobs;
    }

    public String SerializeWave()
    {
        Map<String, ArrayList<ArrayList<Integer>>> mobs = RebasedNPCs();

        // Map in-game names to website parameter names
        Map<String, String> npc_names = new HashMap<String, String>() {{
            put("Jal-MejRah", "bat");
            put("Jal-Ak", "blob");
            put("Jal-ImKot", "melee");
            put("Jal-Xil", "ranger");
            put("Jal-Zek", "mager");
        }};

        StringBuilder sb = new StringBuilder();

        sb.append("https://www.infernotrainer.com/?");
        for (Map.Entry<String, ArrayList<ArrayList<Integer>>> entry : mobs.entrySet())
        {
            sb.append(npc_names.get(entry.getKey()));
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("&");
        }
        sb.append("copyable");
        return sb.toString().replaceAll("\\s", "");
    }
}
