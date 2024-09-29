package br.com.gabrielmusskopf.stop.server.messages.request;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageBuilder;
import br.com.gabrielmusskopf.stop.MessageType;
import br.com.gabrielmusskopf.stop.server.Player;

/*
Header:
- msg length (4 bytes)
- type (4 bytes)
Body:
- player length (4 bytes)
- player name (n bytes)
 */
@RequiredArgsConstructor
public class StopRequestedMessage implements Message {

	private final Player player;

	@Override
	public byte[] serialize() {
		return MessageBuilder.of(MessageType.STOP_REQUESTED)
				.put(player.getName().length())
				.put(player.getName())
				.build();
	}
}
