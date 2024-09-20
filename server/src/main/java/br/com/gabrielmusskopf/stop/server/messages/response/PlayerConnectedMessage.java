package br.com.gabrielmusskopf.stop.server.messages.response;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.server.MessageType;
import br.com.gabrielmusskopf.stop.server.messages.Message;

/*
Header:
- msg length (1 byte)
- type (1 byte)
- status (1 byte)
Body:
(empty)
 */
@RequiredArgsConstructor
public class PlayerConnectedMessage implements Message {

	private final MessageType messageType = MessageType.PLAYER_CONNECTED;
	private final ResponseStatus status;

	public byte[] serialize() {
		var buffer = new byte[3];

		buffer[0] = 3;
		buffer[1] = (byte) messageType.getCode();
		buffer[2] = (byte) status.getCode();

		return buffer;
	}

}
