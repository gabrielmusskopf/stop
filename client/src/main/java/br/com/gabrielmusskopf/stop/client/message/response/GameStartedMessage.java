package br.com.gabrielmusskopf.stop.client.message.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Category;

/*
Header:
(empty)
Body:
- categories (4 bytes) [logic addition to insert up to 8 categories, like 00111111]
 */
@Getter
@RequiredArgsConstructor
public class GameStartedMessage {

	private final List<Category> categories = new ArrayList<>();

	public GameStartedMessage(byte[] data) {
		readCategories(data);
	}

	private void readCategories(byte[] data) {
		int c = data[0];

		for (Category value : Category.values()) {
			if ((value.getCode() & c) == value.getCode()) {
				categories.add(value);
			}
		}

	}

	public List<String> getCategoriesPretty() {
		return categories.stream()
				.map(Category::getName)
				.toList();
	}

}
