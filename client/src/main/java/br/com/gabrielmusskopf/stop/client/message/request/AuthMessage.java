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
- name bytes (4 byte)
- name (n bytes)
 */
@Getter
@RequiredArgsConstructor
public class AuthMessage implements Message {

	private final String name;

	@Override
	public byte[] serialize() {
		return MessageBuilder.of(MessageType.AUTH)
				.put(name.length())
				.put(name)
				.build();
	}
}
