package br.com.gabrielmusskopf.stop.client.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GameEndedException extends BaseException {

	public GameEndedException(String message) {
		super(message);
	}

}
