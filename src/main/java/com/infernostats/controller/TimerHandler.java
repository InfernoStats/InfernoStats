package com.infernostats.controller;

import com.infernostats.InfernoStatsConfig;
import com.infernostats.InfernoStatsPlugin;
import com.infernostats.events.TimerPausedEvent;
import com.infernostats.events.TimerStartedEvent;
import com.infernostats.events.TimerStoppedEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;

@Slf4j
@Getter
public class TimerHandler {

	public enum TimerState {
		RUNNING,
		PAUSED,
		STOPPED
	}

	private long duration;
	private TimerState state;

	private final InfernoStatsPlugin plugin;
	private final InfernoStatsConfig config;

	@Inject
	protected TimerHandler(InfernoStatsPlugin plugin, InfernoStatsConfig config) {
		this.plugin = plugin;
		this.config = config;
		this.duration = 0;
		this.state = TimerState.STOPPED;
	}

	@Subscribe
	protected void onTimerStartedEvent(TimerStartedEvent e) {
		if (this.state == TimerState.STOPPED)
			this.duration = e.getOffset();
		else
			this.duration += e.getOffset();

		this.state = TimerState.RUNNING;
		this.config.tzhaarTimerState(this.state);
	}

	@Subscribe
	protected void onTimerPausedEvent(TimerPausedEvent e) {
		this.state = TimerState.PAUSED;
		this.config.tzhaarTimerState(this.state);
		this.config.tzhaarDuration(this.duration);
	}

	@Subscribe
	protected void onTimerStoppedEvent(TimerStoppedEvent e) {
		this.state = TimerState.STOPPED;
		this.config.tzhaarTimerState(this.state);
		this.config.tzhaarDuration(this.duration);
	}

	@Subscribe
	protected void onGameTick(GameTick e) {
		if (this.state == TimerState.RUNNING)
			this.duration += 1;
	}
}
