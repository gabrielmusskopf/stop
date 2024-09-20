package br.com.gabrielmusskopf.stop.client.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnexpectedMessageException extends BaseException {

	public UnexpectedMessageException(String message, Object... params) {
		super(message, params);
	}

}
