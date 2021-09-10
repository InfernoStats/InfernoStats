package com.infernostats.wavehistory;

import com.infernostats.InfernoStatsConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WaveStatsPanel extends JPanel {
    public Wave wave;
    public String serializedLineOfSight;

    public JLabel waveNumber;
    public JLabel duration;
    public JLabel damageTaken;
    public JLabel prayerDrain;
    public JLabel damageDealt;
    public JLabel idleTicks;
    public WaveImage imageLabel;
    private JPanel textPanel;
    private InfernoStatsConfig config;
    private boolean repainted = false;

    private static final Border normalBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, ColorScheme.DARK_GRAY_COLOR),
                new EmptyBorder(4, 6, 4, 6));

    private static final Border hoverBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, ColorScheme.DARK_GRAY_COLOR),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_HOVER_COLOR),
                        new EmptyBorder(3, 5, 3, 5)));

    public WaveStatsPanel(InfernoStatsConfig config, Wave wave)
    {
        this.wave = wave;
        serializedLineOfSight = wave.SerializeWave();

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
        if (wave.IsSplit())
        {
            waveNumber.setText("Wave " + wave.id + " - " + wave.CurrentTimeString());
        }
        else
        {
            waveNumber.setText("Wave " + wave.id);
        }
        waveNumber.setHorizontalAlignment(SwingConstants.CENTER);
        waveNumber.setForeground(wave.failed ? Color.RED : Color.WHITE);
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
        duration.setText("Time: " + wave.WaveTimeString());
        duration.setForeground(Color.WHITE);
        durationLine.add(duration, BorderLayout.WEST);

        JPanel damageTakenLine = new JPanel();
        damageTakenLine.setLayout(new BorderLayout());
        damageTakenLine.setBackground(null);

        damageTaken = new JLabel();
        damageTaken.setText("Damage Taken: " + wave.damageTaken);
        damageTaken.setForeground(Color.WHITE);
        damageTakenLine.add(damageTaken, BorderLayout.WEST);

        JPanel prayerDrainLine = new JPanel();
        prayerDrainLine.setLayout(new BorderLayout());
        prayerDrainLine.setBackground(null);

        prayerDrain = new JLabel();
        prayerDrain.setText("Prayer Drain: " + wave.prayerDrain);
        prayerDrain.setForeground(Color.WHITE);
        prayerDrainLine.add(prayerDrain, BorderLayout.WEST);

        JPanel damageDealtLine = new JPanel();
        damageDealtLine.setLayout(new BorderLayout());
        damageDealtLine.setBackground(null);

        damageDealt = new JLabel();
        damageDealt.setText("Damage Dealt: " + wave.damageDealt);
        damageDealt.setForeground(Color.WHITE);
        damageDealtLine.add(damageDealt, BorderLayout.WEST);

        JPanel idleTicksLine = new JPanel();
        idleTicksLine.setLayout(new BorderLayout());
        idleTicksLine.setBackground(null);

        idleTicks = new JLabel();
        idleTicks.setText("Idle ticks: " + wave.idleTicks);
        idleTicks.setForeground(Color.WHITE);
        if (config.trackIdleTicks() && config.showIdleTicksInSidepanel()) {
            idleTicksLine.add(idleTicks, BorderLayout.WEST);
        }

        textPanel.add(durationLine);
        textPanel.add(damageTakenLine);
        textPanel.add(prayerDrainLine);
        textPanel.add(damageDealtLine);
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

        MouseAdapter waveStatsMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                setBackground(ColorScheme.DARK_GRAY_COLOR);
                for (Component c : getComponents())
                {
                    c.setBackground(ColorScheme.DARK_GRAY_COLOR);
                }
                setBorder(hoverBorder);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                setBackground(ColorScheme.DARKER_GRAY_COLOR);
                for (Component c : getComponents())
                {
                    c.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                }
                setBorder(normalBorder);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getButton() == MouseEvent.BUTTON3)
                {
                    return;
                }

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI(serializedLineOfSight));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    } catch (URISyntaxException uriSyntaxException) {
                        uriSyntaxException.printStackTrace();
                    }
                }
            }
        };
        addMouseListener(waveStatsMouseListener);
    }

    public void RedrawWaveSpawn()
    {
        Map<String, ArrayList<ArrayList<Integer>>> mobs = wave.RebasedNPCs();
        Map<String, Integer> mob_sizes = new HashMap<String, Integer>() {{
            put("Jal-MejRah", 2);
            put("Jal-Ak", 3);
            put("Jal-ImKot", 4);
            put("Jal-Xil", 3);
            put("Jal-Zek", 4);
        }};

        Map<String, Color> mob_colors = new HashMap<String, Color>() {{
            put("Jal-MejRah", Color.GRAY);
            put("Jal-Ak", Color.YELLOW);
            put("Jal-ImKot", Color.ORANGE);
            put("Jal-Xil", Color.GREEN);
            put("Jal-Zek", Color.RED);
        }};

        for (Map.Entry<String, ArrayList<ArrayList<Integer>>> entry : mobs.entrySet())
        {
            final String name = entry.getKey();
            final int size = mob_sizes.get(name);
            final ArrayList<ArrayList<Integer>> spawnTiles = entry.getValue();

            for (ArrayList<Integer> spawnTile : spawnTiles)
            {
                final int x = spawnTile.get(0);
                final int y = spawnTile.get(1);

                imageLabel.drawSpawn(x, y, size, mob_colors.get(name));
            }
        }

        imageLabel.revalidate();
        imageLabel.repaint();
    }
}

class WaveImage extends JLabel
{
    private final int IMAGE_SCALE = 2;
    private final BufferedImage image = ImageUtil.loadImageResource(getClass(), "/inferno-base.png");
    private final Image scaledImage = image.getScaledInstance(
            image.getWidth() * IMAGE_SCALE,
            image.getHeight() * IMAGE_SCALE,
            Image.SCALE_DEFAULT
    );

    class WaveSpawn
    {
        int x;
        int y;
        int size;
        Color color;

        public WaveSpawn(int x, int y, int size, Color color)
        {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
        }
    }
    private ArrayList<WaveSpawn> spawns;

    public WaveImage()
    {
        this.setIcon(new ImageIcon(scaledImage));
        this.spawns = new ArrayList<>();
    }

    public void drawSpawn(int x, int y, int size, Color color)
    {
        spawns.add(new WaveSpawn(x, y, size, color));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (WaveSpawn spawn : spawns)
        {
            g.setColor(spawn.color);
            g.fillRect(spawn.x * 2, spawn.y * 2, spawn.size, spawn.size);
        }
    }
}