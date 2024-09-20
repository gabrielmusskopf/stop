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

	private static final int THREADS_NUMBER = 2;
	private static final int ROUND_TIMEOUT_SECONDS = 10;

	private final String id = "round-" + RandomStringUtils.secure().nextAlphabetic(5);

	private final char letter;
	private final List<Category> categories;
	private final Player player1;
	private final Player player2;
	private LocalDateTime startTime;

	public void start() throws InterruptedException {
		var executor = Executors.newFixedThreadPool(THREADS_NUMBER);
		List<Future<?>> futures = new ArrayList<>();

		final Object lock = new Object();
		final boolean[] finished = { false };
		startTime = LocalDateTime.now();
		log.debug("Starting round at {}", startTime);

		for (int i = 0; i < THREADS_NUMBER; i++) {
			final var player = getPlayer(i);
			final var future = executor.submit(() -> {
				try {
					log.debug("Starting {} thread and waiting for client {} messages", id, player.getHost());
					while (roundIsValid()) {
						final var msg = RawMessage.readRawMessage(player);
						switch (msg.getType()) {
							case CATEGORY_WORD -> validateWord(msg);
							default -> log.error("Unexpected client message received");
						}
					}
					synchronized (lock) {
						if (!finished[0]) {
							log.debug("Round {} thread finished. Finalizing the others because the round is over", id);
							finished[0] = true;

							for (Future<?> f : futures) {
								f.cancel(true);
							}
						}
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
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

	private void validateWord(RawMessage msg) {
		var m = new WordReceivedMessage(msg.getData());
		log.debug("Word '{}' received for '{}' category", m.getWord(), m.getCategory());
	}

	private boolean roundIsValid() {
		return LocalDateTime.now().isBefore(startTime.plusSeconds(ROUND_TIMEOUT_SECONDS));
	}

	private Player getPlayer(int i) {
		return i % 2 == 0 ? player1 : player2;
	}

}
