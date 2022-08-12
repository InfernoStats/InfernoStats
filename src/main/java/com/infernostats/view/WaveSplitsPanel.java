package com.infernostats.view;

import com.infernostats.controller.WaveHandler;
import com.infernostats.model.Wave;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WaveSplitsPanel extends JPanel {
	private final JLabel titleLabel = new JLabel();

	@Setter
	private WaveHandler waveHandler = null;

	@Inject
	public WaveSplitsPanel() {
		setBorder(new EmptyBorder(2, 8, 2, 8));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);

		titleLabel.setText("Copy Wave Splits to Clipboard");
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		titleLabel.setForeground(Color.WHITE);
		add(titleLabel);

		MouseAdapter waveStatsMouseListener = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setUnderline(titleLabel);
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				unsetUnderline(titleLabel);
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					return;
				}

				Toolkit.getDefaultToolkit()
						.getSystemClipboard()
						.setContents(
								new StringSelection(getWaveData()),
								null
						);
			}
		};

		addMouseListener(waveStatsMouseListener);
	}

	private void setUnderline(JLabel label) {
		Font font = label.getFont();
		Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		label.setFont(font.deriveFont(attributes));
	}

	private void unsetUnderline(JLabel label) {
		Font font = label.getFont();
		Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
		attributes.put(TextAttribute.UNDERLINE, -1);
		label.setFont(font.deriveFont(attributes));
	}

	private String getWaveData() {
		return waveHandler.toText(true);
	}
}