package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.server.exception.IllegalGameException;

@Slf4j
@Getter
public class Game implements Runnable {

	private static final int DEFAULT_ROUNDS = 2;

	// jogador1 - categoria1 - palavra
	// jogador1 - categoria2 - palavra	private final Player player1;
	// private final Map<Player, Map<String, String>> categoryAnswers;

	private final int roundsCount;
	private final List<Round> rounds = new ArrayList<>();
	private final List<String> categories = List.of("nome", "cep");
	private final String identifier;
	private Player player1;
	private Player player2;
	private int currentRound = -1;
	private GameState gameState;

	public Game() {
		this(DEFAULT_ROUNDS);
	}

	public Game(int roundsCount) {
		this.gameState = GameState.WAITING_PLAYER;
		this.roundsCount = roundsCount;
		this.identifier = "game-" + RandomStringUtils.secure().nextAlphabetic(5);
	}

	public void playerJoin(Player player) {
		if (GameState.RUNNING.equals(gameState)) {
			throw new IllegalGameException("Game is already running");
		}
		if (player1 != null && player2 != null) {
			throw new IllegalGameException("Both players already joined");
		}

		if (player1 == null) {
			player1 = player;
		} else {
			player2 = player;
		}
	}

	@Override
	public void run() {
		try {
			boolean areEnoughPlayers = hasEnoughPlayers();
			if (!areEnoughPlayers) {
				log.info("A new game '{}' is waiting", identifier);
				broadcast("Not enough players to start the game, please wait");
			}

			while (!hasEnoughPlayers()) {
				// wait
			}

			log.info("The game '{}' is starting. Ongoing games: {}", identifier, GamePool.ongoingGamesCount());
			gameState = GameState.RUNNING;
			broadcast("Both players joined. Game will start now!");

			// game logic
			Thread.sleep(10 * 1000);

			gameState = GameState.FINISHED;
			GamePool.endGame();
			broadcast("Game is finished. Thanks for playing :)");
			disconnectAll();
			log.info("The game '{}' finished. Ongoing games: {}", identifier, GamePool.ongoingGamesCount());

		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void broadcast(String message) {
		if (player1 != null) player1.send(message);
		if (player2 != null) player2.send(message);
	}

	public boolean hasEnoughPlayers() {
		return player1 != null && player2 != null;
	}

	private void disconnectAll() throws IOException {
		if (player1 != null) player1.disconnect();
		if (player2 != null) player2.disconnect();
	}

	private char generateRoundLetter() {
		return 'a';
	}

	public void answer(Player player, String category, String word) {
		//		if (player == null || category == null || word == null) {
		//			throw new IllegalArgumentException();
		//		}
		//
		//		if (!this.categories.contains(category)) {
		//			throw new InvalidCategoryException();
		//		}
		//
		//		if (!isValidWord(word)) {
		//			throw new InvalidAnswerException();
		//		}
		//
		//		var playerAnswers = this.categoryAnswers.get(player);
		//		if (playerAnswers == null) {
		//			throw new InvalidPlayerException();
		//		}
		//
		//		var playerCategoryAnswer = playerAnswers.get(category);
		//		if (playerCategoryAnswer != null) {
		//			throw new InvalidCategoryException("Player {0} already answered category {1}", player);
		//		}
		//
		//		playerAnswers.put(category, word);
	}

	private boolean isValidWord(String word) {
		return word.toLowerCase().charAt(0) == getCurrentRound().getLetter();
	}

	public boolean hasRounds() {
		return rounds.size() < roundsCount;
	}

	public void nextRound() {
		currentRound++;
		this.rounds.add(new Round(generateRoundLetter()));
	}

	public int getCurrentRoundCount() {
		return rounds.size();
	}

	private Round getCurrentRound() {
		return rounds.get(currentRound);
	}
}
