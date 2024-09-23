package br.com.gabrielmusskopf.stop;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Score {
	FULL(10),
	HALF(5),
	ZERO(0);

	private final int points;

}
