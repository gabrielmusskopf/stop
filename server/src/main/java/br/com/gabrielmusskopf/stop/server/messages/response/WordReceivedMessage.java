package br.com.gabrielmusskopf.stop.server.messages.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Category;

/*
Header:
- msg length (4 byte)
- type (4 byte)
Body:
- category (4 byte)
- word bytes (4 byte)
- word
 */
@Getter
@RequiredArgsConstructor
public class WordReceivedMessage {

	private String word;
	private Category category;

	public WordReceivedMessage(byte[] bytes) {
		parseData(bytes);
	}

	private void parseData(byte[] data) {
		category = Category.singleFrom(data[0]);
		int wordSize = data[1];
		word = new String(readN(data, 2, wordSize));
	}

	private byte[] readN(byte[] data, int n, int starting) {
		byte[] buff = new byte[n - starting];
		for (int i = 0; i < n; i++) {
			buff[i] = data[starting + i];
		}
		return buff;
	}

}
