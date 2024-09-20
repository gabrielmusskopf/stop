package br.com.gabrielmusskopf.stop.server.messages;

import br.com.gabrielmusskopf.stop.server.messages.request.ConnectionClosedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.GameEndedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.GameStartingMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.WaitingPlayersMessage;
import br.com.gabrielmusskopf.stop.server.messages.response.PlayerConnectedMessage;
import br.com.gabrielmusskopf.stop.server.messages.response.ResponseStatus;

public abstract class MessageFactory {

	public static Message playerConnectedSuccessfully() {
		return new PlayerConnectedMessage(ResponseStatus.OK);
	}

	public static Message waitingPlayer() {
		return new WaitingPlayersMessage();
	}

	public static Message gameStarted() {
		return new GameStartingMessage();
	}

	public static Message gameEnded() {
		return new GameEndedMessage();
	}

	public static Message closeConnection() {
		return new ConnectionClosedMessage();
	}

}
