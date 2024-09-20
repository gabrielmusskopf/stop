package br.com.gabrielmusskopf.stop.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.client.exception.ConnectionClosedException;
import br.com.gabrielmusskopf.stop.client.exception.UnexpectedMessageException;
import br.com.gabrielmusskopf.stop.client.message.GameStartedMessage;
import br.com.gabrielmusskopf.stop.client.message.MessageType;
import br.com.gabrielmusskopf.stop.client.message.PlayerConnectedMessage;
import br.com.gabrielmusskopf.stop.client.message.RequestStatus;
import br.com.gabrielmusskopf.stop.client.message.RoundStartedMessage;

@Slf4j
public class Client {

	private final Socket socket;
	private final BufferedInputStream in;
	private final PrintWriter out;
	private boolean isConnected;

	public Client(Socket socket) throws IOException {
		this.socket = socket;
		this.in = new BufferedInputStream(socket.getInputStream());
		this.out = new PrintWriter(socket.getOutputStream(), true);
	}

	public void start() throws IOException {
		log.info("Connected to Stop server");

		waitServerConfirmation();
		log.info("Client is connected to a game");

		// here the player is already connected to a game
		while (isConnected) {
			// client state machine
			var msg = readRawMessage();
			switch (msg.getType()) {
				case WAITING_PLAYERS -> log.info("Waiting another player to join");
				case GAME_STARTED -> {
					var gsm = new GameStartedMessage(msg.getData());
					log.info("Game has started. The categories are {}", gsm.getCategories());
				}
				case GAME_ENDED -> log.info("Game has ended. Thanks for playing :)");
				case ROUND_STARTED -> {
					var m = new RoundStartedMessage(msg.getData());
					log.info("A new round started! Letter is '{}'", m.getLetter());
				}
				case CONNECTION_CLOSED -> {
					log.info("Client was disconnected by the server");
					isConnected = false;
				}
				default -> throw new UnexpectedMessageException();
			}
		}

		socket.close();
		log.info("The joy is over, see you space cowboy");
	}

	// wait for the server to confirm if the client was able to join a game
	private void waitServerConfirmation() {
		try {
			var msg = readRawMessage();

			if (MessageType.CONNECTION_CLOSED.equals(msg.getType())) {
				throw new ConnectionClosedException("Unexpected connection closure");
			}
			if (!MessageType.PLAYER_CONNECTED.equals(msg.getType())) {
				throw new UnexpectedMessageException("Client was expecting {}, but got {} message type", MessageType.PLAYER_CONNECTED, msg.getType());
			}

			var message = new PlayerConnectedMessage(msg.getData());
			if (!RequestStatus.OK.equals(message.getStatus())) {
				throw new UnexpectedMessageException("Connection request was not successful");
			}

			isConnected = true;

		} catch (Exception e) {
			log.error(e.getMessage());
			isConnected = false;
		}
	}

	private RawMessage readRawMessage() throws IOException {
		var size = in.read();
		var typeCode = in.read();
		var data = new byte[size - 2]; // without size and type
		in.read(data, 0, size - 2);

		return new RawMessage(size, typeCode, data);
	}

}
