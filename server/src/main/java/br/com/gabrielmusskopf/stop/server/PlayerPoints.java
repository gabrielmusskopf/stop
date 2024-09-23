package br.com.gabrielmusskopf.stop.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Score;

public class PlayerPoints {

	private final Map<Category, Score> answersPoints = new HashMap<>();

	public int get(Category category) {
		return Optional.ofNullable(answersPoints.get(category))
				.orElse(Score.ZERO)
				.getPoints();
	}

	public void put(Category category, Score score) {
		answersPoints.put(category, score);
	}

}
