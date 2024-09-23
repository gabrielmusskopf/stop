package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.server.exception.IllegalGameException;
import br.com.gabrielmusskopf.stop.server.messages.MessageFactory;

@Slf4j
@Getter
public class Game implements Runnable {

	private static final int DEFAULT_ROUNDS = 1;

	private final List<Category> categories = List.of( // static for now
			Category.NAME, Category.ANIMAL
			//Category.ANIMAL, Category.COLOR, Category.WORD, Category.LOCATION, Category.OBJECT
	);

	private final int roundsCount;
	private final List<Round> rounds = new ArrayList<>();
	private final String identifier;
	private Player player1;
	private Player player2;
	private int currentRound = 0;
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
			log.info("The game '{}' is starting. Ongoing games: {}", identifier, GamePool.ongoingGamesCount());
			gameState = GameState.RUNNING;
			broadcast(MessageFactory.gameStarted(categories));

			gameLoop();

			gameState = GameState.FINISHED;
			GamePool.endGame();
			broadcast(MessageFactory.gameEnded());
			disconnectAll();
			log.info("The game '{}' finished. Ongoing games: {}", identifier, GamePool.ongoingGamesCount());

		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void gameLoop() throws InterruptedException, IOException {
		while (hasRounds()) {
			var round = nextRound();
			log.info("A round {} started", currentRound);

			broadcast(MessageFactory.roundStarted(round.getLetter()));
			round.start();
			round.computePoints();

			round.getPlayersPoints().forEach((player, points) -> log.info("Player {} points: {}", player.getName(), points));

			log.info("A round {} ended", currentRound);
			broadcast(MessageFactory.roundFinished(round));
		}
	}

	private void broadcast(Message message) throws IOException {
		if (player1 != null) player1.send(message);
		if (player2 != null) player2.send(message);
	}

	public boolean hasEnoughPlayers() {
		return player1 != null && player2 != null;
	}

	public boolean hasSomeConnectedPlayer() {
		if (player1 != null) return player1.isConnected();
		if (player2 != null) return player2.isConnected();
		return false;
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

	public Round nextRound() {
		currentRound++;
		var round = new Round(generateRoundLetter(), roundsCount, categories, player1, player2);
		this.rounds.add(round);
		return round;
	}

	public int getCurrentRoundCount() {
		return rounds.size();
	}

	private Round getCurrentRound() {
		return rounds.get(currentRound - 1);
	}
}
