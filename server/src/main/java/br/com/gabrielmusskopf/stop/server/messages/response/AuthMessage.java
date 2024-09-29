package br.com.gabrielmusskopf.stop.server.messages.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.BytesUtils;

/*
Header:
- msg length (4 byte)
- type (4 byte)
Body:
- name length (4 byte)
- name (n bytes)
 */
@Getter
@RequiredArgsConstructor
public class AuthMessage {

	private final String name;

	public AuthMessage(byte[] bytes) {
		int length = bytes[0];
		name = new String(BytesUtils.readN(bytes, length, 1));
	}

}
