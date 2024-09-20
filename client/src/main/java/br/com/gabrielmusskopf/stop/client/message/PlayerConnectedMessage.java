package br.com.gabrielmusskopf.stop.client.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.MessageStatus;

/*
Header:
- status (1 byte)
Body:
(empty)
 */
@Getter
@RequiredArgsConstructor
public class PlayerConnectedMessage {

	private final MessageStatus status;

	public PlayerConnectedMessage(byte[] data) {
		this.status = MessageStatus.fromCode(data[0]);
	}

}
