package com.infernostats.view;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;

public class WaveListContainer extends JScrollPane {
	public WaveListContainer(WaveListPanel waveListPanel) {
		super(waveListPanel);

		this.setBackground(ColorScheme.DARK_GRAY_COLOR);
		this.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
	}
}
