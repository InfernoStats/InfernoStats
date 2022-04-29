package com.infernostats.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimerStartedEvent {
	int offset;
}
