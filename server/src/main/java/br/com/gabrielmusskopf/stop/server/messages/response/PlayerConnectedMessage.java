package br.com.gabrielmusskopf.stop.server.messages.response;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.server.MessageType;
import br.com.gabrielmusskopf.stop.server.messages.Message;
import br.com.gabrielmusskopf.stop.server.messages.MessageBuilder;

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

	private final ResponseStatus status;

	public byte[] serialize() {
		return MessageBuilder.of(MessageType.PLAYER_CONNECTED)
				.put(status.getCode())
				.build();
	}

}
