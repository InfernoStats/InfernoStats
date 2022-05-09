package com.infernostats;

import com.google.inject.Provides;
import com.infernostats.controller.ChatHandler;
import com.infernostats.controller.TickLossHandler;
import com.infernostats.controller.TimerHandler;
import com.infernostats.controller.WaveHandler;
import com.infernostats.events.*;
import com.infernostats.model.Wave;
import com.infernostats.model.WaveSplit;
import com.infernostats.model.WaveState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;

import java.util.ArrayList;

import static net.runelite.api.ItemID.FIRE_CAPE;
import static net.runelite.api.ItemID.INFERNAL_CAPE;

@PluginDescriptor(
		name = "Inferno Stats",
		description = "Track restoration specials during an inferno attempt.",
		tags = {"combat", "npcs", "overlay"},
		enabledByDefault = false
)
@Slf4j
public class InfernoStatsPlugin extends Plugin {
	private static final int INFERNO_REGION_ID = 9043;
	private static final int FIGHT_CAVES_REGION_ID = 9551;

	@Getter(AccessLevel.PACKAGE)
	private InfernoStatsPanel panel;

	@Getter(AccessLevel.PACKAGE)
	private NavigationButton navButton;

	@Inject
	private ChatHandler chatHandler;

	@Inject
	private TimerHandler timerHandler;

	@Inject
	private WaveHandler waveHandler;

	@Inject
	private TickLossHandler tickLossHandler;

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private ItemManager itemManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InfernoStatsOverlay overlay;

	@Inject
	private InfernoStatsConfig config;

	@Provides
	InfernoStatsConfig getConfig(ConfigManager configManager) {
		return configManager.getConfig(InfernoStatsConfig.class);
	}

	@Override
	protected void startUp() {
		panel = injector.getInstance(InfernoStatsPanel.class);
		navButton = NavigationButton.builder()
				.tooltip("Inferno Stats")
				.icon(ImageUtil.loadImageResource(getClass(), "/blob-square.png"))
				.priority(6)
				.panel(panel)
				.build();

		if (isInInferno() || !config.hide())
			clientToolbar.addNavigation(navButton);

		if (config.splitsOverlay())
			overlayManager.add(overlay);

		WaveSplit.UpdateTargetSplits(this.config);

		eventBus.register(waveHandler);
		eventBus.register(timerHandler);
		eventBus.register(chatHandler);
		eventBus.register(tickLossHandler);

		eventBus.register(TimerStartedEvent.class);
		eventBus.register(TimerPausedEvent.class);
		eventBus.register(TimerStoppedEvent.class);
	}

	@Override
	protected void shutDown() {
		overlayManager.remove(overlay);
		clientToolbar.removeNavigation(navButton);

		eventBus.unregister(TimerStartedEvent.class);
		eventBus.unregister(TimerPausedEvent.class);
		eventBus.unregister(TimerStoppedEvent.class);

		eventBus.unregister(tickLossHandler);
		eventBus.unregister(chatHandler);
		eventBus.unregister(timerHandler);
		eventBus.unregister(waveHandler);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(this.config.GROUP))
			return;

		switch (event.getKey())
		{
			case "hide":
				resetNav();
				break;
			case "showInfernoTimer":
			case "showFightCavesTimer":
				createTimer();
				break;
			case "splitsOverlay":
				if (config.splitsOverlay()) {
					overlayManager.add(overlay);
				} else {
					overlayManager.remove(overlay);
				}
		}

		WaveSplit.UpdateTargetSplits(this.config);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		switch (gameStateChanged.getGameState()) {
			case LOADING:
				if (timerHandler.getState() == TimerHandler.TimerState.RUNNING)
					if (!isInFightCaves() && !isInInferno()) {
						eventBus.post(new TimerStoppedEvent());
						eventBus.post(new WaveFinishedEvent(WaveState.FAILED));
					}
				resetNav();
				break;
			case LOGIN_SCREEN:
			case HOPPING:
				if (timerHandler.getState() == TimerHandler.TimerState.RUNNING)
					eventBus.post(new TimerPausedEvent());
				break;
		}
	}

	@Subscribe
	protected void onGameTick(GameTick e) {
		if (isInInferno())
			this.panel.UpdateWave();
	}

	@Subscribe
	protected void onWaveStartedEvent(WaveStartedEvent e) {
		Wave wave = e.getWave();
		if (wave.getId() == 1)
			this.panel.ClearWaves();
		this.panel.AddWave(wave);
	}

	@Subscribe
	protected void onWaveFinishedEvent(WaveFinishedEvent e) {
		this.panel.UpdateWave();
	}

	@Subscribe
	protected void onRunCompletedEvent(RunCompletedEvent e) {
		Wave wave = this.getCurrentWave();
		wave.setState(e.getState());

		this.panel.UpdateWave();
		this.waveHandler.WriteWaves();
	}

	@Subscribe
	protected void onTimerStartedEvent(TimerStartedEvent e) {
		createTimer();
	}

	@Subscribe
	protected void onTimerStoppedEvent(TimerStoppedEvent e) {
		removeTimer();
		overlay.reset();
	}

	private void resetNav() {
		if (isInInferno() || !config.hide())
			clientToolbar.addNavigation(navButton);
		else
			clientToolbar.removeNavigation(navButton);
	}

	private void createTimer() {
		removeTimer();

		if (config.showFightCavesTimer() && isInFightCaves())
			infoBoxManager.addInfoBox(new InfernoStatsTimer(itemManager.getImage(FIRE_CAPE), this));
		else if (config.showInfernoTimer() && isInInferno())
			infoBoxManager.addInfoBox(new InfernoStatsTimer(itemManager.getImage(INFERNAL_CAPE), this));
	}

	private void removeTimer() {
		infoBoxManager.removeIf(InfernoStatsTimer.class::isInstance);
	}

	public Wave getCurrentWave() {
		return waveHandler.getWave();
	}

	public ArrayList<Wave> getWaves() {
		return waveHandler.getWaves();
	}

	public TimerHandler.TimerState getTimerState() {
		return timerHandler.getState();
	}

	public long getRunDuration() {
		return timerHandler.getDuration();
	}

	public boolean isInInferno() {
		return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION_ID);
	}

	public boolean isInFightCaves() {
		return client.getMapRegions() != null && ArrayUtils.contains(client.getMapRegions(), FIGHT_CAVES_REGION_ID);
	}
}