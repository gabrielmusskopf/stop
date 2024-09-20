package br.com.gabrielmusskopf.stop.client.message.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageBuilder;
import br.com.gabrielmusskopf.stop.MessageType;

/*
Header:
- msg length (4 bytes)
- type (4 bytes)
Body:
- category (4 byte)
- word bytes (4 byte)
- word
 */
@Getter
@RequiredArgsConstructor
public class SendWordMessage implements Message {

	private final Category category;
	private final String word;

	@Override
	public byte[] serialize() {
		return MessageBuilder.of(MessageType.CATEGORY_WORD)
				.put(Category.fromCategories(category))
				.put(word.length())
				.put(word.getBytes())
				.build();
	}
}
