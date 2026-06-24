package com.infernostats.model;

import lombok.Getter;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;

@Getter
public class WaveNpc {
	private final InfernoNpc type;
	// The region spawn point of the NPC
	private final Point spawn;

	public WaveNpc(InfernoNpc type, WorldPoint spawn) {
		this.type = type;
		this.spawn = new Point(spawn.getRegionX(), spawn.getRegionY());
	}

	@Override
	public String toString() {
		return type.name + "@{" + spawn.getX() + "," + spawn.getY() + "}";
	}
}
