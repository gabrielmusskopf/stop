package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

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
	private final Map<Category, String> answers;

	public Round(char letter, Player player, List<Category> categories) {
		this.letter = letter;
		this.player = player;
		this.answers = new HashMap<>();
		this.categories = IntStream
				.range(0, categories.size())
				.boxed()
				.collect(Collectors.toMap(i -> i + 1, categories::get));
	}

	public void start() throws IOException {
		// round loop logic
		log.info("A new round started! Letter is '{}'", letter);

		System.out.println("\nDigite o número da categoria, e após isso, a palavra");
		System.out.println("Digite 'S' para solicitar STOP\n");
		categories.forEach((number, category) -> System.out.printf("%d. %s%s", number, category, " ".repeat(8)));
		System.out.println();

		var executor = Executors.newSingleThreadExecutor();
		executor.submit(this::userInteractionTask);

		while (true) {
			var msg = RawMessage.readRawMessageOrUnknown(player);
			switch (msg.getType()) {
				case UNKNOWN -> {
					// read timed out
				}
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

	private void userInteractionTask() {
		try (var scanner = new Scanner(System.in)) {
			while (!Thread.interrupted()) {
				System.out.print("> ");

				waitInteraction();
				var action = scanner.next();

				if (StringUtils.isNumeric(action)) {
					sendWord(action, scanner);
					continue;
				}
				switch (action) {
					case "S", "s" -> requestStop();
					default -> System.out.printf("Opção %s desconhecida", action);
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (InterruptedException e) {
			// Expected interruption by the main thread
			// println after the last ">"
			System.out.println();
		}
	}

	private void sendWord(String action, Scanner scanner) throws IOException, InterruptedException {
		int categoryKey = Integer.parseInt(action);
		if (!categories.containsKey(categoryKey)) {
			System.out.printf("Categoria %s desconhecida\n", categoryKey);
			return;
		}
		waitInteraction();
		scanner.nextLine(); // skip the last \n

		var category = categories.get(categoryKey);

		waitInteraction();
		var word = scanner.nextLine();

		var message = MessageFactory.sendWord(category, word);
		player.send(message);
		answers.put(category, word);
	}

	private void requestStop() throws IOException {
		if (answers.size() < categories.size()) {
			var unanswered = categories.values().stream()
					.filter(c -> !answers.containsKey(c))
					.toList();

			System.out.printf("Não é possível, existem categorias não respondidas: %s\n", unanswered);
			return;
		}
		var message = MessageFactory.stop();
		player.send(message);
		log.info("Player requested stop");
	}

	// used for keep thread cpu bounded instead of io bounded,
	// so the main thread can interrupt it
	private void waitInteraction() throws IOException, InterruptedException {
		while (System.in.available() <= 0) {
			Thread.sleep(100);
		}
	}

}
