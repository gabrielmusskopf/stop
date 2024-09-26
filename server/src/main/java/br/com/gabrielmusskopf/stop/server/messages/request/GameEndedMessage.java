package br.com.gabrielmusskopf.stop.server.messages.request;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageBuilder;
import br.com.gabrielmusskopf.stop.MessageType;
import br.com.gabrielmusskopf.stop.server.Round;

/*
Header:
- msg length 		(4 byte)
- type 				(4 byte)
Body:
- player 1 points	(4 bytes)
- player 2 points	(4 bytes)
 */
@RequiredArgsConstructor
public class GameEndedMessage implements Message {

	private final List<Round> rounds;

	public byte[] serialize() {
		var mb = MessageBuilder.of(MessageType.GAME_ENDED);

		rounds.stream()
				.map(round -> round.getPlayersPoints().entrySet()
						.stream()
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								e -> e.getValue().getPoints())))
				.forEach(e -> e.forEach((player, point) -> mb.put(point)));

		return mb.build();
	}

}
