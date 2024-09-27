package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.client.exception.BaseException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Slf4j
public class StopClient {

	private static final boolean LOG_ENABLED = true;
	private static final String LOG_LEVEL = "DEBUG";

	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 12345;

	public static void main(String[] args) {
		// TODO: test if server still reachable

		configure();

		try (var socket = new Socket(SERVER_ADDRESS, SERVER_PORT); var player = new Player(socket, SERVER_PORT)) {
			var game = new Game(player);
			game.start();
			log.info("The joy is over, see you space cowboy");
		} catch (IOException e) {
			log.error("An exception occurred", e);
		} catch (BaseException e) {
			// That's fine
			log.error(e.getMessage());
		}
	}

	private static void configure() {
		final var rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		if (!LOG_ENABLED) {
			rootLogger.setLevel(Level.OFF);
		} else {
			rootLogger.setLevel(Level.toLevel(LOG_LEVEL));
		}

	}
}
