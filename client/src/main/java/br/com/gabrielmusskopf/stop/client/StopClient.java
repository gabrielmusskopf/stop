package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.client.exception.BaseException;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;

@Slf4j
public class StopClient {

	private static final boolean LOG_ENABLED = true;
	private static final boolean LOG_TO_FILE = true;
	private static final boolean LOG_TO_CONSOLE = true;
	private static final Level LOG_LEVEL = Level.DEBUG;

	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 12345;

	public static void main(String[] args) {
		configure();
		log.info("PID {}", ProcessHandle.current().pid());

		log.info("===========================");
		log.info("       STOP SERVER");
		log.info("===========================");

		try (var socket = new Socket(SERVER_ADDRESS, SERVER_PORT); var player = new Player(socket, SERVER_PORT)) {
			System.out.println("""
					   _____ __
					  / ___// /_____  ____
					  \\__ \\/ __/ __ \\/ __ \\
					 ___/ / /_/ /_/ / /_/ /
					/____/\\__/\\____/ .___/
					              /_/
					""");
			var game = new Game(player);
			game.start();
			log.info("The joy is over, see you space cowboy");
		} catch (IOException e) {
			log.error("An exception occurred", e);
			System.out.println("Houve um problema com a conexão ao servidor");
		} catch (BaseException e) {
			// That's fine
			log.error(e.getMessage());
		}
	}

	private static void configure() {
		final var rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		if (!LOG_ENABLED) {
			rootLogger.setLevel(Level.OFF);
			return;
		}
		rootLogger.setLevel(LOG_LEVEL);
		configureAdapter(rootLogger, "CONSOLE", LOG_TO_CONSOLE);
		configureAdapter(rootLogger, "FILE", LOG_TO_FILE);
	}

	private static void configureAdapter(Logger logger, String name, boolean enable) {
		final var appender = switch (name) {
			case "CONSOLE" -> (ConsoleAppender<ILoggingEvent>) logger.getAppender(name);
			case "FILE" -> (FileAppender<ILoggingEvent>) logger.getAppender(name);
			default -> {
				log.error("Appender name must match what is defined in logback.xml");
				yield null;
			}
		};
		if (appender == null) {
			return;
		}

		final boolean isAttached = logger.isAttached(appender);
		if (enable && !isAttached) {
			logger.addAppender(appender);
		} else if (!enable && isAttached) {
			logger.detachAppender(appender);
		}

		if (isAttached) {
			log.debug("{} ({}) appender attached", appender.getName(), appender.getClass().getSimpleName());
		} else {
			log.debug("{} ({}) appender dettached", appender.getName(), appender.getClass().getSimpleName());
		}
	}
}
