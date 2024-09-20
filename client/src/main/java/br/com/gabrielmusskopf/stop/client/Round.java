package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.RawMessage;
import br.com.gabrielmusskopf.stop.client.exception.ConnectionClosedException;
import br.com.gabrielmusskopf.stop.client.exception.GameEndedException;

@Slf4j
@Getter
@RequiredArgsConstructor
public class Round {

	private final char letter;
	private final Player player;
	private final Map<Integer, Category> categories;

	public Round(char letter, Player player, List<Category> categories) {
		this.letter = letter;
		this.player = player;
		this.categories = IntStream
				.range(0, categories.size())
				.boxed()
				.collect(Collectors.toMap(i -> i + 1, categories::get));
	}

	public void start() throws IOException {
		// round loop logic
		log.info("A new round started! Letter is '{}'", letter);

		var executor = Executors.newSingleThreadExecutor();
		executor.submit(this::sendUserAnswers);

		while (true) {
			var msg = RawMessage.readRawMessage(player);
			switch (msg.getType()) {
				case ROUND_FINISHED -> {
					log.info("Round finished");
					executor.shutdownNow();
					return;
				}
				case GAME_ENDED -> {
					log.info("Game ended.");
					executor.shutdownNow();
					throw new GameEndedException();
				}
				case CONNECTION_CLOSED -> {
					log.info("Client was disconnected by the server");
					executor.shutdownNow();
					throw new ConnectionClosedException();
				}
				default -> log.error("Unexpected {} message. Ignoring.", msg.getType());
			}
		}
	}

	private void sendUserAnswers() {
		System.out.println("Digite o número da categoria, e após isso, a palavra");
		categories.forEach((number, category) -> System.out.printf("%d. %s%s", number, category, " ".repeat(8)));
		System.out.println();

		try (var scanner = new Scanner(System.in)) {
			while (!Thread.interrupted()) {
				System.out.print("> ");

				waitInteraction();
				int categoryKey = scanner.nextInt();
				if (!categories.containsKey(categoryKey)) {
					System.out.printf("Categoria %s desconhecida\n", categoryKey);
					continue;
				}
				waitInteraction();
				scanner.nextLine(); // skip the last \n

				var category = categories.get(categoryKey);

				waitInteraction();
				var word = scanner.nextLine();

				var message = MessageFactory.sendWord(category, word);
				try {
					player.send(message);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				// TODO: stop request
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (InterruptedException e) {
			// Expected interruption by the main thread
			// println after the last ">"
			System.out.println();
		}
	}

	// used for keep thread cpu bounded instead of io bounded,
	// so the main thread can interrupt it
	private void waitInteraction() throws IOException, InterruptedException {
		while (System.in.available() <= 0) {
			Thread.sleep(100);
		}
	}

}
