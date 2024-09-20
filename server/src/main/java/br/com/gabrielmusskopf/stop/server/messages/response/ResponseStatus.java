package br.com.gabrielmusskopf.stop.server.messages.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseStatus {
	OK(0),
	FAIL(1);

	private final int code;

}
