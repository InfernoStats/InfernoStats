package com.infernostats.wavehistory;

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
    private JLabel titleLabel;
    private final WaveHistory waveHistory;

    @Inject
    public WaveSplitsPanel(WaveHistory waveHistory) {
        this.waveHistory = waveHistory;

        setBorder(new EmptyBorder(2, 8, 2, 8));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        titleLabel = new JLabel();
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
                                new StringSelection(WaveSplitTimes()),
                                null
                        );
            }
        };
        addMouseListener(waveStatsMouseListener);
    }

    private void setUnderline(JLabel label)
    {
        Font font = label.getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        label.setFont(font.deriveFont(attributes));
    }

    private void unsetUnderline(JLabel label)
    {
        Font font = label.getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, -1);
        label.setFont(font.deriveFont(attributes));
    }

    public String WaveSplitTimes() {
        StringBuilder splits = new StringBuilder();

        for (Wave wave : waveHistory.waves) {
            if (wave.IsSplit()) {
                splits.append("Wave: " + wave.id + ", ");
                splits.append("Wave Split: " + wave.SplitTimeString());
                splits.append('\n');
            }
        }

        if (waveHistory.waves.isEmpty())
        {
            splits.append("Duration (Not Started): N/a");
            return splits.toString();
        }

        Wave currWave = waveHistory.CurrentWave();
        if (currWave.id == 69 && currWave.stopTime != null)
        {
            if (currWave.forceReset)
            {
                // Timer is paused because you force reset at zuk
                splits.append("Duration (Unfinished): " + currWave.CurrentTimeString());
            }
            else if (currWave.failed)
            {
                // You died at zuk and the timer is stopped
                splits.append("Duration (Failed): " + currWave.CurrentTimeString());
            }
            else
            {
                // You killed zuk and the timer is stopped
                splits.append("Duration (Success): " + currWave.CurrentTimeString());
            }
        }
        else if (currWave.failed)
        {
            splits.append("Duration (Failed): " + currWave.CurrentTimeString());
        }
        else
        {
            splits.append("Duration (Unfinished): " + currWave.CurrentTimeString());
        }

        return splits.toString();
    }
}
