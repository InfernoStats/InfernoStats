package com.infernostats.tickcounter;

import java.util.HashSet;
import java.util.Optional;
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

/**
 * Adapted from https://github.com/winterdaze/tick-counter but changed to track only a single player.
 */
@Slf4j
public class TickCounter
{
	private static final Logger logger = LoggerFactory.getLogger(TickCounter.class);

	public static final int BLOWPIPE_TICKS = 2;
	public static final int BLOWPIPE_ID = 12926;

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

	private Optional<AnimationChanged> animationUpdate = Optional.empty();

	public TickCounter(InfernoStatsConfig config) {
		this.config = config;
	}

	public void onAnimationChanged(Client client, AnimationChanged e)
	{
		if (!config.trackIdleTicks())
			return;
		if (!(e.getActor() instanceof Player))
			return;
		// We need to defer the change to the Game Tick so we can see if the player has also changed weapons on this
		// tick.
		animationUpdate = Optional.of(e);
	}

	private void handleAnimationChange(Client client, AnimationChanged e, int weaponId) {
		Player p = (Player) e.getActor();
		int animation = p.getAnimation();
		// Get default ticks for the animation + weapon combination.
		// We use the weapon that was equipped on the previous tick as it is possible to change weapon on the same tick
		// as animation start, which makes it think you've attacked with the new weapon.
		int delta = TickCounterUtils.getTicksForAnimation(animation, weaponId);
		// Handle special cases.
		switch (animation)
		{
			case 7617: // rune knife
			case 8194: // dragon knife
			case 8291: // dragon knife spec
			case 5061: // blowpipe
				if (weaponId == BLOWPIPE_ID)
				{
					delta = 0;
					isBlowpiping = true;
				}
				break;
			case -1:
				isBlowpiping = false;
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
		Player player = client.getLocalPlayer();
		int weaponId = player.getPlayerComposition().getEquipmentId(KitType.WEAPON);
		log.debug("{} Player is holding weapon {}", client.getTickCount(), weaponId);
		if (animationUpdate.isPresent()) {
			handleAnimationChange(client, animationUpdate.get(), weaponId);
			animationUpdate = Optional.empty();
		}

		if (config.trackIdleTicks()) {
			Set<Integer> oldAttackableNpcs = attackableNpcs;
			attackableNpcs = getNearbyAttackableNpcs(client);
			if (client.getTickCount() > idleTickStartTimer) {
				// If there were any NPCs that were attackable in the previous tick that are still attackable now, then
				// we lost a tick. This avoids counting missed ticks when the first NPC spawns (since you can't attack on
				// the same tick).
				if (oldAttackableNpcs != null) {
					Set<Integer> intersection = Sets.intersection(oldAttackableNpcs, attackableNpcs);
					if (intersection.size() > 0) {
						++this.idleTicks;
					}
				}
			}
		}
		if (isBlowpiping && player.getAnimationFrame() == 0)
		{
			idleTickStartTimer = client.getTickCount() + BLOWPIPE_TICKS;
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
		log.debug("Clearing state for TickCounter");
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
			}).forEach(npc -> result.add(npc.getIndex()));
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
