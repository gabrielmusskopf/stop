package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.net.Socket;

public class StopClient {

	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 12345;

	public static void main(String[] args) throws IOException {
		try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT)) {
			var client = new Client(socket);
			client.start();
		}
	}
}
