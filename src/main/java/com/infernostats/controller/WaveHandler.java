package com.infernostats.controller;

import com.infernostats.InfernoStatsConfig;
import com.infernostats.InfernoStatsPlugin;
import com.infernostats.events.WaveFinishedEvent;
import com.infernostats.events.WaveStartedEvent;
import com.infernostats.model.Wave;
import com.infernostats.model.Location;
import com.infernostats.model.WaveState;
import com.infernostats.view.TimeFormatting;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.StatChanged;
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

import static net.runelite.api.Skill.PRAYER;
import static net.runelite.client.RuneLite.RUNELITE_DIR;

@Slf4j
public class WaveHandler {
	@Getter
	private Wave wave;

	@Getter
	@Setter
	private Location location;

	@Getter
	private final ArrayList<Wave> waves;

	private int prayer;

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
		if ((!this.plugin.isInInferno() && !this.plugin.isInFightCaves()) || this.wave == null)
			return;

		if (this.wave.getState() != WaveState.STARTED)
			return;

		if (this.plugin.getTimerState() == TimerHandler.TimerState.RUNNING)
			this.wave.setDuration(this.wave.getDuration() + 1);

		prayer = client.getBoostedSkillLevel(PRAYER);
	}

	@Subscribe
	public void onStatChanged(StatChanged event) {
		if ((!this.plugin.isInInferno() && !this.plugin.isInFightCaves()) || this.wave == null)
			return;

		if (this.wave.getState() != WaveState.STARTED)
			return;

		if (event.getSkill() != PRAYER)
			return;

		if (event.getBoostedLevel() == prayer - 1)
			wave.setPrayerDrain(wave.getPrayerDrain() + 1);
	}

	@Subscribe(priority = 1)
	protected void onWaveStartedEvent(WaveStartedEvent e) {
		if (this.plugin.isInInferno())
			setLocation(Location.INFERNO);
		else if (this.plugin.isInFightCaves())
			setLocation(Location.FIGHT_CAVES);

		this.wave = e.getWave();
		if (this.wave.getId() == 1)
			waves.clear();
		this.waves.add(this.wave);
	}

	@Subscribe(priority = 1)
	protected void onWaveFinishedEvent(WaveFinishedEvent e) {
		this.wave.setState(e.getState());
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

		if ((!this.plugin.isInInferno() && !this.plugin.isInFightCaves()) || this.wave == null)
			return;

		if (!hitsplat.isMine())
			return;

		if (target == this.client.getLocalPlayer()) {
			this.wave.setDamageTaken(this.wave.getDamageTaken() + hitsplat.getAmount());
		} else {
			this.wave.setDamageDealt(this.wave.getDamageDealt() + hitsplat.getAmount());
		}
	}

	public void WriteWaves()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
			return;

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
		String time = formatter.format(LocalDateTime.now());

		if (config.saveWaveTimes()) {
			switch (config.splitsFileType())
			{
				case TEXT:
					toFile(player.getName(), fileName(time, false), toText(false));
					break;
				case CSV:
					toFile(player.getName(), fileName(time, false), toCSV(false));
					break;
			}
		}

		if (config.saveSplitTimes()) {
			switch (config.splitsFileType())
			{
				case TEXT:
					toFile(player.getName(), fileName(time, true), toText(true));
					break;
				case CSV:
					toFile(player.getName(), fileName(time, true), toCSV(true));
					break;
			}
		}
	}

	private void toFile(String username, String filename, String contents) {
		try {
			Path path = null;
			switch (this.wave.getLocation())
			{
				case FIGHT_CAVES:
					path = Files.createDirectories(Paths.get(
						RUNELITE_DIR.getPath(), "inferno-stats", username, "fight-caves"));
					break;
				case INFERNO:
					path = Files.createDirectories(Paths.get(
						RUNELITE_DIR.getPath(), "inferno-stats", username, "inferno"));
					break;
			}
			Files.write(path.resolve(filename), contents.getBytes());
		} catch (IOException ex) {
			log.debug("Error writing file: {}", ex.getMessage());
		}
	}

	private String toText(boolean splitWaves) {
		StringBuilder text = new StringBuilder();
		ArrayList<Wave> waves = this.getWaves();

		if (splitWaves) // only record the important splits
		{
			Wave prev = null;
			for (Wave wave : waves) {
				if (!wave.isSplit())
					continue;

				text.append("Wave: ").append(wave.getId())
						.append(", ")
						.append("Split: ").append(TimeFormatting.getSplitTime(wave))
						.append(" ")
						.append("(+").append(TimeFormatting.getSplitDelta(wave, prev)).append(")")
						.append(idleTicksToText(true, wave))
						.append(damageTakenToText(true, wave))
						.append("\n");

				prev = wave;
			}
		}
		else // record every single wave
		{
			for (Wave wave : waves) {
				text.append("Wave: ").append(wave.getId())
						.append(", ")
						.append("Split: ").append(TimeFormatting.getSplitTime(wave))
						.append(idleTicksToText(false, wave))
						.append(damageTakenToText(false, wave))
						.append("\n");
			}
		}

		if (waves.isEmpty()) {
			text.append("Duration (Not Started): N/a");
			return text.toString();
		}

		String duration = TimeFormatting.getCurrentTotalTime(wave);
		switch (wave.getState()) {
			case FINISHED:
			case STARTED:
				text.append("Duration (Unfinished): ").append(duration);
				break;
			case SUCCESS:
				text.append("Duration (Success): ").append(duration);
				break;
			case FAILED:
				text.append("Duration (Failed): ").append(duration);
				break;
		}

		if (config.showIdleTicksInSplitsFile())
		{
			text.append(", Total Idle Ticks: ").append(SOMETHING_HERE);
		}

		if (config.showDamageTakenInSplitsFile())
		{
			text.append(", Total Damage Taken: ").append(SOMETHING_HERE);
		}

		return text.toString();

		private String idleTicksToText(boolean isSplitWavesEnabled, Wave wave) {
			/*
			 * Wave: 9, Split: 02:58 (+02:58), Idle: 28 (+28)
			 * Wave: 18, Split: 07:36 (+04:37), Idle: 71 (+43)
			 */
			if (!config.showIdleTicksInSplitsFile()) return "" // return nothing if the user doesnt want idle ticks to show up in splits file

			StringBuilder text = new StringBuilder();
			text.append(", ")
					.append("Idle: ")
					.append(SOMETHING_HERE); // total idle ticks so far

			if (isSplitWavesEnabled)
			{
				text.append(" ")
						.append("(+")
						.append(SOMETHING_HERE) // delta idle ticks (idle ticks just for the current split)
						.append(")");
			}

			return text.toString();
		}

		private String damageTakenToText(boolean isSplitWavesEnabled, Wave wave) {
			/*
			 * Wave: 9, Split: 02:58 (+02:58), Damage Taken: 23 (+23)
			 * Wave 18: Split: 07:36 (+04:37), Damage Taken: 41 (+18)
			 */
			if (!config.showDamageTakenInSplitsFile()) return "" // return nothing if the user doesnt want damage taken to show up in splits file

			StringBuilder text = new StringBuilder();
			text.append(", ").append("Damage Taken: ").append(SOMETHING_HERE); // total damage taken so far

			if (isSplitWavesEnabled)
			{
				text.append(" ")
					.append("(+")
					.append(SOMETHING_HERE) // delta damage taken (damage taken just for the current split)
					.append(")");
			}
		}
	}

	private String toCSV(boolean splitWaves) {
		StringBuilder csv = new StringBuilder();

		if (splitWaves) {
			// Header
			csv.append("wave,split,time,delta");
			if (config.showIdleTicksInSplitsFile())
				csv.append(",idle,deltaidle");
			if (config.showDamageTakenInSplitsFile())
				csv.append(",damagetaken,deltadamagetaken");
			csv.append("\n")

			// Content
			Wave prev = null;
			for (Wave wave : waves) {
				if (!wave.isSplit())
					continue;

				csv.append(wave.getId())
						.append(",")
						.append(TimeFormatting.getSplitTimeCSV(wave))
						.append(",")
						.append(TimeFormatting.getCurrentWaveTimeCSV(wave))
						.append(",")
						.append(TimeFormatting.getSplitDeltaCSV(wave, prev))
						.append(",")
						//.append(wave.getIdleTicks())
						.append(idleTicksToCSV(true, wave))
						.append(damageTakenToCSV(true, wave))
						.append("\n");

				prev = wave;
			}
		} else {
			// Header
			csv.append("wave,split,time");
			if (config.showIdleTicksInSplitsFile())
				csv.append(",idle");
			if (config.showDamageTakenInSplitsFile())
				csv.append(",damagetaken");
			csv.append("\n");

			// Content
			for (Wave wave : waves) {
				csv.append(wave.getId())
						.append(",")
						.append(TimeFormatting.getSplitTimeCSV(wave))
						.append(",")
						.append(TimeFormatting.getCurrentWaveTimeCSV(wave))
						.append(",")
						//.append(wave.getIdleTicks())
						.append(idleTicksToCSV(false, wave))
						.append(damageTakenToCSV(false, wave))
						.append("\n");
			}
		}

		return csv.toString();

		private String idleTicksToCSV(Boolean deltaTimesEnabled, Wave wave) {
			if (!config.showIdleTicksInSplitsFile()) return "" // return nothing if the user doesnt want idle ticks to show up in splits file

			StringBuilder text = new StringBuilder();
			text.append(",").append(SOMETHING_HERE); // total idle ticks so far

			if (deltaTimesEnabled)
			{
				text.append(",").append(SOMETHING_HERE); // delta idle ticks (idle ticks just for the current split)
			}

			return text.toString();
		}

		private String damageTakenToCSV(Boolean deltaTimesEnabled, Wave wave) {
			if (!config.showDamageTakenInSplitsFile()) return "" // return nothing if the user doesnt want damage taken to show up in splits file

			StringBuilder text = new StringBuilder();
			text.append(",").append(SOMETHING_HERE); // total damage taken so far

			if (deltaTimesEnabled)
			{
				text.append(",").append(SOMETHING_HERE); // delta damage taken (damage taken just for the current split)
			}

			return text.toString();
		}
	}

	private String fileName(String time, boolean splitWaves) {
		String wavesText = (splitWaves ? "Splits" : "Full") + getExtension();

		switch (wave.getState()) {
			case SUCCESS:
				return time + " Successful KC on Wave " + wave.getId() + ", " + wavesText;
			case FAILED:
			default:
				return time + " Failed KC on Wave " + wave.getId() + ", " + wavesText;
		}
	}

	private String getExtension()
	{
		switch (config.splitsFileType())
		{
			case CSV:
				return ".csv";
			default:
			case TEXT:
				return ".txt";
		}
	}
}
