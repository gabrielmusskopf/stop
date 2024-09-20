package br.com.gabrielmusskopf.stop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
