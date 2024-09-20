package br.com.gabrielmusskopf.stop.client.exception;

import org.slf4j.helpers.MessageFormatter;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BaseException extends RuntimeException {

	public BaseException(String message, Object... params) {
		super(MessageFormatter.arrayFormat(message, params).getMessage());
	}

}
