package com.infernostats.wavehistory;

import com.infernostats.InfernoStatsConfig;
import com.infernostats.InfernoStatsPlugin;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;


@Slf4j
@Singleton
public class WaveHistoryPanel extends PluginPanel {
    private final JPanel waveHistoryContainer = new JPanel();
    private final TotalStatsPanel totalStatsPanel;
    private final WaveSplitsPanel waveSplitsPanel;
    private ArrayList<WaveStatsPanel> waveStatsPanels = new ArrayList<>();

    private final InfernoStatsPlugin plugin;
    private final InfernoStatsConfig config;

    @Inject
    public WaveHistoryPanel(InfernoStatsPlugin plugin, InfernoStatsConfig config, WaveHistory waveHistory)
    {
        this.plugin = plugin;
        this.config = config;

        totalStatsPanel = new TotalStatsPanel();
        waveSplitsPanel = new WaveSplitsPanel(waveHistory);

        setLayout(new BorderLayout(0, 4));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setBorder(new EmptyBorder(8, 8, 8, 8));
        JPanel mainContent = new JPanel(new BorderLayout());

        waveHistoryContainer.setSize(getSize());
        waveHistoryContainer.setLayout(new BoxLayout(waveHistoryContainer, BoxLayout.Y_AXIS));

        JScrollPane scrollableContainer = new JScrollPane(mainContent);
        scrollableContainer.setBackground(ColorScheme.DARK_GRAY_COLOR);
        scrollableContainer.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        add(totalStatsPanel, BorderLayout.NORTH, 0);
        add(waveSplitsPanel, BorderLayout.CENTER, 0);

        mainContent.add(waveHistoryContainer, BorderLayout.NORTH);
        add(scrollableContainer, BorderLayout.SOUTH);
    }

    public void addWave(Wave wave)
    {
        SwingUtilities.invokeLater(() ->
        {
            WaveStatsPanel waveStatsPanel = new WaveStatsPanel(config, wave);
            waveStatsPanels.add(waveStatsPanel);
            waveHistoryContainer.add(waveStatsPanel, 0);
            updateUI();
        });
    }

    public void updateWave(Wave wave)
    {
        SwingUtilities.invokeLater(() ->
        {
            for (WaveStatsPanel panel : waveStatsPanels)
            {
                if (panel.wave.id == wave.id)
                {
                    panel.serializedLineOfSight = wave.SerializeWave();
                    panel.waveNumber.setForeground(wave.failed ? Color.RED : Color.WHITE);
                    panel.duration.setText("Time: " + wave.WaveTimeString());
                    panel.damageTaken.setText("Damage Taken: " + wave.damageTaken);
                    panel.prayerDrain.setText("Prayer Drain: " + wave.prayerDrain);
                    panel.damageDealt.setText("Damage Dealt: " + wave.damageDealt);
                    panel.idleTicks.setText("Idle ticks: " + wave.idleTicks);
                    panel.RedrawWaveSpawn();
                }
            }
        });
    }

    public void ClearWaves()
    {
        waveHistoryContainer.removeAll();
        waveHistoryContainer.validate();
        waveHistoryContainer.repaint();
    }
}
