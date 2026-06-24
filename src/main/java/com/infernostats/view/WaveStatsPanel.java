package com.infernostats.view;

import com.infernostats.InfernoStatsConfig;
import com.infernostats.model.InfernoNpc;
import com.infernostats.model.WaveNpc;
import com.infernostats.model.Wave;
import com.infernostats.model.WaveSpawn;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.LinkBrowser;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Setter
public class WaveStatsPanel extends JPanel {
	private Wave wave;
	private String baseURL;

	private String waveURL;
	private JLabel waveNumber;
	private JLabel duration;
	private JLabel damageTaken;
	private JLabel damageDealt;
	private JLabel prayerDrain;
	private JLabel idleTicks;
	private WaveImage imageLabel;
	private JPanel textPanel;

	private InfernoStatsConfig config;

	private static final Border normalBorder = BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 4, 0, ColorScheme.DARK_GRAY_COLOR),
			new EmptyBorder(4, 6, 4, 6));

	private static final Border hoverBorder = BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 4, 0, ColorScheme.DARK_GRAY_COLOR),
			BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR),
					new EmptyBorder(3, 5, 3, 5)));

	public WaveStatsPanel(InfernoStatsConfig config, Wave wave) {
		this.config = config;

		this.wave = wave;
		this.baseURL = config.url().base;
		this.waveURL = generateURL();

		setLayout(new BorderLayout(5, 5));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(normalBorder);

		JPanel wavePanel = new JPanel();
		wavePanel.setLayout(new BoxLayout(wavePanel, BoxLayout.Y_AXIS));
		wavePanel.setBackground(null);

		JPanel wavePanelTop = new JPanel();
		wavePanelTop.setLayout(new BoxLayout(wavePanelTop, BoxLayout.Y_AXIS));
		wavePanelTop.setBackground(null);

		JPanel waveNumberLine = new JPanel();
		waveNumberLine.setLayout(new BorderLayout());
		waveNumberLine.setBackground(null);

		waveNumber = new JLabel();
		if (wave.isSplit()) {
			waveNumber.setText("Wave " + wave.getId() + " - " + TimeFormatting.getSplitTime(wave));
		} else {
			waveNumber.setText("Wave " + wave.getId());
		}
		waveNumber.setHorizontalAlignment(SwingConstants.CENTER);
		waveNumber.setForeground(Color.WHITE);
		waveNumberLine.add(waveNumber);

		JPanel wavePanelBottom = new JPanel();
		wavePanelBottom.setLayout(new BoxLayout(wavePanelBottom, BoxLayout.X_AXIS));
		wavePanelBottom.setBackground(null);

		textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setBackground(null);

		JPanel durationLine = new JPanel();
		durationLine.setLayout(new BorderLayout());
		durationLine.setBackground(null);

		duration = new JLabel();
		duration.setText("Time: 00:00");
		duration.setForeground(Color.WHITE);
		durationLine.add(duration, BorderLayout.WEST);

		JPanel damageTakenLine = new JPanel();
		damageTakenLine.setLayout(new BorderLayout());
		damageTakenLine.setBackground(null);

		damageTaken = new JLabel();
		damageTaken.setText("Damage Taken: 0");
		damageTaken.setForeground(Color.WHITE);
		damageTakenLine.add(damageTaken, BorderLayout.WEST);

		JPanel damageDealtLine = new JPanel();
		damageDealtLine.setLayout(new BorderLayout());
		damageDealtLine.setBackground(null);

		damageDealt = new JLabel();
		damageDealt.setText("Damage Dealt: 0");
		damageDealt.setForeground(Color.WHITE);
		damageDealtLine.add(damageDealt, BorderLayout.WEST);

		JPanel prayerDrainLine = new JPanel();
		prayerDrainLine.setLayout(new BorderLayout());
		prayerDrainLine.setBackground(null);

		prayerDrain = new JLabel();
		prayerDrain.setText("Prayer Drain: 0");
		prayerDrain.setForeground(Color.WHITE);
		prayerDrainLine.add(prayerDrain, BorderLayout.WEST);

		JPanel idleTicksLine = new JPanel();
		idleTicksLine.setLayout(new BorderLayout());
		idleTicksLine.setBackground(null);

		idleTicks = new JLabel();
		idleTicks.setText("Idle Ticks: 0");
		idleTicks.setForeground(Color.WHITE);
		idleTicksLine.add(idleTicks, BorderLayout.WEST);

		textPanel.add(durationLine);
		textPanel.add(damageTakenLine);
		textPanel.add(damageDealtLine);
		textPanel.add(prayerDrainLine);

		if (config.showIdleTicksInSidePanel())
			textPanel.add(idleTicksLine);

		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));
		imagePanel.setBackground(null);

		imageLabel = new WaveImage();
		imagePanel.add(imageLabel);

		wavePanelTop.add(waveNumberLine);
		wavePanelBottom.add(textPanel);
		wavePanelBottom.add(imagePanel);

		wavePanel.add(wavePanelTop, BorderLayout.NORTH);
		wavePanel.add(wavePanelBottom, BorderLayout.SOUTH);

		add(wavePanel, BorderLayout.NORTH);

		MouseAdapter waveStatsMouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setBackground(ColorScheme.DARK_GRAY_COLOR);
				for (Component c : getComponents()) {
					c.setBackground(ColorScheme.DARK_GRAY_COLOR);
				}
				setBorder(hoverBorder);
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setBackground(ColorScheme.DARKER_GRAY_COLOR);
				for (Component c : getComponents()) {
					c.setBackground(ColorScheme.DARKER_GRAY_COLOR);
				}
				setBorder(normalBorder);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					return;
				}

				LinkBrowser.browse(waveURL);
			}
		};

		addMouseListener(waveStatsMouseListener);
	}

	public void RedrawWaveSpawn() {
		for (WaveSpawn spawn : getWaveSpawns()) {
			imageLabel.drawSpawn(spawn.getX(), spawn.getY(), spawn.getSize(), spawn.getColor());
		}

		imageLabel.revalidate();
		imageLabel.repaint();
	}

	public String generateURL() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.baseURL);

		for (InfernoNpc type : InfernoNpc.values()) {
			List<WaveSpawn> spawns = getWaveSpawns().stream()
				.filter(s -> s.getColor().equals(type.color))
				.collect(java.util.stream.Collectors.toList());

			if (spawns.isEmpty())
				continue;

			List<List<Integer>> tiles = spawns.stream()
				.map(s -> java.util.Arrays.asList(s.getX(), s.getY()))
				.collect(java.util.stream.Collectors.toList());

			sb.append(type.urlParam).append("=").append(tiles).append("&");
		}

		sb.append("copyable");
		return sb.toString().replaceAll("\\s", "");
	}

	private List<WaveSpawn> getWaveSpawns() {
		// The SW-most corner region tile is 17,17 -> website tile 0,29
		// The NW-most corner region tile is 17,46 -> website tile 0,0
		final int xOffset = 17;
		final int yOffset = 46;

		List<WaveSpawn> spawns = new ArrayList<>();
		for (WaveNpc npc : wave.getWaveNpcs()) {
			final InfernoNpc type = npc.getType();
			final int x = npc.getSpawn().getX() - xOffset;
			final int y = yOffset - npc.getSpawn().getY();
			spawns.add(new WaveSpawn(x, y, type.size, type.color));
		}

		return spawns;
	}

	public void update() {
		switch (this.getWave().getState()) {
			case FINISHED:
			case STARTED:
				this.getWaveNumber().setForeground(Color.WHITE);
				break;
			case SUCCESS:
				this.getWaveNumber().setForeground(Color.GREEN);
				break;
			case FAILED:
				this.getWaveNumber().setForeground(Color.RED);
				break;
		}

		this.waveURL = this.generateURL();
		this.duration.setText("Time: " + TimeFormatting.getCurrentWaveTime(wave));
		this.damageTaken.setText("Damage Taken: " + this.wave.getDamageTaken());
		this.damageDealt.setText("Damage Dealt: " + this.wave.getDamageDealt());
		this.prayerDrain.setText("Prayer Drain: " + this.wave.getPrayerDrain());
		this.idleTicks.setText("Idle Ticks: " + this.wave.getIdleTicks());
		this.RedrawWaveSpawn();
	}
}
