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
import br.com.gabrielmusskopf.stop.client.message.response.GameEndedMessage;
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
		System.out.println("\nBem-vindo ao STOP");

		// here the player is already connected to a game
		while (true) {
			// game state machine
			var msg = RawMessage.readRawMessageOrUnknown(player);
			switch (msg.getType()) {
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
					var message = new GameStartedMessage(msg.getData());
					log.info("Game has started. The categories are {}", message.getCategories());
					System.out.printf(
							"O jogo com os jogadores '%s' e '%s' iniciou, as categorias são: %s\n",
							message.getPlayer1(),
							message.getPlayer2(),
							message.getCategoriesPretty());
					categories = message.getCategories();
				}
				case ROUND_STARTED -> {
					if (categories == null || categories.isEmpty()) {
						log.debug("Cannot start round without categories. Client will ignore message.");
						return;
					}
					var message = new RoundStartedMessage(msg.getData());
					var round = new Round(message.getLetter(), player, categories);
					round.start();
				}
				case GAME_ENDED -> {
					var message = new GameEndedMessage(msg.getData());
					endGame(message);
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

	private void endGame(GameEndedMessage message) throws IOException {
		System.out.println("O jogo encerrou. Placar:");

		final var p1Name = message.getPlayer1();
		final var p2Name = message.getPlayer2();
		final var p1Points = message.getPlayer1Points();
		final var p2Points = message.getPlayer2Points();

		int columnSize = 16;
		if (p1Name.length() > columnSize) {
			columnSize = p1Name.length();
		}
		if (p2Name.length() > columnSize) {
			columnSize = p2Name.length();
		}
		int slashesCount = 3;
		int spacesArroundSlashes = 4;
		System.out.println("-".repeat(slashesCount + spacesArroundSlashes + 2 * columnSize));
		System.out.printf(
				"| %s%s | %s%s |\n",
				p1Name,
				" ".repeat(columnSize - p1Name.length()),
				p2Name,
				" ".repeat(columnSize - p2Name.length())
		);
		System.out.println("-".repeat(slashesCount + spacesArroundSlashes + 2 * columnSize));
		System.out.printf(
				"| %s%s | %s%s |\n",
				p1Points,
				" ".repeat(columnSize - String.valueOf(p1Points).length()),
				p2Points,
				" ".repeat(columnSize - String.valueOf(p2Points).length())
		);
		System.out.println("-".repeat(slashesCount + spacesArroundSlashes + 2 * columnSize));

		if (p1Points == p2Points) {
			System.out.println("""
					 _____                       _
					| ____|_ __ ___  _ __   __ _| |_ ___
					|  _| | '_ ` _ \\| '_ \\ / _` | __/ _ \\
					| |___| | | | | | |_) | (_| | ||  __/
					|_____|_| |_| |_| .__/ \\__,_|\\__\\___|
									|_|
					""");
		} else {
			var winner = p1Points > p2Points ? p1Name : p2Name;
			if (player.getName().equals(winner)) {
				System.out.println("""
						__     __
						\\ \\   / /__ _ __   ___ ___ _   _
						 \\ \\ / / _ \\ '_ \\ / __/ _ \\ | | |
						  \\ V /  __/ | | | (_|  __/ |_| |
						   \\_/ \\___|_| |_|\\___\\___|\\__,_|
						""");
			} else {
				System.out.println("""
						 ____              _
						|  _ \\ ___ _ __ __| | ___ _   _
						| |_) / _ \\ '__/ _` |/ _ \\ | | |
						|  __/  __/ | | (_| |  __/ |_| |
						|_|   \\___|_|  \\__,_|\\___|\\__,_|
						""");
			}
		}

		System.out.println("\nObrigado por jogar :)");
		player.close();
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
