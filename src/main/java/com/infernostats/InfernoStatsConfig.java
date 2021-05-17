package com.infernostats;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("infernostats")
public interface InfernoStatsConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "effectiveRestoration",
			name = "Effective Restoration",
			description = "Calculates same-tick health and prayer loss into special attack"
	)
	default boolean effectiveRestoration()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "showWaveTimes",
			name = "Wave Times",
			description = "Displays a chat message of the time taken to complete the previous wave"
	)
	default boolean waveTimes()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "showSplitTimes",
			name = "Split Times",
			description = "Displays a chat message of the total time taken to reach the current wave"
	)
	default boolean splitTimes()
	{
		return true;
	}

	@ConfigItem(
			position = 3,
			keyName = "showWaveTimer",
			name = "Wave Timer",
			description = "Displays an infobox of the wave timer"
	)
	default boolean waveTimer()
	{
		return false;
	}

	@ConfigItem(
			position = 4,
			keyName = "showSpecialCounter",
			name = "Special Counter",
			description = "Displays an infobox of the restoration of special attacks"
	)
	default boolean specialCounter()
	{
		return true;
	}

	@ConfigItem(
			position = 5,
			keyName = "hide",
			name = "Hide when outside of the Inferno",
			description = "Don't show the button in the sidebar when you're not in the Inferno"
	)
	default boolean hide()
	{
		return true;
	}
}
