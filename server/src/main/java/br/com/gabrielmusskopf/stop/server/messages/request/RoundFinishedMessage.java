package br.com.gabrielmusskopf.stop.server.messages.request;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageBuilder;
import br.com.gabrielmusskopf.stop.MessageType;
import br.com.gabrielmusskopf.stop.server.Round;

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
@RequiredArgsConstructor
public class RoundFinishedMessage implements Message {

	private final Round round;

	@Override
	public byte[] serialize() {
		var mb = MessageBuilder.of(MessageType.ROUND_FINISHED);

		mb.put(round.getNumber()); // round number

		round.getCategories().forEach(category -> {
			mb.put(category.getCode()); // category

			round.getPlayersAnswers().forEach((player, answers) -> {
				var playerCategoryAnswer = answers.get(category);
				mb.put(playerCategoryAnswer.length()); // answer length
				mb.put(playerCategoryAnswer); // answer
			});

			round.getPlayerPoints().forEach((player, points) -> {
				mb.put(points); // points
			});
		});

		return mb.build();
	}

}
