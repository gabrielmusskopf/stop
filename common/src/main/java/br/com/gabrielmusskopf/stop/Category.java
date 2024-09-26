package br.com.gabrielmusskopf.stop;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
	NAME(1, "Nome"),        //000000001
	OBJECT(2, "Objeto"),     //000000010
	LOCATION(4, "CEP"),    //000000100
	COLOR(8, "Cor"),        //000001000
	ANIMAL(16, "Animal"),        //000010000
	WORD(32, "Palavra");        //000100000

	private final int code;
	private final String name;

	public static Category singleFrom(int x) {
		var categories = from(x);
		if (categories.size() > 1) {
			throw new IllegalStateException();
		}
		return categories.getFirst();
	}

	public static List<Category> from(int x) {
		List<Category> categories = new ArrayList<>();
		for (Category value : Category.values()) {
			if ((value.getCode() & x) == value.getCode()) {
				categories.add(value);
			}
		}
		return categories;
	}

	public static int fromCategories(Category category) {
		return fromCategories(List.of(category));
	}

	public static int fromCategories(Iterable<Category> categories) {
		int value = 0;
		for (Category category : categories) {
			value += category.getCode();
		}
		return value;
	}

}
