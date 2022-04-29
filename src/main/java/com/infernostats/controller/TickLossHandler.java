package com.infernostats.controller;

import com.google.common.collect.Sets;
import com.infernostats.InfernoStatsConfig;
import com.infernostats.InfernoStatsPlugin;
import com.infernostats.events.WaveStartedEvent;
import com.infernostats.model.Wave;
import com.infernostats.model.WaveState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class TickLossHandler {
	private Wave wave;
	private int magicXp;
	private int tickDelay;
	private int currChinCount;
	private int prevChinCount;
	private Animation currAnim;
	private Animation prevAnim;
	private Set<Integer> attackableNpcs;

	@Inject
	private Client client;

	@Inject
	private ItemManager itemManager;

	private final InfernoStatsPlugin plugin;
	private final InfernoStatsConfig config;

	@Inject
	protected TickLossHandler(InfernoStatsPlugin plugin, InfernoStatsConfig config) {
		this.plugin = plugin;
		this.config = config;

		this.magicXp = -1;
		this.tickDelay = 0;
		this.currChinCount = -1;
		this.prevChinCount = -1;

		this.currAnim = Animation.UNKNOWN;
		this.prevAnim = Animation.UNKNOWN;

		this.attackableNpcs = new HashSet<>();
	}

	@Subscribe
	protected void onWaveStartedEvent(WaveStartedEvent e) {
		this.wave = e.getWave();
	}

	@Subscribe
	public void onStatChanged(StatChanged e) {
		if (e.getSkill() != Skill.MAGIC)
			return;

		final int newMagicXp = e.getXp();
		if (newMagicXp == this.magicXp)
			return;

		this.magicXp = newMagicXp;

		// Ghost barrage occurred
		if (this.prevAnim == Animation.BLOWPIPE)
			this.tickDelay += 5;
	}

	@Subscribe
	protected void onGameTick(GameTick e) {
		if (this.tickDelay > 0)
			this.tickDelay -= 1;

		if (isCooldownActive() || !isWaveActive())
			return;

		Player player = this.client.getLocalPlayer();
		if (player == null)
			return;

		PlayerComposition composition = player.getPlayerComposition();
		if (composition == null)
			return;

		final int weapon = composition.getEquipmentId(KitType.WEAPON);

		this.prevAnim = this.currAnim;
		this.currAnim = Animation.valueOf(player.getAnimation());

		if (this.prevAnim != Animation.BLOWPIPE && this.currAnim == Animation.BLOWPIPE) {
			this.tickDelay = 2;
		} else if (this.prevAnim == Animation.BLOWPIPE && this.currAnim == Animation.BLOWPIPE) {
			if (player.getAnimationFrame() == 0)
				this.tickDelay = 2;
		}

		this.tickDelay += getAnimationDelay(this.currAnim, weapon);

		Set<Integer> prevNpcs = attackableNpcs;
		this.attackableNpcs = getNearbyAttackableNpcs();
		if (prevNpcs.size() != 0) {
			Set<Integer> intersection = Sets.intersection(prevNpcs, attackableNpcs);
			if (intersection.size() > 0 && this.tickDelay == 0) {
				this.wave.setIdleTicks(this.wave.getIdleTicks() + 1);
			}
		}
	}

	private boolean isCooldownActive() {
		return this.tickDelay > 1;
	}

	private boolean isWaveActive() {
		return this.wave != null && this.wave.getState() == WaveState.STARTED;
	}

	private Set<Integer> getNearbyAttackableNpcs() {
		Set<Integer> result = new HashSet<>();
		if (!this.client.getNpcs().isEmpty()) {
			this.client.getNpcs().stream().filter(npc -> {
				return !npc.isDead() && isAttackable(npc);
			}).forEach(npc -> result.add(npc.getIndex()));
		}
		return result;
	}

	private boolean isAttackable(NPC npc) {
		for (int i = 0; i < npc.getComposition().getActions().length; ++i) {
			String action = npc.getComposition().getActions()[i];
			if (action != null && action.equals("Attack")) {
				return true;
			}
		}
		return false;
	}

	private int getAnimationDelay(Animation animation, int weapon) {
		switch (animation) {
			case BLOWPIPE:
				switch (weapon) {
					case ItemID.CHINCHOMPA:
					case ItemID.CHINCHOMPA_10033:
					case ItemID.RED_CHINCHOMPA:
					case ItemID.RED_CHINCHOMPA_10034:
					case ItemID.BLACK_CHINCHOMPA:
						break;
					default:
						return 0;
				}

				ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
				if (itemContainer == null)
					return 0;

				this.prevChinCount = this.currChinCount;
				this.currChinCount = itemContainer.count(weapon);

				if (this.prevChinCount > this.currChinCount)
					return 3;

				return 0;
			case EVENT_RPG:
			case CHINCHOMPA:
			case TRIDENT_SANG:
				return 3;
			case BOW:
				return weapon == ItemID.TWISTED_BOW ? 5 : 3;
			case SURGE:
				return weapon == ItemID.HARMONISED_NIGHTMARE_STAFF ? 4 : 5;
			case STAFF_BASH:
				switch (weapon) {
					case ItemID.BEGINNER_WAND:
					case ItemID.APPRENTICE_WAND:
					case ItemID.TEACHER_WAND:
					case ItemID.MASTER_WAND:
					case ItemID.KODAI_WAND:
					case ItemID._3RD_AGE_WAND:
						return 4;
					default:
						return 5;
				}
			case CLAW_SCRATCH:
			case CLAW_SPEC:
			case RAPIER:
			case INQ_MACE:
				return 4;
			case DINHS:
			case STRIKE_BOLT:
			case BURST_BLITZ:
			case CROSSBOW:
			case SCYTHE:
			case BARRAGE:
				return 5;
			case CHALLY_JAB:
			case CHALLY_SWIPE:
			case CHALLY_SPEC:
				return 7;
			default:
				return 0;
		}
	}

	@Getter
	@AllArgsConstructor
	private enum Animation {
		UNKNOWN(-2),
		IDLE(-1),
		CLAW_SCRATCH(393),
		STAFF_BASH(414),
		PUNCH(422),
		KICK(423),
		BOW(426),
		CHALLY_JAB(428),
		CHALLY_SWIPE(440),
		MSB_SPEC(1074),
		STRIKE_BOLT(1162),
		TRIDENT_SANG(1167),
		CHALLY_SPEC(1203),
		BURST_BLITZ(1379),
		BARRAGE(1979),
		EVENT_RPG(2323),
		INQ_MACE(4503),
		BLOWPIPE(5061),
		DINHS(7511),
		CLAW_SPEC(7514),
		CROSSBOW(7552),
		CHINCHOMPA(7618),
		SURGE(7855),
		SCYTHE(8056),
		RAPIER(8145);

		private final int id;

		public static Animation valueOf(int id) {
			return Arrays.stream(values())
					.filter(anim -> anim.id == id)
					.findFirst()
					.orElse(UNKNOWN);
		}
	}
}

