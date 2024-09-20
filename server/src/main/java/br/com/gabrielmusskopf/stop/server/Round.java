package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;

@Slf4j
@Getter
public class Round {

	private static final int ROUND_PLAYERS_COUNT = 2;
	private static final int ROUND_TIMEOUT_SECONDS = 60;

	private final String id = "round-" + RandomStringUtils.secure().nextAlphabetic(5);

	private final char letter;
	private final List<Category> categories;
	private final Player player1;
	private final Player player2;
	private LocalDateTime startTime;

	private final Object lock = new Object();
	private final List<Future<?>> futures = new ArrayList<>();
	private boolean finished = false ;

	public Round(char letter, List<Category> categories, Player player1, Player player2) {
		this.letter = letter;
		this.categories = categories;
		this.player1 = player1;
		this.player2 = player2;
	}

	// starts two threads to receive both answers at the same time
	public void start() throws InterruptedException {
		var executor = Executors.newFixedThreadPool(ROUND_PLAYERS_COUNT);

		startTime = LocalDateTime.now();
		log.debug("Starting round at {}", startTime);

		for (int i = 0; i < ROUND_PLAYERS_COUNT; i++) {
			final var player = getPlayer(i);
			final var future = executor.submit(() -> {
				playerListenerTask(player);
			});
			futures.add(future);
		}

		log.info("Round '{}' started. This thread will wait until round timeout or some player ends", id);
		executor.shutdown();
		// FIXME: even with a stop request, executor still waits for the timeout
		if (!executor.awaitTermination(ROUND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			log.debug("Round '{}' is over by timeout. Finalizing all players threads", id);
			executor.shutdownNow();
		}

		log.debug("All players threads are over for {}", id);
	}

	private void playerListenerTask(Player player) {
		try {
			log.debug("Starting {} thread and waiting for client {} messages", id, player.getHost());
			new RoundPlayer(categories, player).loop(); // blocking
			interruptOtherPlayers();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void interruptOtherPlayers() {
		if (!finished) {
			log.debug("Round {} thread finished. Finalizing the others because the round is over", id);
			finished = true;
			futures.forEach(f -> f.cancel(true));
		}
	}

	private Player getPlayer(int i) {
		return i % 2 == 0 ? player1 : player2;
	}

}
