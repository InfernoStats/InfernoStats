package com.infernostats.events;

import com.infernostats.model.Wave;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WaveStartedEvent {
	Wave wave;
}
