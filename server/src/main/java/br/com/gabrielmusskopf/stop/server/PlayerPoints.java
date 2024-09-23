package br.com.gabrielmusskopf.stop.server;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Category;

@RequiredArgsConstructor
public class PlayerPoints {

	private static final int STOP_REQUEST_POINTS = 10;

	private final Map<Category, Integer> answersPoints;
	private final boolean requestStop;

	public int getPoints() {
		int points = answersPoints.values().stream().reduce(0, Integer::sum);
		if (requestStop) {
			points += STOP_REQUEST_POINTS;
		}
		return points;
	}

	public Integer get(Category category) {
		return answersPoints.get(category);
	}

}
