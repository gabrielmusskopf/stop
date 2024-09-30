package br.com.gabrielmusskopf.stop.server.messages.request;

import java.util.List;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageBuilder;
import br.com.gabrielmusskopf.stop.MessageType;
import br.com.gabrielmusskopf.stop.server.Player;
import br.com.gabrielmusskopf.stop.server.Round;

/*
Header:
- msg length 		(4 byte)
- type 				(4 byte)
Body:
- player 1 name length (4 bytes)
- player 1 name (4 bytes)
- player 2 name length (4 bytes)
- player 2 name (4 bytes)
- player 1 points	(4 bytes)
- player 2 points	(4 bytes)
 */
@RequiredArgsConstructor
public class GameEndedMessage implements Message {

	private final List<Round> rounds;
	private final Player player1;
	private final Player player2;

	public byte[] serialize() {
		var mb = MessageBuilder.of(MessageType.GAME_ENDED);

		mb.put(player1.getName().length());
		mb.put(player1.getName());
		mb.put(player2.getName().length());
		mb.put(player2.getName());

		int p1Points = 0;
		int p2Points = 0;

		for (Round round : rounds) {
			var playersPoints = round.getPlayersPoints();
			p1Points += playersPoints.get(player1).getPoints();
			p2Points += playersPoints.get(player2).getPoints();
		}

		mb.put(p1Points);
		mb.put(p2Points);

		return mb.build();
	}

}
