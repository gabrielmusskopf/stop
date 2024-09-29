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
import br.com.gabrielmusskopf.stop.server.exception.UnexpectedGameEndException;
import br.com.gabrielmusskopf.stop.server.messages.MessageFactory;

@Slf4j
@Getter
public class Game implements Runnable {

	private static final int DEFAULT_ROUNDS = 2;

	private final LetterGenerator letterGenerator = new LetterGenerator();
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
			broadcast(MessageFactory.gameEnded(rounds));
			disconnectAll();
			log.info("The game '{}' finished. Ongoing games: {}", identifier, GamePool.ongoingGamesCount());

		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		} catch (UnexpectedGameEndException e) {
			log.error("Game was finished unexpectedly");
		}
	}

	private void gameLoop() throws InterruptedException, IOException {
		while (GameState.RUNNING.equals(gameState) && hasRounds()) {
			var round = nextRound();
			log.info("A round {} started", currentRound);

			broadcast(MessageFactory.roundStarted(round.getLetter()));
			round.start(); // block until round finish
			log.info("Round {} ended", currentRound);

			round.computePoints();
			round.getPlayersPoints().forEach((player, points) -> log.info("Player {} points: {}", player.getName(), points.getAnswersPoints()));

			if (round.isStopRequested()) {
				var stopRequestPlayer = round.getStopRequestedPlayer();
				log.info("Round {} ended with stop from {}", currentRound, stopRequestPlayer.getName());

				var message = MessageFactory.stopRequested(stopRequestPlayer);
				broadcast(message);
			}

			broadcast(MessageFactory.roundFinished(round));
		}
	}

	private void broadcast(Message message) throws IOException {
		boolean hasError = false;

		try {
			if (player1 != null) player1.send(message);
		} catch (IOException e) {
			log.error("Error when broadcasting to {}.", player1.getName());
			hasError = true;
		}

		try {
			if (player2 != null) player2.send(message);
		} catch (IOException e) {
			log.error("Error when broadcasting to {}.", player2.getName());
			hasError = true;
		}

		if (hasError) {
			endUnexpetedly();
		}
	}

	private void endUnexpetedly() {
		gameState = GameState.FINISHED;
		GamePool.endGame();
		sendToSurvivors(MessageFactory.closeConnection());
		throw new UnexpectedGameEndException();
	}

	private void sendToSurvivors(Message message) {
		try {
			if (player1 != null && player1.isConnected()) player1.send(message);
		} catch (IOException ignored) {
		}
		try {
			if (player2 != null && player2.isConnected()) player2.send(message);
		} catch (IOException ignored) {
		}
	}

	public boolean hasEnoughPlayers() {
		return player1 != null && player2 != null;
	}

	public boolean isStillWaiting() {
		if (GameState.WAITING_PLAYER.equals(gameState)) {
			return hasOneConnectedPlayer();
		}
		return false;
	}

	public boolean isEmpty() {
		return player1 == null && player2 == null;
	}

	public boolean hasSomeConnectedPlayer() {
		return isPlayer1Connected() || isPlayer2Connected();
	}

	public boolean hasOneConnectedPlayer() {
		return (isPlayer1Connected() && !isPlayer2Connected()) || (!isPlayer1Connected() && isPlayer2Connected());
	}

	public boolean isPlayer1Connected() {
		return player1 != null && player1.isConnected();
	}

	public boolean isPlayer2Connected() {
		return player2 != null && player2.isConnected();
	}

	private void disconnectAll() {
		// TODO: finish the game when someone disconnect
		if (player1 != null) {
			if (player1.isConnected()) {
				player1.gracefullyDisconnect();
			} else {
				player1.close();
			}
		}
		if (player2 != null) {
			if (player2.isConnected()) {
				player2.gracefullyDisconnect();
			} else {
				player2.close();
			}
		}
	}

	public boolean hasRounds() {
		return rounds.size() < roundsCount;
	}

	public Round nextRound() {
		currentRound++;
		var round = new Round(letterGenerator.generate(), roundsCount, categories, player1, player2);
		this.rounds.add(round);
		return round;
	}

	public void removeAllPlayers() {
		if (player1 != null) {
			player1.close();
			player1 = null;
		}
		if (player2 != null) {
			player2.close();
			player2 = null;
		}
	}

	public void removePlayer(Player player) {
		if (player1.equals(player)) {
			player1.gracefullyDisconnect();
			player1 = null;
		} else if (player2.equals(player)) {
			player2.gracefullyDisconnect();
			player2 = null;
		}
	}

}
