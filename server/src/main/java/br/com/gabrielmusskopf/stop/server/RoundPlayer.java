package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.MessageType;
import br.com.gabrielmusskopf.stop.RawMessage;
import br.com.gabrielmusskopf.stop.server.messages.response.WordReceivedMessage;

@Slf4j
@Getter
@RequiredArgsConstructor
public class RoundPlayer {

	private final List<Category> categories;
	private final Player player;
	private final PlayerAnswers answers;
	private boolean running = true;
	@Getter
	private boolean stop = false;

	public RoundPlayer(List<Category> categories, Player player) throws IOException {
		this.categories = categories;
		this.player = player;
		this.answers = new PlayerAnswers();
	}

	public boolean loop() throws IOException {
		while (!stop) {
			if (Thread.currentThread().isInterrupted()) {
				log.debug("Thread is interrupted");
				return false;
			}
			final var msg = RawMessage.readRawMessageOrUnknown(player);
			if (MessageType.UNKNOWN.equals(msg.getType())) {
				continue;
			}
			switch (msg.getType()) {
				case CATEGORY_WORD -> receiveWord(msg);
				case STOP -> stop();
				case PING -> { // hearbeat
				}
				default -> log.error("Unexpected client message received");
			}
		}
		return true;
	}

	private void stop() {
		log.debug("Player {} requested stop", player.getHost());
		if (answers.size() == categories.size()) {
			stop = true;
			return;
		}
		var unanswered = categories.stream()
				.filter(c -> !answers.containAnswer(c))
				.toList();

		System.out.printf("Não é possível, existem categorias não respondidas: %s\n", unanswered);
	}

	private void receiveWord(RawMessage msg) {
		var m = new WordReceivedMessage(msg.getData());
		log.debug("Word '{}' received for '{}' category", m.getWord(), m.getCategory());

		answers.put(m.getCategory(), m.getWord());
	}

}
