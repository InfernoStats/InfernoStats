package com.infernostats;

import com.infernostats.controller.TimerHandler;
import lombok.AllArgsConstructor;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(InfernoStatsConfig.GROUP)
public interface InfernoStatsConfig extends Config {
	String GROUP = "InfernoConfig";

	@ConfigSection(
			name = "Overlays",
			description = "Overlays Section",
			position = 0,
			closedByDefault = true
	)
	String overlays = "overlays";

	@ConfigItem(
			position = 0,
			keyName = "showStatsOverlay",
			name = "Splits Overlay",
			description = "Displays an overlay with the wave number and splits/predicted time.",
			section = overlays
	)
	default boolean splitsOverlay() {
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "showInfernoTimer",
			name = "Inferno Timer",
			description = "Display elapsed time in the Inferno",
			section = overlays
	)
	default boolean showInfernoTimer() {
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "showFightCavesTimer",
			name = "Fight Caves Timer",
			description = "Display elapsed time in the Fight Caves",
			section = overlays
	)
	default boolean showFightCavesTimer() {
		return true;
	}

	@ConfigItem(
			keyName = "tzhaarDuration",
			name = "",
			description = "",
			section = overlays,
			hidden = true
	)
	long tzhaarDuration();

	@ConfigItem(
			keyName = "tzhaarDuration",
			name = "",
			description = "",
			section = overlays
	)
	void tzhaarDuration(long tzhaarDuration);

	@ConfigItem(
			keyName = "tzhaarTimerState",
			name = "",
			description = "",
			section = overlays,
			hidden = true
	)
	TimerHandler.TimerState tzhaarTimerState();

	@ConfigItem(
			keyName = "tzhaarTimerState",
			name = "",
			description = "",
			section = overlays
	)
	void tzhaarTimerState(TimerHandler.TimerState tzhaarTimerState);

	@ConfigSection(
			name = "Wave and Split Times",
			description = "Wave and Split Times Section",
			position = 1,
			closedByDefault = true
	)
	String waveAndSplitTimes = "waveAndSplitTimes";

	@ConfigItem(
			position = 0,
			keyName = "showWaveTimes",
			name = "Wave Times",
			description = "Displays a chat message of the time taken to complete the previous wave",
			section = waveAndSplitTimes
	)
	default boolean waveTimes() {
		return false;
	}

	@ConfigItem(
			position = 1,
			keyName = "showSplitTimes",
			name = "Split Times",
			description = "Displays a chat message of the total time taken to reach the current wave",
			section = waveAndSplitTimes
	)
	default boolean splitTimes() {
		return true;
	}

	@ConfigItem(
			position = 2,
			keyName = "predictedCompletionTime",
			name = "Predicted Time",
			description = "Show predicted completion time",
			section = waveAndSplitTimes
	)
	default boolean predictedCompletionTime() {
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "saveWaveTimes",
			name = "Save Wave Times",
			description = "Saves the time of every wave after a run",
			section = waveAndSplitTimes
	)
	default boolean saveWaveTimes() {
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "saveSplitTimes",
			name = "Save Split Times",
			description = "Saves the time of every split after a run",
			section = waveAndSplitTimes
	)
	default boolean saveSplitTimes() {
		return true;
	}

	@ConfigSection(
			name = "Target Split Times",
			description = "Split times that you wish to achieve",
			position = 2,
			closedByDefault = true
	)
	String targetSplitTimes = "targetSplitTimes";

	@ConfigItem(
			position = 0,
			keyName = "targetSplitTimes",
			name = "Target Split Times",
			description = "Enable Target Split Times",
			section = targetSplitTimes
	)
	default boolean showTargetSplitTimes() {
		return false;
	}

	@ConfigItem(
			position = 1,
			keyName = "targetWave9Split",
			name = "Wave 9",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave9Split() {
		return "2:55";
	}

	@ConfigItem(
			position = 2,
			keyName = "targetWave18Split",
			name = "Wave 18",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave18Split() {
		return "7:15";
	}

	@ConfigItem(
			position = 3,
			keyName = "targetWave25Split",
			name = "Wave 25",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave25Split() {
		return "11:05";
	}

	@ConfigItem(
			position = 4,
			keyName = "targetWave35Split",
			name = "Wave 35",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave35Split() {
		return "17:58";
	}

	@ConfigItem(
			position = 5,
			keyName = "targetWave42Split",
			name = "Wave 42",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave42Split() {
		return "23:13";
	}

	@ConfigItem(
			position = 6,
			keyName = "targetWave50Split",
			name = "Wave 50",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave50Split() {
		return "30:50";
	}

	@ConfigItem(
			position = 7,
			keyName = "targetWave57Split",
			name = "Wave 57",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave57Split() {
		return "38:06";
	}

	@ConfigItem(
			position = 8,
			keyName = "targetWave60Split",
			name = "Wave 60",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave60Split() {
		return "41:29";
	}

	@ConfigItem(
			position = 9,
			keyName = "targetWave63Split",
			name = "Wave 63",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave63Split() {
		return "44:59";
	}

	@ConfigItem(
			position = 10,
			keyName = "targetWave66Split",
			name = "Wave 66",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave66Split() {
		return "48:41";
	}

	@ConfigItem(
			position = 11,
			keyName = "targetWave67Split",
			name = "Wave 67",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave67Split() {
		return "49:27";
	}

	@ConfigItem(
			position = 12,
			keyName = "targetWave68Split",
			name = "Wave 68",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave68Split() {
		return "50:32";
	}

	@ConfigItem(
			position = 13,
			keyName = "targetWave69Split",
			name = "Wave 69",
			description = "Split Target",
			section = targetSplitTimes
	)
	default String targetWave69Split() {
		return "53:06";
	}

	@ConfigSection(
			name = "Idle Ticks",
			description = "Settings around tracking player idle ticks",
			position = 4,
			closedByDefault = true
	)
	String idleTicks = "idleTicks";

	@ConfigItem(
			keyName = "trackIdleTicks",
			name = "Track Idle Ticks",
			description = "Track idle ticks, where the player is not attacking a live NPC",
			position = 0,
			section = idleTicks
	)
	default boolean trackIdleTicks() {
		return false;
	}

	@ConfigItem(
			keyName = "showIdleTicksInSidePanel",
			name = "Show Idle Ticks in Side Panel",
			description = "Show idle ticks in the side panel",
			position = 1,
			section = idleTicks
	)
	default boolean showIdleTicksInSidePanel() {
		return true;
	}

	@ConfigItem(
			keyName = "showIdleTicksInChatbox",
			name = "Show Idle Ticks in Chatbox",
			description = "Show idle ticks in the chat box",
			position = 2,
			section = idleTicks
	)
	default boolean showIdleTicksInChatbox() {
		return false;
	}

	@AllArgsConstructor
	enum URL {
		LINE_OF_SIGHT("https://infernostats.github.io/inferno.html?"),
		TRAINER("https://www.infernotrainer.com/?");

		public final String base;
	}

	@ConfigItem(
			position = 5,
			keyName = "url",
			name = "Wave Tool",
			description = "Tool to use when opening wave spawns"
	)
	default URL url() {
		return URL.LINE_OF_SIGHT;
	}

	@ConfigItem(
			position = 6,
			keyName = "hide",
			name = "Hide when outside of the Inferno",
			description = "Don't show the button in the sidebar when you're not in the Inferno"
	)
	default boolean hide() {
		return false;
	}
}
