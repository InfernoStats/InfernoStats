package com.infernostats;

import com.infernostats.view.TimeFormatting;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import java.awt.*;
import java.awt.image.BufferedImage;

public class InfernoStatsTimer extends InfoBox {
	private final InfernoStatsPlugin plugin;

	public InfernoStatsTimer(BufferedImage image, InfernoStatsPlugin plugin) {
		super(image, plugin);

		this.plugin = plugin;
	}

	@Override
	public String getText() {
		return getTime();
	}

	@Override
	public String getTooltip() {
		return "Elapsed time: " + getTime();
	}

	@Override
	public Color getTextColor() {
		return Color.WHITE;
	}

	private String getTime() {
		return TimeFormatting.formatGameTicks(this.plugin.getRunDuration());
	}
}
