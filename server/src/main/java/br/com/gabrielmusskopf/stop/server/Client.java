package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import lombok.Getter;

@Getter
public class Client {

	private final Socket socket;

	public Client(Socket socket) {
		this.socket = socket;
	}

	public String getIp() {
		return socket.getInetAddress().getHostAddress();
	}

	public void disconnect() throws IOException {
		socket.close();
	}

	public void write(byte[] bytes) throws IOException {
		var out = new ObjectOutputStream(socket.getOutputStream());
		out.flush();
		out.write(bytes);
		out.close();
	}

	private void handleMessage() {

	}

}
