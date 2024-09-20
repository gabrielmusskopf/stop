package br.com.gabrielmusskopf.stop.server.messages;

import java.util.List;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageStatus;
import br.com.gabrielmusskopf.stop.server.messages.request.ConnectionClosedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.GameEndedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.GameStartedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.RoundFinishedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.RoundStartedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.WaitingPlayersMessage;
import br.com.gabrielmusskopf.stop.server.messages.response.PlayerConnectedMessage;

public abstract class MessageFactory {

	public static Message playerConnectedSuccessfully() {
		return new PlayerConnectedMessage(MessageStatus.OK);
	}

	public static Message waitingPlayer() {
		return new WaitingPlayersMessage();
	}

	// TODO: move to another message
	public static Message gameStarted(List<Category> categories) {
		return new GameStartedMessage(categories);
	}

	public static Message gameEnded() {
		return new GameEndedMessage();
	}

	public static Message closeConnection() {
		return new ConnectionClosedMessage();
	}

	public static Message roundStarted(char letter) {
		return new RoundStartedMessage(letter);
	}

	public static Message roundFinished() {
		return new RoundFinishedMessage();
	}

}
