package br.com.gabrielmusskopf.stop.client.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConnectionClosedException extends BaseException {

	public ConnectionClosedException(String message) {
		super(message);
	}

	public ConnectionClosedException(String message, Object... params) {
		super(message, params);
	}

}
