package br.com.gabrielmusskopf.stop.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Player {

	private final String name = "player";
	private final Socket socket;
	private final BufferedReader in;
	private final PrintWriter out;

	public Player(Socket socket) throws IOException {
		this.socket = socket;
		this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		this.out = new PrintWriter(this.socket.getOutputStream(), true);
	}

	public void send(String message) {
		out.println(message);
	}

	public void disconnect() throws IOException {
		socket.close();
	}

	public String getHost() {
		return socket.getInetAddress().getHostAddress();
	}

	//	@Override
	//	public void run() {
	//		try {
	//			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	//			out = new PrintWriter(clientSocket.getOutputStream(), true);
	//
	//			out.println("Bem-vindo ao jogo 'Stop'!");
	//
	//			String inputLine;
	//			while ((inputLine = in.readLine()) != null) {
	//				System.out.println("Jogador: " + inputLine);
	//				out.println("Você disse: " + inputLine);
	//			}
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} finally {
	//			try {
	//				clientSocket.close();
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//	}

}
