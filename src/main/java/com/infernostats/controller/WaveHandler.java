package com.infernostats.controller;

import com.infernostats.InfernoStatsConfig;
import com.infernostats.InfernoStatsPlugin;
import com.infernostats.events.WaveFinishedEvent;
import com.infernostats.events.WaveStartedEvent;
import com.infernostats.model.Wave;
import com.infernostats.model.WaveState;
import com.infernostats.view.TimeFormatting;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class WaveHandler {
	@Getter
	private Wave wave;
	private final ArrayList<Wave> waves;

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	private final InfernoStatsPlugin plugin;
	private final InfernoStatsConfig config;

	@Inject
	protected WaveHandler(InfernoStatsPlugin plugin, InfernoStatsConfig config) {
		this.plugin = plugin;
		this.config = config;
		this.wave = null;
		this.waves = new ArrayList<>();
	}

	@Subscribe
	protected void onGameTick(GameTick e) {
		if (!this.plugin.isInInferno() || this.wave == null)
			return;

		if (this.wave.getState() != WaveState.STARTED)
			return;

		if (this.plugin.getTimerState() == TimerHandler.TimerState.RUNNING)
			this.wave.setDuration(this.wave.getDuration() + 1);
	}

	@Subscribe
	protected void onWaveStartedEvent(WaveStartedEvent e) {
		this.wave = e.getWave();
		this.waves.add(this.wave);
	}

	@Subscribe
	protected void onWaveFinishedEvent(WaveFinishedEvent e) {
		this.wave.setState(e.getState());

		if (isRunCompleted(this.wave.getId(), e.getState())) {
			Player player = client.getLocalPlayer();
			if (player == null)
				return;

			if (config.saveWaveTimes()) {
				toFile(player.getName(), csvName(false), toCSV(false));
			}

			if (config.saveSplitTimes()) {
				toFile(player.getName(), csvName(true), toCSV(true));
			}

			waves.clear();
		}
	}

	@Subscribe
	protected void onNpcSpawned(NpcSpawned e) {
		final NPC npc = e.getNpc();
		final Actor npcActor = e.getActor();
		final WorldPoint spawnTile = npcActor.getWorldLocation();
		final int npcId = npc.getId();

		if (!this.plugin.isInInferno() || this.wave == null)
			return;

		// ROCKY_SUPPORT is the normal pillar id; ROCKY_SUPPORT_7710 spawns as a pillar falls
		if (npcId == NpcID.ROCKY_SUPPORT || npcId == NpcID.ROCKY_SUPPORT_7710)
			return;

		// We'll ignore nibblers and jads, and zuk spawns off the map
		if (npcId == NpcID.JALNIB || npcId == NpcID.JALTOKJAD || npcId == NpcID.TZKALZUK)
			return;

		// We only want the original wave spawn, not minions or mager respawns
		if (wave.duration().compareTo(Duration.ofSeconds(1)) > 0)
			return;

		wave.addNPC(npc.getName(), spawnTile);
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event) {
		Actor target = event.getActor();
		Hitsplat hitsplat = event.getHitsplat();

		if (!this.plugin.isInInferno() || this.wave == null)
			return;

		if (!hitsplat.isMine())
			return;

		if (target == this.client.getLocalPlayer()) {
			if (hitsplat.getHitsplatType() == Hitsplat.HitsplatType.DAMAGE_ME)
				this.wave.setDamageTaken(this.wave.getDamageTaken() + hitsplat.getAmount());
		} else {
			if (hitsplat.getHitsplatType() == Hitsplat.HitsplatType.DAMAGE_ME)
				this.wave.setDamageDealt(this.wave.getDamageDealt() + hitsplat.getAmount());
		}
	}

	private void toFile(String username, String filename, String contents) {
		try {
			Path path = Files.createDirectories(Paths.get(RUNELITE_DIR.getPath(), "inferno-stats", username));
			Files.write(path.resolve(filename), contents.getBytes());
		} catch (IOException ex) {
			log.debug("Error writing file: {}", ex.getMessage());
		}
	}

	public String toCSV(boolean splitWaves) {
		StringBuilder csv = new StringBuilder();

		if (splitWaves) {
			csv.append("wave,split,time,delta\n");

			Wave prev = null;
			for (Wave wave : waves) {
				if (!wave.isSplit())
					continue;

				csv.append(wave.getId())
						.append(",")
						.append(TimeFormatting.getSplitTime(wave))
						.append(",")
						.append(TimeFormatting.getCurrentWaveTime(wave))
						.append(",")
						.append(TimeFormatting.getSplitDelta(wave, prev))
						.append("\n");

				prev = wave;
			}
		} else {
			csv.append("wave,split,time\n");

			for (Wave wave : waves) {
				csv.append(wave.getId())
						.append(",")
						.append(TimeFormatting.getSplitTime(wave))
						.append(",")
						.append(TimeFormatting.getCurrentWaveTime(wave))
						.append("\n");
			}
		}

		return csv.toString();
	}

	public String csvName(boolean splitWaves) {
		String wavesText = splitWaves ? "Splits.csv" : "Full.csv";

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		String timeText = formatter.format(LocalDateTime.now());

		switch (this.wave.getState()) {
			case FINISHED:
				return timeText + " Successful KC on Wave " + this.wave.getId() + ", " + wavesText;
			case FAILED:
			default:
				return timeText + " Failed KC on Wave " + this.wave.getId() + ", " + wavesText;
		}
	}

	private boolean isRunCompleted(final int waveId, WaveState state) {
		return waveId == 69 && state == WaveState.FINISHED || state == WaveState.FAILED;
	}
}
