package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;

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

		System.out.println("Digite o número da categoria, e após isso, a palavra");
		categories.forEach((number, category) -> System.out.printf("%d. %s\n", number, category));

		// TODO: maybe use another thread to collect answer
		//  this way, when a round timeout happens, client will
		//  receive a message and deal with it right away
		var scanner = new Scanner(System.in);

		while (true) {
			System.out.print("* ");

			int categoryKey = scanner.nextInt();
			if (!categories.containsKey(categoryKey)) {
				System.out.printf("Categoria %s desconhecida", categoryKey);
				continue;
			}
			scanner.nextLine(); // skip the last \n

			var category = categories.get(categoryKey);
			var word = scanner.nextLine();

			var message = MessageFactory.sendWord(category, word);
			player.send(message);

			// TODO: stop request
		}

	}

}
