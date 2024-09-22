package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.net.Socket;

import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.client.exception.BaseException;

@Slf4j
public class StopClient {

	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 12345;

	public static void main(String[] args) {
		// TODO: test if server still reachable
		try (var socket = new Socket(SERVER_ADDRESS, SERVER_PORT); var player = new Player(socket)) {
			var game = new Game(player);
			game.start();
			log.info("The joy is over, see you space cowboy");
		} catch (IOException e) {
			log.error("An exception occurred", e);
		} catch (BaseException e) {
			// That's fine
		}
	}
}
