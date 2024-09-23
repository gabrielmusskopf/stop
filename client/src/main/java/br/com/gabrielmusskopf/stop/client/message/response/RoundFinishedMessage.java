package br.com.gabrielmusskopf.stop.client.message.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.BytesUtils;
import br.com.gabrielmusskopf.stop.Category;

/*
Header:
- msg length (4 bytes)
- type (4 bytes)
Body:
- round number				(4 bytes)
- category code 			(4 bytes)<--
- player 1 answer length	(4 bytes)  |
- player 1 answer			(n bytes)  |
- player 2 answer length	(4 bytes)  |
- player 2 answer			(n bytes)  |
- player 1 points			(4 bytes)  |
- player 2 points			(4 bytes)---
 */
@Getter
@RequiredArgsConstructor
public class RoundFinishedMessage {

	private final Map<Category, List<PlayerAnswerPoints>> playerAnswers = new HashMap<>();
	private final int number;

	public RoundFinishedMessage(byte[] data) {
		number = data[0];

		int i = 1;
		while (i < data.length) {
			var category = Category.singleFrom(data[i]);

			var p1AnswerLength = data[++i];
			var p1Answer = BytesUtils.readString(data, p1AnswerLength, ++i);
			i += p1AnswerLength - 1;

			var p2AnswerLength = data[++i];
			var p2Answer = BytesUtils.readString(data, p2AnswerLength, ++i);
			i += p2AnswerLength - 1;

			var p1Points = data[++i];
			var p2Points = data[++i];

			i++;

			var p1 = new PlayerAnswerPoints(p1Answer, p1Points);
			var p2 = new PlayerAnswerPoints(p2Answer, p2Points);

			playerAnswers.put(category, List.of(p1, p2));
		}
	}

	public record PlayerAnswerPoints(String answer, int points) {
	}

}
