package com.infernostats.specialweapon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;

@AllArgsConstructor
@Getter
public
enum SpecialWeapon
{
	SARADOMIN_GODSWORD("Saradomin Godsword", ItemID.SARADOMIN_GODSWORD, true, true),
	SARADOMIN_GODSWORD_OR("Saradomin Godsword", ItemID.SARADOMIN_GODSWORD_OR, true, true),
	TOXIC_BLOWPIPE("Toxic Blowpipe", ItemID.TOXIC_BLOWPIPE, true, false),
	ELDRITCH_NIGHTMARE_STAFF("Eldritch Nightmare Staff", ItemID.ELDRITCH_NIGHTMARE_STAFF, false, true);

	private final String name;
	private final int itemID;
	private final boolean health;
	private final boolean prayer;

	public static final int ELDRITCH_PRAYER_CAP = 120;
}