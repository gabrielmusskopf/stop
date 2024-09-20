package br.com.gabrielmusskopf.stop.server.messages.request;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.server.MessageType;
import br.com.gabrielmusskopf.stop.server.messages.Message;
import br.com.gabrielmusskopf.stop.server.messages.MessageBuilder;

/*
Header:
- msg length (1 byte)
- type (1 byte)
Body:
(empty)
 */
@RequiredArgsConstructor
public class ConnectionClosedMessage implements Message {

	public byte[] serialize() {
		return MessageBuilder.of(MessageType.CONNECTION_CLOSED).build();
	}

}
