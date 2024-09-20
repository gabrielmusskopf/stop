package br.com.gabrielmusskopf.stop.client.exception;

import br.com.gabrielmusskopf.stop.MessageType;

public class UnexpectedMessageException extends BaseException {

	public UnexpectedMessageException(String message, Object... params) {
		super(message, params);
	}

	public UnexpectedMessageException(MessageType type) {
		super("Message of type %s".formatted(type));
	}

}
