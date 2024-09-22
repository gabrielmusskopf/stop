package br.com.gabrielmusskopf.stop.client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.Readable;

@Slf4j
public class Player implements AutoCloseable, Readable {

	private final Socket socket;
	private final BufferedInputStream in;
	private final DataOutputStream out;

	public Player(Socket socket) throws IOException {
		this.socket = socket;
		this.socket.setSoTimeout(3000);
		this.in = new BufferedInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}

	public int read() throws IOException {
		return in.read();
	}

	public byte[] read(int size) throws IOException {
		var buff = new byte[size];
		in.read(buff, 0, size);
		return buff;
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

	public String getHost() {
		return socket.getInetAddress().getHostAddress();
	}

	public void disconnect() throws IOException {
		socket.close();
		in.close();
		out.close();
	}

	@Override
	public void close() throws IOException {
		disconnect();
	}

}
