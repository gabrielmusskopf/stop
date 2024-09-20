package br.com.gabrielmusskopf.stop.server.messages.request;

import java.util.List;

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
- categories (4 bytes) [logic addition to insert up to 8 categories, like 00111111]
- letter (1 byte)
 */
@RequiredArgsConstructor
public class GameStartedMessage implements Message {

	private final List<Category> categories;

	@Override
	public byte[] serialize() {
		int c = categories.stream().map(Category::getCode).reduce(0, Integer::sum);

		return MessageBuilder.of(MessageType.GAME_STARTED)
				.put(c)
				.build();
	}
}
