package br.com.gabrielmusskopf.stop.server;

import java.util.HashMap;
import java.util.Map;

import br.com.gabrielmusskopf.stop.Category;

public class PlayerAnswers {

	private final Map<Category, String> answers = new HashMap<>();

	public int size() {
		return answers.size();
	}

	public boolean containAnswer(Category category) {
		return answers.containsKey(category);
	}

	public void put(Category category, String answer) {
		answers.put(category, answer);
	}

	public String get(Category category) {
		return answers.getOrDefault(category, "<empty>");
	}

}
