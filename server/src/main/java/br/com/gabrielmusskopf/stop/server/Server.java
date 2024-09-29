package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.MessageType;
import br.com.gabrielmusskopf.stop.RawMessage;
import br.com.gabrielmusskopf.stop.server.messages.response.AuthMessage;

@Slf4j
@RequiredArgsConstructor
public class Server {

	private static final int PORT = 12345;
	private final GameLobby gameLobby;

	public void start() throws IOException {
		try (var server = new ServerSocket(PORT)) {
			log.info("Server is running on " + PORT);
			while (true) {
				// TODO: wait for some message with user details, like name
				final var playerSocket = server.accept();
				log.info("New client connected: {}", playerSocket.getInetAddress().getHostAddress());
				var player = authenticate(playerSocket);
				if (player == null) {
					log.debug("Could not authenticate player");
					continue;
				}
				log.debug("Connected with name: {}", player.getName());
				gameLobby.addClient(player);
			}
		}
	}

	private Player authenticate(Socket playerSocket) throws IOException {
		var player = new Player(playerSocket);

		int tries = 5;
		int attempt = 0;
		var msg = RawMessage.unknown();
		do {
			attempt++;
			msg = RawMessage.readRawMessageOrUnknown(player);
			if (!MessageType.UNKNOWN.equals(msg.getType())) {
				break;
			}
			try {
				Thread.sleep(TimeUnit.SECONDS.toMillis(2));
			} catch (InterruptedException ignored) {
			}
		} while (attempt < tries);

		if (!MessageType.AUTH.equals(msg.getType())) {
			player.close();
			log.debug("Expected {} but got {} message type", MessageType.AUTH, msg.getType());
			return null;
		}

		var message = new AuthMessage(msg.getData());
		player.setName(message.getName());

		return player;
	}

}
