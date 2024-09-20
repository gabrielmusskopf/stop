package br.com.gabrielmusskopf.stop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
	PLAYER_CONNECTED(1),
	WAITING_PLAYERS(2),
	GAME_STARTED(3),
	GAME_ENDED(4),
	CONNECTION_CLOSED(5),
	ROUND_STARTED(6),
	ROUND_FINISHED(7),
	CATEGORY_WORD(8),
	STOP(9);

	private final int code;

	public static MessageType fromCode(int code) {
		for (MessageType value : values()) {
			if (value.getCode() == code) {
				return value;
			}
		}
		throw new IllegalArgumentException();
	}

}
