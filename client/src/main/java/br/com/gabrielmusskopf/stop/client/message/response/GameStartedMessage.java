package br.com.gabrielmusskopf.stop.client.message.response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.BytesUtils;
import br.com.gabrielmusskopf.stop.Category;

/*
Header:
(empty)
Body:
- categories (4 bytes) [logic addition to insert up to 8 categories, like 00111111]
- player 1 name length (4 bytes)
- player 1 name (n bytes)
- player 2 name length (4 bytes)
- player 2 name (n bytes)
 */
@Getter
@RequiredArgsConstructor
public class GameStartedMessage {

	private final List<Category> categories = new ArrayList<>();
	private final String player1;
	private final String player2;

	public GameStartedMessage(byte[] data) {
		int i = 0;
		int c = data[i];
		i++;

		for (Category value : Category.values()) {
			if ((value.getCode() & c) == value.getCode()) {
				categories.add(value);
			}
		}

		int player1NameLength = data[i];
		i++;
		this.player1 = new String(BytesUtils.readN(data, player1NameLength, i));
		i += player1NameLength;
		int player2NameLength = data[i];
		i++;
		this.player2 = new String(BytesUtils.readN(data, player2NameLength, i));
	}

	public String getCategoriesPretty() {
		return categories.stream()
				.map(Category::getName)
				.collect(Collectors.joining(", "));
	}

}
