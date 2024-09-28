package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.RawMessage;
import br.com.gabrielmusskopf.stop.client.exception.ConnectionClosedException;
import br.com.gabrielmusskopf.stop.client.message.MessageFactory;
import br.com.gabrielmusskopf.stop.client.message.response.RoundFinishedMessage;

@Slf4j
@Getter
@RequiredArgsConstructor
public class Round {

	private final char letter;
	private final Player player;
	private final Map<Integer, Category> categories;
	private final Map<Category, String> answers;
	private final UserTerminal userTerminal;

	public Round(char letter, Player player, List<Category> categories) {
		this.letter = letter;
		this.player = player;
		this.answers = new HashMap<>();
		this.categories = IntStream
				.range(0, categories.size())
				.boxed()
				.collect(Collectors.toMap(i -> i + 1, categories::get));
		this.userTerminal = new UserTerminal(this::clientRoundLoop);
	}

	public void start() throws IOException {
		// round loop logic
		log.info("A new round started! Letter is '{}'", letter);

		System.out.println("\nDigite o número da categoria, e após isso, a palavra");
		System.out.println("Digite 'S' para solicitar STOP\n");
		System.out.printf("Letra da rodada: '%s'\n\n", letter);
		categories.forEach((number, category) -> System.out.printf("%d. %s%s", number, category, " ".repeat(8)));
		System.out.println();

		// lister server messages
		while (true) {
			var msg = RawMessage.readRawMessageOrUnknown(player);
			// TODO: broadcast when someone request a valid STOP
			switch (msg.getType()) {
				case UNKNOWN -> {
					// read timed out
				}
				case ROUND_FINISHED -> {
					log.info("Round finished");
					userTerminal.stop();
					printAnswers(msg);
					return;
				}
				case GAME_ENDED, CONNECTION_CLOSED -> {
					log.info("Game ended or client was disconnected by the server.");
					userTerminal.stop();
					throw new ConnectionClosedException();
				}
				default -> log.error("Unexpected {} message. Ignoring.", msg.getType());
			}
		}
	}

	private void clientRoundLoop(UserAction action) {
		try {
			switch (action) {
				case SendWordUserAction a -> sendWord(a);
				case StopUserAction a -> requestStop();
				default -> log.warn("unknown action");
			}
		} catch (IOException e) {
			log.error("IOException", e);
		}
	}

	private void printAnswers(RawMessage msg) {
		var roundFinishedMessage = new RoundFinishedMessage(msg.getData());

		// TODO: make columnSize variable
		int columnSize = 16;
		System.out.printf("\nRodada %d finalizou\n", roundFinishedMessage.getNumber());
		System.out.printf("\n%sJogador 1%sJogador 2\n", " ".repeat(columnSize), " ".repeat(columnSize));

		roundFinishedMessage.getPlayerAnswers().forEach((category, answers) -> {
			var categorySpaced = "%s%s".formatted(category, " ".repeat(columnSize - category.name().length()));
			var categoryAnswers = answers.stream()
					.map(a -> "%s%s%s".formatted(
							a.answer(),
							" ".repeat(columnSize - a.answer().length()),
							StringUtils.leftPad(String.valueOf(a.points()), 2, "0"))) // length 12
					.collect(Collectors.joining(" ".repeat(6)));

			System.out.printf("%s%s\n", categorySpaced, categoryAnswers);
		});
		System.out.println();
	}

	private void sendWord(SendWordUserAction action) throws IOException {
		if (!categories.containsKey(action.getCategory())) {
			System.out.printf("Categoria %s desconhecida\n", action.getCategory());
			return;
		}
		var wordLetter = action.getWord().toLowerCase().charAt(0);
		// FIXME: letter still the same from the previous round
		if (wordLetter != letter) {
			System.out.printf("Letra inicial inválida: %c\n", wordLetter);
		}

		var category = categories.get(action.getCategory());

		var message = MessageFactory.sendWord(category, action.getWord());
		player.send(message);
		answers.put(category, action.getWord());
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
		userTerminal.pause();
		System.out.println("""
				_________________              ______
				___  /_  ___/_  /_________________  /
				__  /_____ \\_  __/  __ \\__  __ \\_  /
				 /_/ ____/ // /_ / /_/ /_  /_/ //_/
				(_)  /____/ \\__/ \\____/_  .___/(_)
				                       /_/
				""");
	}

}
