package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.MessageStatus;
import br.com.gabrielmusskopf.stop.MessageType;
import br.com.gabrielmusskopf.stop.RawMessage;
import br.com.gabrielmusskopf.stop.client.exception.ConnectionClosedException;
import br.com.gabrielmusskopf.stop.client.exception.UnexpectedMessageException;
import br.com.gabrielmusskopf.stop.client.message.response.GameStartedMessage;
import br.com.gabrielmusskopf.stop.client.message.response.PlayerConnectedMessage;
import br.com.gabrielmusskopf.stop.client.message.response.RoundStartedMessage;

@Slf4j
@RequiredArgsConstructor
public class Game {

	private final Player player;
	private List<Category> categories;

	public void start() throws IOException {
		log.info("Connected to Stop server");

		waitServerConfirmation();
		log.info("Client is connected to a game");

		// here the player is already connected to a game
		while (true) {
			// game state machine
			var msg = RawMessage.readRawMessage(player);
			switch (msg.getType()) {
				case WAITING_PLAYERS -> {
					log.info("Waiting another player to join");
				}
				case GAME_STARTED -> {
					var gsm = new GameStartedMessage(msg.getData());
					log.info("Game has started. The categories are {}", gsm.getCategories());
					categories = gsm.getCategories();
				}
				case ROUND_STARTED -> {
					if (categories == null || categories.isEmpty()) {
						log.debug("Cannot start round without categories. Client will ignore message.");
						return;
					}
					var m = new RoundStartedMessage(msg.getData());
					var round = new Round(m.getLetter(), player, categories);
					round.start();
					return;
				}
				case GAME_ENDED -> {
					log.info("Game has ended. Thanks for playing :)");
					return;
				}
				case CONNECTION_CLOSED -> {
					log.info("Client was disconnected by the server");
					return;
				}
				default -> log.error("Unexpected message of type {}. Ignoring it.", msg.getType());
			}
		}
	}

	// wait for the server to confirm if the client was able to join a game
	private void waitServerConfirmation() throws IOException {
		var msg = RawMessage.readRawMessage(player);

		if (MessageType.CONNECTION_CLOSED.equals(msg.getType())) {
			throw new ConnectionClosedException("Unexpected connection closure");
		}
		if (!MessageType.PLAYER_CONNECTED.equals(msg.getType())) {
			throw new UnexpectedMessageException("Client was expecting {}, but got {} message type", MessageType.PLAYER_CONNECTED, msg.getType());
		}

		var message = new PlayerConnectedMessage(msg.getData());
		if (!MessageStatus.OK.equals(message.getStatus())) {
			throw new UnexpectedMessageException("Connection request was not successful");
		}
	}

}
