package com.infernostats;

import com.infernostats.model.Wave;
import com.infernostats.view.*;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;

@Getter(AccessLevel.PACKAGE)
public class InfernoStatsPanel extends PluginPanel {
	private WaveStatsPanel waveStatsPanel;

	private final TitlePanel titlePanel = new TitlePanel();
	private final WaveSplitsPanel waveSplitsPanel = new WaveSplitsPanel();
	private final WaveListPanel waveListPanel = new WaveListPanel();
	private final WaveListContainer waveListContainer = new WaveListContainer(waveListPanel);

	private final InfernoStatsPlugin plugin;
	private final InfernoStatsConfig config;

	@Inject
	private InfernoStatsPanel(InfernoStatsPlugin plugin, InfernoStatsConfig config) {
		this.plugin = plugin;
		this.config = config;

		setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(this.titlePanel, BorderLayout.NORTH, 0);
		add(this.waveSplitsPanel, BorderLayout.CENTER, 1);
		add(this.waveListContainer, BorderLayout.SOUTH, 2);

		waveSplitsPanel.setWaveHandler(this.plugin.getWaveHandler());
	}

	void AddWave(Wave wave) {
		SwingUtilities.invokeLater(() ->
		{
			waveStatsPanel = new WaveStatsPanel(config, wave);
			waveListPanel.add(waveStatsPanel, 0);

			updateUI();
		});
	}

	void UpdateWave() {
		if (waveStatsPanel == null)
			return;

		SwingUtilities.invokeLater(() -> waveStatsPanel.update());
	}

	public void ClearWaves() {
		waveListPanel.removeAll();
		waveListPanel.validate();
		waveListPanel.repaint();
	}
}
