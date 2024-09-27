package br.com.gabrielmusskopf.stop.client.message;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.client.message.request.BeatMessage;
import br.com.gabrielmusskopf.stop.client.message.request.SendWordMessage;
import br.com.gabrielmusskopf.stop.client.message.request.StopMessage;

public abstract class MessageFactory {

	public static Message sendWord(Category category, String word) {
		return new SendWordMessage(category, word);
	}

	public static Message stop() {
		return new StopMessage();
	}

	public static Message beat() {
		return new BeatMessage();
	}

}
