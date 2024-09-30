package br.com.gabrielmusskopf.stop.client.message.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.BytesUtils;

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
@Getter
@RequiredArgsConstructor
public class GameEndedMessage {

	private final String player1;
	private final String player2;
	private final int player1Points;
	private final int player2Points;

	public GameEndedMessage(byte[] data) {
		int i = 0;
		int p1NameLength = data[i];
		i++;

		this.player1 = BytesUtils.readString(data, p1NameLength, i);
		i += p1NameLength;

		int p2NameLength = data[i];
		i++;

		this.player2 = BytesUtils.readString(data, p2NameLength, i);
		i += p2NameLength;

		this.player1Points = data[i];
		i++;
		this.player2Points = data[i];
	}

}
