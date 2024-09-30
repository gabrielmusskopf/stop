package br.com.gabrielmusskopf.stop.server.messages;

import java.util.List;

import br.com.gabrielmusskopf.stop.Category;
import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.MessageStatus;
import br.com.gabrielmusskopf.stop.server.Player;
import br.com.gabrielmusskopf.stop.server.Round;
import br.com.gabrielmusskopf.stop.server.messages.request.ConnectionClosedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.GameEndedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.GameStartedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.PingMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.PlayerConnectedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.RoundFinishedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.RoundStartedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.StopRequestedMessage;
import br.com.gabrielmusskopf.stop.server.messages.request.WaitingPlayersMessage;

public abstract class MessageFactory {

	public static Message playerConnectedSuccessfully() {
		return new PlayerConnectedMessage(MessageStatus.OK);
	}

	public static Message waitingPlayer() {
		return new WaitingPlayersMessage();
	}

	public static Message gameStarted(List<Category> categories, Player player1, Player player2) {
		return new GameStartedMessage(categories, player1, player2);
	}

	public static Message gameEnded(List<Round> rounds, Player player1, Player player2) {
		return new GameEndedMessage(rounds, player1, player2);
	}

	public static Message closeConnection() {
		return new ConnectionClosedMessage();
	}

	public static Message roundStarted(char letter) {
		return new RoundStartedMessage(letter);
	}

	public static Message roundFinished(Round round) {
		return new RoundFinishedMessage(round);
	}

	public static Message ping() {
		return new PingMessage();
	}

	public static Message stopRequested(Player player) {
		return new StopRequestedMessage(player);
	}

}
