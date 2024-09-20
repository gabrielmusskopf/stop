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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.RawMessage;
import br.com.gabrielmusskopf.stop.server.messages.response.WordReceivedMessage;

@Slf4j
@Getter
@RequiredArgsConstructor
public class Round {

	private static final int ROUND_PLAYERS_COUNT = 2;
	private static final int ROUND_TIMEOUT_SECONDS = 10;

	private final String id = "round-" + RandomStringUtils.secure().nextAlphabetic(5);

	private final char letter;
	private final List<Category> categories;
	private final Player player1;
	private final Player player2;
	private LocalDateTime startTime;

	private final Object lock = new Object();
	private final List<Future<?>> futures = new ArrayList<>();
	private boolean finished = false ;

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
		if (!executor.awaitTermination(ROUND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			log.debug("Round '{}' is over by timeout. Finalizing all players threads", id);
			executor.shutdownNow();
		}

		log.debug("All players threads are over for {}", id);
	}

	private void playerListenerTask(Player player) {
		try {
			log.debug("Starting {} thread and waiting for client {} messages", id, player.getHost());
			roundLoop(player);
			interruptOtherPlayers();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void roundLoop(Player player) throws IOException {
		while (roundIsValid()) {
			final var msg = RawMessage.readRawMessage(player);
			switch (msg.getType()) {
				case CATEGORY_WORD -> validateWord(msg);
				// TODO: stop request
				default -> log.error("Unexpected client message received");
			}
		}
	}

	private void interruptOtherPlayers() {
		synchronized (lock) {
			if (!finished) {
				log.debug("Round {} thread finished. Finalizing the others because the round is over", id);
				finished = true;
				futures.forEach(f -> f.cancel(true));
			}
		}
	}

	private void validateWord(RawMessage msg) {
		var m = new WordReceivedMessage(msg.getData());
		log.debug("Word '{}' received for '{}' category", m.getWord(), m.getCategory());
		// TODO: validate word
	}

	private boolean roundIsValid() {
		return LocalDateTime.now().isBefore(startTime.plusSeconds(ROUND_TIMEOUT_SECONDS));
	}

	private Player getPlayer(int i) {
		return i % 2 == 0 ? player1 : player2;
	}

}
