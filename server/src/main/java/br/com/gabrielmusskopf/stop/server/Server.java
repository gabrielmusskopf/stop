package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.net.ServerSocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class Server {

	private static final int PORT = 12345;
	private final GameLobby gameLobby;

	public void start() throws IOException {
		try (var server = new ServerSocket(PORT)) {
			log.info("Server is running on " + PORT);
			while (true) {
				final var playerSocket = server.accept();
				log.info("New client connected: {}", playerSocket.getInetAddress().getHostAddress());
				gameLobby.addClient(new Player(playerSocket));
			}
		}
	}

}
