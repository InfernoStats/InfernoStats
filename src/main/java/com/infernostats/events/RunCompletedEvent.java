package com.infernostats.events;

import com.infernostats.model.WaveState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class RunCompletedEvent {
	WaveState state;
  int duration;

  public RunCompletedEvent(WaveState state) {
    this.state = state;
    this.duration = -1;
  }

  public RunCompletedEvent(WaveState state, int duration) {
    this.state = state;
    this.duration = duration;
  }
}
