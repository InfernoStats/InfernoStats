package com.infernostats.model;

import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Constants;
import net.runelite.api.coords.WorldPoint;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Set;

@Getter
@Setter
public class Wave {
	private final int id;
	private ArrayList<NPC> npcs;
	private final long start;
	private long duration;
	private int damageTaken;
	private int damageDealt;
	private int prayerDrain;
	private int idleTicks;
	private WaveState state;
	private Duration pace;
	private Location location;

	private static final Set<Integer> SPLIT_WAVES = ImmutableSet.of(
			9, 18, 25, 35, 42, 50, 57, 60, 63, 66, 67, 68, 69
	);

	private static final Set<Integer> FIGHT_CAVE_SPLIT_WAVES = ImmutableSet.of(
			7, 15, 31, 46, 53, 61, 62, 63
	);

	public Wave(int id, long start) {
		this.id = id;
		this.start = start;
		this.duration = 0;
		this.damageTaken = 0;
		this.damageDealt = 0;
		this.prayerDrain = 0;
		this.idleTicks = 0;
		this.state = WaveState.STARTED;
		this.npcs = new ArrayList<>();
		this.pace = null;
	}

	public void addNPC(String name, WorldPoint spawnTile) {
		this.npcs.add(new NPC(name, spawnTile));
	}

	public boolean isSplit() {
		switch (location)
		{
			case FIGHT_CAVES:
				return FIGHT_CAVE_SPLIT_WAVES.contains(this.getId());
			default:
			case INFERNO:
				return SPLIT_WAVES.contains(this.getId());
		}
	}

	public Duration duration() {
		return Duration.ofMillis(this.duration * Constants.GAME_TICK_LENGTH);
	}

	public Duration start() {
		return Duration.ofMillis(this.start * Constants.GAME_TICK_LENGTH);
	}
}
