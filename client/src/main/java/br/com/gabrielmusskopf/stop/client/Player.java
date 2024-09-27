package br.com.gabrielmusskopf.stop.client;

import java.io.IOException;
import java.net.Socket;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.Readable;

@Slf4j
public class Player implements AutoCloseable, Readable {

	private final ConnectionHandler connectionHandler;
	@Getter
	private final String host;

	public Player(Socket socket, int originalPort) throws IOException {
		this.connectionHandler = new ConnectionHandler(socket, originalPort);
		this.host = socket.getInetAddress().getHostAddress();
	}

	public int read() throws IOException {
		return connectionHandler.read();
	}

	public byte[] read(int size) throws IOException {
		var buff = new byte[size];
		connectionHandler.read(buff, 0, size);
		return buff;
	}

	public void send(Message message) throws IOException {
		connectionHandler.send(message);
	}

	@Override
	public void close() throws IOException {
		connectionHandler.close();
	}

}
