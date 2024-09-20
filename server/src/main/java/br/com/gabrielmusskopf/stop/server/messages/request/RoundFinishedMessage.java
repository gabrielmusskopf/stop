package br.com.gabrielmusskopf.stop.server.messages.request;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageBuilder;
import br.com.gabrielmusskopf.stop.MessageType;

/*
Header:
- msg length (4 bytes)
- type (4 bytes)
Body:
(empty)
 */
@RequiredArgsConstructor
public class RoundFinishedMessage implements Message {

	@Override
	public byte[] serialize() {
		return MessageBuilder.of(MessageType.ROUND_FINISHED).build();
	}

}
