package br.com.gabrielmusskopf.stop.server.messages.request;

import java.util.List;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.server.Category;
import br.com.gabrielmusskopf.stop.server.MessageType;
import br.com.gabrielmusskopf.stop.server.messages.Message;
import br.com.gabrielmusskopf.stop.server.messages.MessageBuilder;

/*
Header:
- msg length (4 bytes)
- type (4 bytes)
Body:
- categories (4 bytes) [logic addition to insert up to 8 categories, like 00111111]
 */
@RequiredArgsConstructor
public class GameStartedMessage implements Message {

	private final List<Category> categories = List.of( // static for now
			Category.NAME, Category.ANIMAL, Category.COLOR, Category.WORD, Category.LOCATION, Category.OBJECT
	);

	@Override
	public byte[] serialize() {
		int c = categories.stream().map(Category::getCode).reduce(0, Integer::sum);

		return MessageBuilder.of(MessageType.GAME_STARTED)
				.put(c)
				.build();
	}
}
