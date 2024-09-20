package br.com.gabrielmusskopf.stop.client.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestStatus {
	OK(0),
	FAIL(1);

	private final int code;

	public static RequestStatus fromCode(int code) {
		for (RequestStatus value : values()) {
			if (value.getCode() == code) {
				return value;
			}
		}
		throw new IllegalArgumentException();
	}

}
