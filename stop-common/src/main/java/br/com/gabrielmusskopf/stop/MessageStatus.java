package br.com.gabrielmusskopf.stop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageStatus {
	OK(0),
	FAIL(1);

	private final int code;

	public static MessageStatus fromCode(int code) {
		for (MessageStatus value : values()) {
			if (value.getCode() == code) {
				return value;
			}
		}
		throw new IllegalArgumentException();
	}

}
