package br.com.gabrielmusskopf.stop.server.exception;

import java.text.MessageFormat;

public abstract class BaseException extends RuntimeException {

	public BaseException() {
		super();
	}

	public BaseException(String message) {
		super(message);
	}

	public BaseException(String message, Object... params) {
		super(MessageFormat.format(message, params));
	}

}
