package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.RawMessage;
import br.com.gabrielmusskopf.stop.server.messages.response.WordReceivedMessage;

@Slf4j
@Getter
@RequiredArgsConstructor
public class RoundPlayer {

	private final List<Category> categories;
	private final Player player;
	private final Map<Category, String> answers;
	private boolean stop = false;

	public RoundPlayer(List<Category> categories, Player player) {
		this.categories = categories;
		this.player = player;
		this.answers = new HashMap<>();
	}

	public void loop() throws IOException {
		while (!stop) {
			final var msg = RawMessage.readRawMessage(player);
			switch (msg.getType()) {
				case CATEGORY_WORD -> receiveWord(msg);
				case STOP -> stop();
				default -> log.error("Unexpected client message received");
			}
		}
	}

	private void stop() {
		log.debug("Player {} requested stop", player.getHost());
		if (answers.size() == categories.size()) {
			stop = true;
			return;
		}
		var unanswered = categories.stream()
				.filter(c -> !answers.containsKey(c))
				.toList();

		System.out.printf("Não é possível, existem categorias não respondidas: %s\n", unanswered);
	}

	private void receiveWord(RawMessage msg) {
		var m = new WordReceivedMessage(msg.getData());
		log.debug("Word '{}' received for '{}' category", m.getWord(), m.getCategory());

		answers.put(m.getCategory(), m.getWord());
	}

}
