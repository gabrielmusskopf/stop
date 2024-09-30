package br.com.gabrielmusskopf.stop.server.messages.request;

import java.util.List;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageBuilder;
import br.com.gabrielmusskopf.stop.MessageType;
import br.com.gabrielmusskopf.stop.server.Player;

/*
Header:
- msg length (4 bytes)
- type (4 bytes)
Body:
- categories (4 bytes) [logic addition to insert up to 8 categories, like 00111111]
- letter (1 byte)
- player 1 name length (4 bytes)
- player 1 name (n bytes)
- player 2 name length (4 bytes)
- player 2 name (n bytes)
 */
@RequiredArgsConstructor
public class GameStartedMessage implements Message {

	private final List<Category> categories;
	private final Player player1;
	private final Player player2;

	@Override
	public byte[] serialize() {
		return MessageBuilder.of(MessageType.GAME_STARTED)
				.put(Category.fromCategories(categories))
				.put(player1.getName().length())
				.put(player1.getName())
				.put(player2.getName().length())
				.put(player2.getName())
				.build();
	}
}
