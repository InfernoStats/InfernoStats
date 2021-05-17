package com.infernostats;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import com.infernostats.specialweapon.SpecialWeapon;
import com.infernostats.specialweapon.SpecialWeaponStats;
import com.infernostats.wavehistory.WaveHistoryPanel;
import com.infernostats.wavehistory.WaveHistory;
import com.infernostats.wavehistory.WaveTimer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.regex.Matcher;

import static net.runelite.api.Skill.HITPOINTS;
import static net.runelite.api.Skill.PRAYER;
import static net.runelite.api.ItemID.INFERNAL_CAPE;

@PluginDescriptor(
		name = "Inferno Stats",
		description = "Track restoration specials during an inferno attempt.",
		tags = {"combat", "npcs", "overlay"},
		enabledByDefault = false
)
@Slf4j
public class InfernoStatsPlugin extends Plugin
{
	private boolean prevMorUlRek;
	private int specialPercentage;
	private Actor lastSpecTarget;
	private int lastSpecTick;
	private WaveTimer waveTimer;
	private WaveHistory waveHistory;
	private SpecialWeapon specialWeapon;

	private static final String CONFIG_GROUP = "infernostats";
	private static final String HIDE_KEY = "hide";
	private static final String TIMER_KEY = "showWaveTimer";
	private static final String SPECIAL_KEY = "showSpecialCounter";

	// Prayer points on every tick
	private int currPrayer;
	// HP and Prayer points on the spec's current tick
	private int currSpecHealth, currSpecPrayer;
	// HP and Prayer points on the spec's previous tick
	private int prevSpecHealth, prevSpecPrayer;

	private static final int INFERNO_REGION_ID = 9043;
	private static final Set<Integer> VOID_REGION_IDS = ImmutableSet.of(
			13135, 13136, 13137, 13391, 13392, 13393, 13647, 13648, 13649
	);
	private static final Set<Integer> MOR_UL_REK_REGION_IDS = ImmutableSet.of(
			9806, 9807, 9808, 9809, 10063, 10064, 10065, 10319, 10320, 10321
	);

	public static final SpecialWeaponStats[] specialCounter = new SpecialWeaponStats[SpecialWeapon.values().length];

	@Inject
	private Client client;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private InfernoStatsConfig config;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private ItemManager itemManager;

	@Getter(AccessLevel.PACKAGE)
	private WaveHistoryPanel panel;

	@Getter(AccessLevel.PACKAGE)
	private NavigationButton navButton;

	@Provides
	InfernoStatsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InfernoStatsConfig.class);
	}

	@Override
	protected void startUp()
	{
		specialPercentage = -1;
		prevMorUlRek = false;
		waveHistory = new WaveHistory();

		panel = injector.getInstance(WaveHistoryPanel.class);
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/blob-square.png");
		navButton = NavigationButton.builder()
				.tooltip("Inferno Stats")
				.icon(icon)
				.priority(6)
				.panel(panel)
				.build();

		if (isInInferno() || !config.hide())
		{
			clientToolbar.addNavigation(navButton);
		}
	}

	@Override
	protected void shutDown()
	{
		removeCounters();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState gameState = event.getGameState();
		if (gameState == GameState.HOPPING
				|| gameState == GameState.LOGIN_SCREEN)
		{
			if (waveTimer != null && !waveTimer.IsPaused())
			{
				waveTimer.Pause();
			}

			// User force-logged or hopped before finishing the wave
			if (waveHistory.CurrentWave().stopTime == null)
			{
				waveHistory.CurrentWave().Pause();
			}
		}

		if (gameState != GameState.LOGGED_IN
				&& gameState != GameState.LOADING)
		{
			return;
		}

		final boolean inVoid = isInVoid();
		final boolean inInferno = isInInferno();
		final boolean inMorUlRek = isInMorUlRek();

		if (!inVoid && !inInferno)
		{
			removeWaveTimer();
		}

		if (inInferno || !config.hide())
		{
			clientToolbar.addNavigation(navButton);
		}
		else
		{
			clientToolbar.removeNavigation(navButton);
		}

		if (inVoid)
		{
			// The user has logged in, but is placed into a temporary void.
			// This is a holding area before being placed into the inferno instance.
			log.debug("User logged out and back in, and is now in the void.");
		}
		else if (inMorUlRek)
		{
			// The user is currently in Mor-Ul-Rek.
			prevMorUlRek = true;
		}
		else if (inInferno && prevMorUlRek == true)
		{
			// The user jumped into the inferno from Mor-Ul-Rek. Clear any existing infoboxes.
			waveHistory.ClearWaves();
			panel.ClearWaves();
			removeCounters();
			removeWaveTimer();
			prevMorUlRek = false;
		}
		else if (inInferno && prevMorUlRek == false)
		{
			// For completeness, the user was moved from the void to the inferno.
			log.debug("User has been moved from the void to an inferno instance.");
		}
		else if (!inInferno && !inMorUlRek)
		{
			// The user is neither in the inferno, nor in Mor-Ul-Rek. Clear any existing infoboxes.
			removeCounters();
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		Actor source = event.getSource();
		Actor target = event.getTarget();

		if (lastSpecTick != client.getTickCount() || source != client.getLocalPlayer() || target == null)
		{
			return;
		}

		lastSpecTarget = target;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!isInInferno())
		{
			return;
		}

		if (waveHistory.waves.size() != 0)
		{
			panel.updateWave(waveHistory.CurrentWave());
		}

		currPrayer = client.getBoostedSkillLevel(PRAYER);

		if (lastSpecTick != client.getTickCount())
		{
			return;
		}

		currSpecHealth = client.getBoostedSkillLevel(HITPOINTS);
		currSpecPrayer = client.getBoostedSkillLevel(PRAYER);
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		int specialPercentage = client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT);

		if (this.specialPercentage == -1 || specialPercentage >= this.specialPercentage)
		{
			this.specialPercentage = specialPercentage;
			return;
		}

		this.specialPercentage = specialPercentage;
		this.specialWeapon = usedSpecialWeapon();

		lastSpecTarget = client.getLocalPlayer().getInteracting();
		lastSpecTick = client.getTickCount();

		prevSpecPrayer = client.getBoostedSkillLevel(Skill.PRAYER);
		prevSpecHealth = client.getBoostedSkillLevel(Skill.HITPOINTS);
	}

	@Subscribe
	public void onStatChanged(StatChanged event) {
		if (!isInInferno()) {
			return;
		}

		// We haven't started Wave 1 yet
		if (waveHistory.waves.size() == 0)
		{
			return;
		}

		if (event.getSkill() == PRAYER)
		{
			final int prayer = event.getBoostedLevel();
			if (prayer == currPrayer - 1)
			{
				waveHistory.CurrentWave().prayerDrain += 1;
			}
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied event)
	{
		Actor target = event.getActor();
		Hitsplat hitsplat = event.getHitsplat();

		if (!isInInferno())
		{
			return;
		}

		if (!hitsplat.isMine())
		{
			return;
		}

		if (target == client.getLocalPlayer())
		{
			// NPC did damage to the player
			if (hitsplat.getHitsplatType() == Hitsplat.HitsplatType.DAMAGE_ME)
			{
				waveHistory.CurrentWave().damageTaken += hitsplat.getAmount();
			}
			return;
		}
		else
		{
			// Player did damage to an NPC
			if (hitsplat.getHitsplatType() == Hitsplat.HitsplatType.DAMAGE_ME)
			{
				waveHistory.CurrentWave().damageDealt += hitsplat.getAmount();
			}
		}

		if (lastSpecTarget != null && lastSpecTarget != target)
		{
			return;
		}

		if (!(target instanceof NPC))
		{
			return;
		}

		// BP spec hits 2 (bp speed) + 1 (delay) ticks after varbit changes
		if (specialWeapon == SpecialWeapon.TOXIC_BLOWPIPE)
		{
			if (client.getTickCount() != lastSpecTick + 3)
			{
				return;
			}
		}

		boolean wasSpec = lastSpecTarget != null;
		lastSpecTarget = null;

		if (wasSpec && specialWeapon != null)
		{
			UpdateCounter(specialWeapon, hitsplat.getAmount());
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		final Actor npcActor = event.getActor();
		final WorldPoint spawnTile = npcActor.getWorldLocation();

		if (!isInInferno())
		{
			return;
		}

		// ROCKY_SUPPORT is the normal pillar id; ROCKY_SUPPORT_7710 spawns as a pillar falls
		if (npc.getId() == NpcID.ROCKY_SUPPORT || npc.getId() == NpcID.ROCKY_SUPPORT_7710)
		{
			return;
		}

		// We'll ignore nibblers and zuk spawns off the map
		if (npc.getId() == NpcID.JALNIB || npc.getId() == NpcID.TZKALZUK)
		{
			return;
		}

		// We only want the original wave spawn, not minions or mager respawns
		if (waveHistory.CurrentWave().WaveTime() > 1 * 1000)
		{
			return;
		}

		waveHistory.AddSpawn(spawnTile, npc);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		final String message = event.getMessage();
		if (event.getType() != ChatMessageType.SPAM && event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		if (!isInInferno())
		{
			return;
		}

		if (WaveTimer.DEFEATED_MESSAGE.matcher(message).matches())
		{
			removeWaveTimer();
			waveHistory.CurrentWave().Finished(true);
			panel.updateWave(waveHistory.CurrentWave());
			return;
		}

		if (WaveTimer.COMPLETE_MESSAGE.matcher(message).matches())
		{
			removeWaveTimer();
			waveHistory.CurrentWave().Finished(false);
			panel.updateWave(waveHistory.CurrentWave());
			return;
		}

		if (WaveTimer.PAUSED_MESSAGE.matcher(message).find())
		{
			waveTimer.Pause();
			waveHistory.CurrentWave().Finished(false);
			return;
		}

		if (WaveTimer.WAVE_COMPLETE_MESSAGE.matcher(message).find())
		{
			waveHistory.CurrentWave().Finished(false);

			if (config.waveTimes())
			{
				final String waveMessage = new ChatMessageBuilder()
						.append(ChatColorType.HIGHLIGHT)
						.append("Wave Completed in: " + waveHistory.CurrentWave().WaveTimeString())
						.build();

				chatMessageManager.queue(
						QueuedMessage.builder()
								.type(ChatMessageType.CONSOLE)
								.runeLiteFormattedMessage(waveMessage)
								.build());
			}
		}

		Matcher matcher = WaveTimer.WAVE_MESSAGE.matcher(message);
		if (matcher.find())
		{
			int wave = Integer.parseInt(matcher.group(1));

			// TODO: Does the in-game timer reset to 6 seconds if you force log on wave 1?
			if (wave == 1 || waveTimer == null)
			{
				createWaveTimer();
			}
			else if (waveTimer != null && waveTimer.IsPaused())
			{
				waveTimer.Resume();

				if (waveHistory.CurrentWave().forceReset)
				{
					waveHistory.CurrentWave().ReinitWave();
					return;
				}
			}

			waveHistory.NewWave(wave, waveTimer.GetTime());
			panel.addWave(waveHistory.CurrentWave());

			if (config.splitTimes() && waveHistory.CurrentWave().IsSplit())
			{
				final String splitMessage = new ChatMessageBuilder()
						.append(ChatColorType.HIGHLIGHT)
						.append("Wave Split: " + waveTimer.GetTime())
						.build();

				chatMessageManager.queue(
						QueuedMessage.builder()
								.type(ChatMessageType.CONSOLE)
								.runeLiteFormattedMessage(splitMessage)
								.build());
			}
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals(CONFIG_GROUP))
		{
			return;
		}

		if (event.getKey().equals(HIDE_KEY))
		{
			if (isInInferno() || !config.hide())
			{
				clientToolbar.addNavigation(navButton);
			}
			else
			{
				clientToolbar.removeNavigation(navButton);
			}
		}

		if (event.getKey().equals(TIMER_KEY))
		{
			if (config.waveTimer() && waveTimer != null)
			{
				infoBoxManager.addInfoBox(waveTimer);
			}
			else
			{
				infoBoxManager.removeInfoBox(waveTimer);
			}
		}

		if (event.getKey().equals(SPECIAL_KEY))
		{
			if (config.specialCounter())
			{
				for (SpecialWeaponStats counter : specialCounter)
				{
					if (counter != null)
					{
						infoBoxManager.addInfoBox(counter);
					}
				}
			}
			else
			{
				for (SpecialWeaponStats counter : specialCounter)
				{
					if (counter != null)
					{
						infoBoxManager.removeInfoBox(counter);
					}
				}
			}
		}
	}

	private SpecialWeapon usedSpecialWeapon()
	{
		ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment == null)
		{
			return null;
		}

		Item weapon = equipment.getItem(EquipmentInventorySlot.WEAPON.getSlotIdx());
		if (weapon == null)
		{
			return null;
		}

		for (SpecialWeapon specialWeapon : SpecialWeapon.values())
		{
			if (specialWeapon.getItemID() == weapon.getId())
			{
				return specialWeapon;
			}
		}

		return null;
	}

	private void UpdateCounter(SpecialWeapon specialWeapon, int hit)
	{
		SpecialWeaponStats counter = specialCounter[specialWeapon.ordinal()];

		if (counter == null)
		{
			counter = new SpecialWeaponStats(client, itemManager.getImage(specialWeapon.getItemID()), this, specialWeapon);
			infoBoxManager.addInfoBox(counter);
			specialCounter[specialWeapon.ordinal()] = counter;
		}

		counter.addHits(hit, prevSpecHealth, currSpecHealth,
				prevSpecPrayer, currSpecPrayer, config.effectiveRestoration());
	}

	private void removeCounters()
	{
		for (int i = 0; i < specialCounter.length; ++i)
		{
			SpecialWeaponStats counter = specialCounter[i];

			if (counter != null)
			{
				infoBoxManager.removeInfoBox(counter);
				specialCounter[i] = null;
			}
		}
	}

	private void createWaveTimer()
	{
		// The first wave message of the inferno comes six seconds after the in-game timer starts counting
		waveTimer = new WaveTimer(
				itemManager.getImage(INFERNAL_CAPE),
				this,
				Instant.now().minus(Duration.ofSeconds(6)),
				null
		);
		if (config.waveTimer())
		{
			infoBoxManager.addInfoBox(waveTimer);
		}
	}

	private void removeWaveTimer()
	{
		if (waveTimer != null)
		{
			infoBoxManager.removeInfoBox(waveTimer);
			waveTimer = null;
		}
	}

	private boolean isInInferno()
	{
		return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION_ID);
	}

	private boolean isInMorUlRek()
	{
		if (client.getMapRegions() == null)
		{
			return false;
		}

		int[] currentMapRegions = client.getMapRegions();

		// Verify that all regions exist in MOR_UL_REK_REGIONS
		for (int region : currentMapRegions)
		{
			if (!MOR_UL_REK_REGION_IDS.contains(region))
			{
				return false;
			}
		}

		return true;
	}

	private boolean isInVoid()
	{
		if (client.getMapRegions() == null)
		{
			return false;
		}

		int[] currentMapRegions = client.getMapRegions();

		for (int region : currentMapRegions)
		{
			if (!VOID_REGION_IDS.contains(region))
			{
				return false;
			}
		}

		return true;
	}
}