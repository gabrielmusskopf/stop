package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
		System.out.println("Bem-vindo ao STOP"); //TODO banana lib to make this pretty \o/

		// here the player is already connected to a game
		while (true) {
			// game state machine
			var msg = RawMessage.readRawMessageOrUnknown(player);
			switch (msg.getType()) {
				// TODO: Receive game score
				case PLAYER_CONNECTED -> {
					// player restart socket connection
				}
				case UNKNOWN -> {
					// read timed out
				}
				case WAITING_PLAYERS -> {
					log.info("Waiting another player to join");
					System.out.println("Aguardando outro jogador para iniciar a partida");
				}
				case GAME_STARTED -> {
					var gsm = new GameStartedMessage(msg.getData());
					log.info("Game has started. The categories are {}", gsm.getCategories());
					System.out.printf("O jogo iniciou, as categorias são %s", gsm.getCategoriesPretty());
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
				}
				case GAME_ENDED -> {
					System.out.println("O jogo encerrou. Obrigado por jogar :)");
					player.close();
					return;
				}
				case CONNECTION_CLOSED -> {
					// was not possible to connect again
					log.info("Client was disconnected by the server");
					System.out.println("Você foi desconectado pelo servidor, lamento...");
					player.close();
					return;
				}
				default -> log.warn("Unexpected message of type {}. Ignoring it.", msg.getType());
			}
		}
	}

	// wait for the server to confirm if the client was able to join a game
	private void waitServerConfirmation() throws IOException {
		int tries = 5;
		int c = 0;
		final int waitSeconds = 1;
		var msg = RawMessage.unknown();

		while (c < tries) {
			log.debug("Waiting connect confimation {}/{}", c + 1, tries);
			msg = RawMessage.readRawMessageOrUnknown(player);
			if (!MessageType.UNKNOWN.equals(msg.getType())) {
				log.debug("Connected {}/{}", c + 1, tries);
				break;
			}
			gentleSleep(waitSeconds);
			c++;
		}

		if (MessageType.UNKNOWN.equals(msg.getType())) {
			throw new ConnectionClosedException("Could not connect to server after {} tries", c);
		}

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

	private void gentleSleep(int waitSeconds) {
		try {
			Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}
