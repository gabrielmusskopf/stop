package br.com.gabrielmusskopf.stop.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.client.message.MessageType;

@Getter
@RequiredArgsConstructor
public class RawMessage {

	// common headers
	private final int size;
	private final MessageType type;

	// body
	private final byte[] data;

	public RawMessage(int size, int code, byte[] data) {
		this.size = size;
		this.type = MessageType.fromCode(code);
		this.data = data;
	}

}
