package br.com.gabrielmusskopf.stop.server;

import java.util.Map;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Score;

@RequiredArgsConstructor
public class PlayerPoints {

	private final Map<Category, Score> answersPoints;

	public int getPoints() {
		return answersPoints.values().stream()
				.map(Score::getPoints)
				.reduce(0, Integer::sum);
	}

}
