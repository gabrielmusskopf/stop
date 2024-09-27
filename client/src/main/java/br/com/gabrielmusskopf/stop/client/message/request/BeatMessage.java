package br.com.gabrielmusskopf.stop.client.message.request;

import lombok.Getter;
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
@Getter
@RequiredArgsConstructor
public class BeatMessage implements Message {

	@Override
	public byte[] serialize() {
		return MessageBuilder.of(MessageType.PING).build();
	}
}
