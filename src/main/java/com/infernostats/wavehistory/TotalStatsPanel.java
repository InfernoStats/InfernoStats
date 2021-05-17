package com.infernostats.wavehistory;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TotalStatsPanel extends JPanel {
    public TotalStatsPanel() {
        setBorder(new EmptyBorder(8, 8, 8, 8));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel titleLabel = new JLabel();
        titleLabel.setText("Inferno Stats");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel);
    }
}
