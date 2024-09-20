package br.com.gabrielmusskopf.stop.server.messages.request;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.server.MessageType;
import br.com.gabrielmusskopf.stop.server.messages.Message;

/*
Header:
- msg length (1 byte)
- type (1 byte)
Body:
(empty)
 */
@RequiredArgsConstructor
public class WaitingPlayersMessage implements Message {

	private final MessageType messageType = MessageType.WAITING_PLAYERS;

	public byte[] serialize() {
		var buffer = new byte[2];

		buffer[0] = 2;
		buffer[1] = (byte) messageType.getCode();

		return buffer;
	}

}
