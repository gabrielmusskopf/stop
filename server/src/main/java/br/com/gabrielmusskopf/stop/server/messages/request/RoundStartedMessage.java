package br.com.gabrielmusskopf.stop.server.messages.request;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.server.MessageType;
import br.com.gabrielmusskopf.stop.server.messages.Message;
import br.com.gabrielmusskopf.stop.server.messages.MessageBuilder;

/*
Header:
- msg length (4 bytes)
- type (4 bytes)
Body:
- letter (1 byte)
 */
@RequiredArgsConstructor
public class RoundStartedMessage implements Message {

	private final char letter;

	@Override
	public byte[] serialize() {
		return MessageBuilder.of(MessageType.ROUND_STARTED)
				.put(letter)
				.build();
	}
}
