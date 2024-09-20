package br.com.gabrielmusskopf.stop.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.server.messages.Message;
import br.com.gabrielmusskopf.stop.server.messages.MessageFactory;

@Slf4j
@RequiredArgsConstructor
public class Player {

	private final String name = "player";
	private final Socket socket;
	private final BufferedReader in;
	private final DataOutputStream out;

	public Player(Socket socket) throws IOException {
		this.socket = socket;
		this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		this.out = new DataOutputStream(this.socket.getOutputStream());
	}

	public void send(Message message) throws IOException {
		try {
			out.write(message.serialize());
			out.flush();
		} catch (IOException e) {
			log.error("Could not write message to client {}. Closing connection.", getHost());
			disconnect();
		}
	}

	public boolean isConnected() {
		return socket.isConnected();
	}

	public void disconnect() throws IOException {
		send(MessageFactory.closeConnection());
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
