package br.com.gabrielmusskopf.stop.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class StopClient {

	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 12345;

	public static void main(String[] args) {
		try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
			 var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			 var out = new PrintWriter(socket.getOutputStream(), true);
			 var console = new BufferedReader(new InputStreamReader(System.in))) {

			System.out.println("Conectado ao servidor 'Stop'.");

			String serverMessage;
			while ((serverMessage = in.readLine()) != null) {
				System.out.println("Servidor: " + serverMessage);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
