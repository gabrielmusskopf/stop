package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Score;

@Slf4j
public class Round {

	private static final int ROUND_PLAYERS_COUNT = 2;
	private static final int ROUND_TIMEOUT_SECONDS = 60;

	private final String id = "round-" + RandomStringUtils.secure().nextAlphabetic(5);

	@Getter
	private final char letter;
	@Getter
	private final int number;
	@Getter
	private final List<Category> categories;
	private final Player player1;
	private final Player player2;
	@Getter
	private final Map<Player, PlayerAnswers> playersAnswers;
	@Getter
	private final Map<Player, PlayerPoints> playersPoints;

	private final List<Future<?>> futures = new ArrayList<>();

	private boolean finished = false;

	// lock 'stopRequested' after setting for the first time
	private boolean stopLock = false;
	@Getter
	private boolean stopRequested = false;
	@Getter
	private Player stopRequestedPlayer;

	public Round(char letter, int number, List<Category> categories, Player player1, Player player2) {
		this.letter = letter;
		this.number = number;
		this.categories = categories;
		this.player1 = player1;
		this.player2 = player2;
		this.playersAnswers = new HashMap<>();
		this.playersPoints = new HashMap<>();

		this.playersAnswers.put(player1, new PlayerAnswers());
		this.playersAnswers.put(player2, new PlayerAnswers());
		this.playersPoints.put(player1, new PlayerPoints());
		this.playersPoints.put(player2, new PlayerPoints());
	}

	// starts two threads to receive both answers at the same time
	public void start() throws InterruptedException {
		var executor = Executors.newFixedThreadPool(ROUND_PLAYERS_COUNT);

		final var startTime = LocalDateTime.now();
		log.debug("Starting round at {}", startTime);

		for (int i = 0; i < ROUND_PLAYERS_COUNT; i++) {
			final var player = getPlayer(i);
			final var future = executor.submit(() -> playerListener(player));
			futures.add(future);
		}

		log.info("Round '{}' started. This thread will wait until round timeout or some player ends", id);
		executor.shutdown();
		if (!executor.awaitTermination(ROUND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			log.debug("Round '{}' is over by timeout. Finalizing all players threads", id);
			executor.shutdownNow();
		}

		log.debug("All players threads are over for {}", id);
	}

	private void playerListener(Player player) {
		try {
			log.debug("Starting {} thread and waiting for client {} messages", id, player.getHost());

			var roundPlayer = new RoundPlayer(categories, player);
			boolean stop = roundPlayer.loop(); // block until stop or round timeout
			if (!stopLock && stop) {
				stopRequestedPlayer = player;
				stopRequested = true;
				stopLock = true;
			}

			playersAnswers.put(player, roundPlayer.getAnswers());

			log.debug("Round {} thread finished", id);
			interruptOtherPlayers();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void interruptOtherPlayers() {
		if (!finished) {
			log.debug("Finalizing the others because the round is over");
			finished = true;
			futures.forEach(f -> f.cancel(true));
		}
	}

	private Player getPlayer(int i) {
		return i % 2 == 0 ? player1 : player2;
	}

	public void computePoints() {
		if (playersAnswers.isEmpty()) {
			log.warn("Attempt to compute points for a unanswered round");
			return;
		}

		categories.forEach(category -> {
			computePlayerPoints(category, player1);
			computePlayerPoints(category, player2);
		});
	}

	private void computePlayerPoints(Category category, Player player) {
		var otherPlayer = player1.equals(player) ? player2 : player1;

		var playerAnswer = getAnswer(category, player);
		var otherPlayerAnswer = getAnswer(category, otherPlayer);
		log.debug("Computing {} points for {}", player.getName(), category);

		Score score;
		if (playerAnswer == null || playerAnswer.charAt(0) != letter) {
			// no answer for category or wrong letter
			score = Score.ZERO;
		} else if (playerAnswer.equalsIgnoreCase(otherPlayerAnswer)) {
			// do have a valid answer and the other answer the same
			score = Score.HALF;
		} else {
			// valid unique answer
			score = Score.FULL;
		}

		if (!playersPoints.containsKey(player)) {
			playersPoints.put(player, new PlayerPoints());
		}

		playersPoints.get(player).put(category, score);
	}

	private String getAnswer(Category category, Player player) {
		return Optional.ofNullable(playersAnswers.get(player))
				.map(p -> p.get(category))
				.map(String::toLowerCase)
				.orElse(null);
	}

}
