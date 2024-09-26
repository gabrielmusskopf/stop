package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserTerminal {

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private static boolean userInteractionRunning = false;
	private static boolean paused = false;

	public static void start(Consumer<UserAction> actionConsumer) {
		if (!userInteractionRunning || executor.isTerminated()) {
			executor.submit(() -> userInteractionTask(actionConsumer));
		}
		if (paused) {
			paused = false;
		}
	}

	public static void pause() {
		paused = true;
	}

	public static void stop() {
		executor.shutdownNow();
	}

	private static void userInteractionTask(Consumer<UserAction> actionConsumer) {
		userInteractionRunning = true;
		try (var scanner = new Scanner(System.in)) {
			while (!Thread.currentThread().isInterrupted()) {
				if (!paused) {
					System.out.print("> ");
					paused = true;
				}
				if (nothingAvailable()) {
					continue;
				}
				paused = false;
				var action = scanner.next();

				if (StringUtils.isNumeric(action)) {
					waitAvailable();
					scanner.nextLine(); // skip the last \n
					waitAvailable();
					var word = scanner.nextLine();
					actionConsumer.accept(new SendWordUserAction(Integer.parseInt(action), word));
					continue;
				}
				switch (action) {
					case "S", "s" -> actionConsumer.accept(new StopUserAction());
					default -> System.out.printf("Opção %s desconhecida\n", action);
				}
			}
			userInteractionRunning = false;
		} catch (IOException e) {
			log.error(e.getMessage());
			userInteractionRunning = false;
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			// Expected interruption by the main thread
			// println after the last ">"
			System.out.println();
		}
	}

	private static void waitAvailable() throws IOException, InterruptedException {
		while (nothingAvailable()) {
		}
	}

	// used for keep thread cpu bounded instead of io bounded,
	// so the main thread can interrupt it
	private static boolean nothingAvailable() throws IOException, InterruptedException {
		if (System.in.available() <= 0) {
			Thread.sleep(100);
		}
		return System.in.available() <= 0;
	}

}
