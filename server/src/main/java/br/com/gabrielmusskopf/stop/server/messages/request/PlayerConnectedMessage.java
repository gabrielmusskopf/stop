package br.com.gabrielmusskopf.stop.server.messages.request;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageBuilder;
import br.com.gabrielmusskopf.stop.MessageStatus;
import br.com.gabrielmusskopf.stop.MessageType;

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

	private final MessageStatus status;

	public byte[] serialize() {
		return MessageBuilder.of(MessageType.PLAYER_CONNECTED)
				.put(status.getCode())
				.build();
	}

}
