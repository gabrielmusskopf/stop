package br.com.gabrielmusskopf.stop.client.message.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
Header:
- msg length 		(4 byte)
- type 				(4 byte)
Body:
- player 1 points	(4 bytes)
- player 2 points	(4 bytes)
 */
@Getter
@RequiredArgsConstructor
public class GameEndedMessage {

	private final int player1Points;
	private final int player2Points;

	public GameEndedMessage(byte[] data) {
		this.player1Points = data[0];
		this.player2Points = data[1];
	}

}
