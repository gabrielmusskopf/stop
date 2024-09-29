package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.util.Scanner;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserTerminal {

	private static final char SYMBOL = '>';

	private Thread worker;
	private boolean running = false;
	private boolean paused = false;

	public UserTerminal(Consumer<UserAction> actionConsumer) {
		run(actionConsumer);
	}

	public void stop() {
		running = false;
		worker.interrupt();
	}

	private void run(Consumer<UserAction> actionConsumer) {
		this.worker = new Thread(() -> {
			running = true;
			paused = false;
			try {
				var scanner = new Scanner(System.in);
				while (running && !Thread.currentThread().isInterrupted()) {
					if (!paused) {
						System.out.print(SYMBOL + " ");
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
			} catch (IOException e) {
				log.error(e.getMessage());
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				// Expected interruption by the main thread
				// println after the last ">"
				System.out.println();
			}
		});

		this.worker.start();
	}

	private void waitAvailable() throws IOException, InterruptedException {
		while (nothingAvailable()) {
		}
	}

	// used for keep thread cpu bounded instead of io bounded,
	// so the main thread can interrupt it
	private boolean nothingAvailable() throws IOException, InterruptedException {
		if (System.in.available() <= 0) {
			Thread.sleep(100);
		}
		return System.in.available() <= 0;
	}

}
