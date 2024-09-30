package br.com.gabrielmusskopf.stop.client.message.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
Header:
(empty)
Body:
- letter (1 byte)
 */
@Getter
@RequiredArgsConstructor
public class RoundStartedMessage {

	private final char letter;

	public RoundStartedMessage(byte[] data) {
		int i = 0;
		this.letter = (char) data[i];
	}

}
