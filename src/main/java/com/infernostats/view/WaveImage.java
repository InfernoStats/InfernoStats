package com.infernostats.view;

import com.infernostats.model.WaveSpawn;
import net.runelite.client.util.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class WaveImage extends JLabel {
	private final ArrayList<WaveSpawn> spawns;

	public WaveImage() {
		int IMAGE_SCALE = 2;
		BufferedImage image = ImageUtil.loadImageResource(getClass(), "/inferno-base.png");
		Image scaledImage = image.getScaledInstance(
				image.getWidth() * IMAGE_SCALE,
				image.getHeight() * IMAGE_SCALE,
				Image.SCALE_DEFAULT
		);
		this.setIcon(new ImageIcon(scaledImage));
		this.spawns = new ArrayList<>();
	}

	public void drawSpawn(int x, int y, int size, Color color) {
		spawns.add(new WaveSpawn(x, y, size, color));
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (WaveSpawn spawn : spawns) {
			g.setColor(spawn.getColor());
			g.fillRect(spawn.getX() * 2, spawn.getY() * 2, spawn.getSize(), spawn.getSize());
		}
	}
}
