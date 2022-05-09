package com.infernostats.events;

import com.infernostats.model.WaveState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RunCompletedEvent {
	WaveState state;
}
