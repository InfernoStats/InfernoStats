package com.infernostats;

import com.infernostats.controller.TimerHandler;
import com.infernostats.model.Wave;
import com.infernostats.view.TimeFormatting;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;

public class InfernoStatsOverlay extends OverlayPanel {
	private Wave prevSplitWave;

	private final InfernoStatsPlugin plugin;
	private final InfernoStatsConfig config;

	@Inject
	private InfernoStatsOverlay(InfernoStatsPlugin plugin, InfernoStatsConfig config) {
		super(plugin);

		this.plugin = plugin;
		this.config = config;
		this.prevSplitWave = null;

		setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
		setPriority(OverlayPriority.MED);
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		if (!this.plugin.isInInferno())
			return null;

		panelComponent.getChildren().clear();

		Wave wave = this.plugin.getCurrentWave();
		if (wave == null)
			return null;

		if (this.plugin.getTimerState() != TimerHandler.TimerState.RUNNING)
			return null;

		if (wave.isSplit())
			prevSplitWave = wave;

		String header = "Current Wave: " + wave.getId();
		LinkedHashMap<String, String> contents = new LinkedHashMap<>();

		if (prevSplitWave != null) {
			if (config.splitTimes()) {
				contents.put("Wave " + prevSplitWave.getId() + " Split: ", TimeFormatting.getSplitTime(prevSplitWave));
			}

			if (config.predictedCompletionTime() && prevSplitWave.getPace() != null) {
				contents.put("Predicted Time: ", TimeFormatting.formatDuration(prevSplitWave.getPace()));
			}
		}

		panelComponent.getChildren().add(TitleComponent.builder()
				.text(header)
				.color(ColorScheme.BRAND_ORANGE)
				.build());

		panelComponent.setPreferredSize(new Dimension(getMaxWidth(graphics, contents, header) + 10, 0));

		for (Map.Entry<String, String> pair : contents.entrySet()) {
			panelComponent.getChildren().add(
					LineComponent
							.builder()
							.left(pair.getKey())
							.right(pair.getValue())
							.build());
		}

		return super.render(graphics);
	}

	private int getMaxWidth(Graphics2D graphics, HashMap<String, String> contents, String header) {
		if (contents.isEmpty())
			return graphics.getFontMetrics().stringWidth(header);

		Map.Entry<String, String> longestPair =
				Collections.max(contents.entrySet(), Comparator.comparingInt(this::keyValueLength));

		return graphics.getFontMetrics().stringWidth(longestPair.getKey()) +
				graphics.getFontMetrics().stringWidth(longestPair.getValue());
	}

	private int keyValueLength(Map.Entry<String, String> entry) {
		return entry.getKey().length() + entry.getValue().length();
	}

	public void reset() {
		this.prevSplitWave = null;
	}
}
