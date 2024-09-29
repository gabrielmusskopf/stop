package br.com.gabrielmusskopf.stop.server;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.lang3.RandomStringUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.Readable;
import br.com.gabrielmusskopf.stop.server.messages.MessageFactory;

@Slf4j
@RequiredArgsConstructor
public class Player implements AutoCloseable, Readable {

	@Getter
	private final String host;
	private final Socket socket;
	private final BufferedInputStream in;
	private final OutputStream out;
	@Getter
	@Setter
	private String name = "player-" + RandomStringUtils.secure().nextAlphabetic(5);
	private boolean isConnected = false;

	public Player(Socket socket) throws IOException {
		this.socket = socket;
		this.socket.setSoTimeout(1000);
		this.host = socket.getInetAddress().getHostAddress();
		this.in = new BufferedInputStream(this.socket.getInputStream());
		this.out = new DataOutputStream(this.socket.getOutputStream());
		this.isConnected = true;
	}

	public void send(Message message) throws IOException {
		if (!isConnected) {
			throw new IOException("Trying to send a message to the unconnected player %s from %s".formatted(name, getHost()));
		}
		try {
			out.flush();
			out.write(message.serialize());
			out.flush();
		} catch (IOException e) {
			log.error("Could not write message to client {} from {}. Closing connection: {}", name, getHost(), e.getMessage());
			close();
			throw e;
		}
	}

	public int read() throws IOException {
		return in.read();
	}

	public byte[] read(int size) throws IOException {
		var buff = new byte[size];
		in.read(buff, 0, size);
		return buff;
	}

	public void ping() {
		try {
			send(MessageFactory.ping());
			isConnected = true;
		} catch (IOException e) {
			log.warn("Player {} with host {} is no longer connected: {}", name, getHost(), e.getMessage());
			logicalDisconnect();
		}
	}

	public void gracefullyDisconnect() {
		try {
			send(MessageFactory.closeConnection());
			close();
		} catch (IOException e) {
			log.error("Error when gracefully: {}", e.getMessage());
		}
	}

	public boolean isConnected() {
		ping();
		return isConnected;
	}

	private void logicalDisconnect() {
		log.debug("Disconnecting {} logically", name);
		isConnected = false;
	}

	@Override
	public void close() {
		logicalDisconnect();
		try {
			socket.close();
			in.close();
			out.close();
		} catch (IOException e) {
			log.error("Error when closing player {} from {}: {}", name, host, e.getMessage());
		}
	}

}
