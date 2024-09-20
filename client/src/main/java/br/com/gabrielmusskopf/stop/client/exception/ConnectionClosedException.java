package br.com.gabrielmusskopf.stop.client.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConnectionClosedException extends BaseException {

	public ConnectionClosedException(String message) {
		super(message);
	}

}
