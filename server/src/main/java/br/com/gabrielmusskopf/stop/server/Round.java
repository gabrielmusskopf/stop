package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

		for (int i = 0; i < THREADS_NUMBER; i++) {
			int threadId = i + 1;
			final var player = getPlayer(i);
			Future<?> future = executor.submit(() -> {
				try {

					while (roundIsValid()) {
						final var msg = readRawMessage(player);
						switch (msg.getType()) {
							case WORD_RECEIVED -> validateWord(msg);
							default -> log.error("Unexpected client message received");
						}
					}

					// acabou

					synchronized (lock) {
						if (!finished[0]) {
							System.out.println("Thread " + threadId + " terminou primeiro! Finalizando as outras...");
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

		executor.shutdown();
		if (!executor.awaitTermination(ROUND_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
			System.out.println("Timeout! Finalizando todas as threads.");
			executor.shutdownNow();
		}

		System.out.println("Todas as threads foram finalizadas.");
	}

	private void validateWord(RawMessage msg) {
		var m = new WordReceivedMessage(msg.getData());
		log.info("Palavra recebida com sucesso: {} para a categoria {}", m.getWord(), m.getCategory());
	}

	private boolean roundIsValid() {
		return startTime.plusSeconds(ROUND_TIMEOUT_SECONDS).isBefore(LocalDateTime.now()); // and STOP requests
	}

	private Player getPlayer(int i) {
		return i % 2 == 0 ? player1 : player2;
	}

	private RawMessage readRawMessage(Player player) throws IOException {
		var size = player.read();
		var typeCode = player.read();
		var data = player.read(size - 2);

		return new RawMessage(size, typeCode, data);
	}


}
