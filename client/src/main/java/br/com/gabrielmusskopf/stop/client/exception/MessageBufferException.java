package br.com.gabrielmusskopf.stop.client.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MessageBufferException extends BaseException {

	public MessageBufferException(String message) {
		super(message);
	}

	public MessageBufferException(String message, Object... params) {
		super(message, params);
	}

}
