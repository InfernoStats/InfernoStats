package com.infernostats.tickcounter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.Sets;
import com.infernostats.InfernoStatsConfig;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.kit.KitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class TickCounter
{
	private static final Logger logger = LoggerFactory.getLogger(TickCounter.class);

	public static final int BLOWPIPE_TICKS = 2;

	private InfernoStatsConfig config;

	private int idleTicks = 0;
	private int idleTickStartTimer = 0;
	Set<Integer> attackableNpcs = new HashSet<>();
	// The onAnimationChanged event does not fire during Rapid blowpiping as the animation is 3 ticks long so it simply
	// repeats. Therefore we simply store whether the blowpipe is being fired or not and infer from the animation frame
	// if it's being fired at 2t or 3t.
	private boolean isBlowpiping = false;

	boolean instanced = false;
	boolean prevInstance = false;

	public TickCounter(InfernoStatsConfig config) {
		this.config = config;
	}

	public void onAnimationChanged(Client client, AnimationChanged e)
	{
		if (!config.trackIdleTicks())
			return;
		if (!(e.getActor() instanceof Player))
			return;
		Player p = (Player) e.getActor();
		int weapon = -1;
		if (p.getPlayerComposition() != null)
			weapon = p.getPlayerComposition().getEquipmentId(KitType.WEAPON);
		int animation = p.getAnimation();
		// Get default ticks for the animation + weapon combination.
		int delta = TickCounterUtils.getTicksForAnimation(animation, weapon);
		// Handle special cases.
		switch (animation)
		{
			case 7617: // rune knife
			case 8194: // dragon knife
			case 8291: // dragon knife spec
			case 5061: // blowpipe
				if (weapon == 12926)
				{
					delta = 0;
					isBlowpiping = true;
					logger.debug("{} BP animation start", client.getTickCount());
				}
				break;
			case -1:
				isBlowpiping = false;
				logger.debug("{} Clear blowpipe animation state", client.getTickCount());
				break;
		}
		if (delta > 0)
		{
			// Store the tick the player should be able to attack again.
			this.idleTickStartTimer = client.getTickCount() + delta;
		}
	}

	public void onGameTick(Client client, GameTick tick)
	{
		if (config.trackIdleTicks() && client.getTickCount() > idleTickStartTimer) {
			Set<Integer> oldAttackableNpcs = attackableNpcs;
			attackableNpcs = getNearbyAttackableNpcs(client);
			// If there were any NPCs that were attackable in the previous tick that are still attackable now, then
			// we lost a tick. This avoids counting missed ticks when the first NPC spawns (since you can't attack on
			// the same tick).
			if (oldAttackableNpcs != null) {
				Set<Integer> intersection = Sets.intersection(oldAttackableNpcs, attackableNpcs);
				if (intersection.size() > 0) {
					log.debug("{} idle tick", client.getTickCount());
					++this.idleTicks;
				}
			}
		}
		if (isBlowpiping && client.getLocalPlayer().getAnimationFrame() == 0)
		{
			idleTickStartTimer = client.getTickCount() + BLOWPIPE_TICKS;
			logger.debug("{} BP animation started, +2 ticks ", client.getTickCount());
		}
		prevInstance = instanced;
		instanced = client.isInInstancedRegion();
		if (!prevInstance && instanced)
		{
			clearState();
		}
	}

	public void startIdleTimer(int clientTick) {
		idleTickStartTimer = clientTick;
	}

	public int getIdleTicks() {
		return idleTicks;
	}

	public void clearState() {
		idleTicks = 0;
		idleTickStartTimer = Integer.MAX_VALUE;
		isBlowpiping = false;
		attackableNpcs.clear();
	}

	/**
	 * Returns a hashset of attackable NPCs nearby. Does not include NPCs that are at zero HP.
	 *
	 * @param client Client to query on.
	 * @return Set of IDs of nearby attackable NPCs.
	 */
	private static Set<Integer> getNearbyAttackableNpcs(Client client) {
		Set<Integer> result = new HashSet<>();
		if (!client.getNpcs().isEmpty()) {
			client.getNpcs().stream().filter(npc -> {
				return !npc.isDead() && isAttackable(npc);
			}).forEach(npc -> result.add(npc.getId()));
		}
		return result;
	}

	private static boolean isAttackable(NPC npc) {
		for (int i = 0; i < npc.getComposition().getActions().length; ++i) {
			String action = npc.getComposition().getActions()[i];
			if (action != null && action.equals("Attack")) {
				return true;
			}
		}
		return false;
	}
}
