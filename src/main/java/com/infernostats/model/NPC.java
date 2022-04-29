package com.infernostats.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;

@Getter
@AllArgsConstructor
public class NPC {
	private final String name;
	private final WorldPoint tile;

	@Override
	public String toString() {
		return name + "@{" + tile.getRegionX() + "," + tile.getRegionY() + "}";
	}
}
