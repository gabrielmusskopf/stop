package br.com.gabrielmusskopf.stop.client;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.Message;
import br.com.gabrielmusskopf.stop.client.exception.MessageBufferException;
import br.com.gabrielmusskopf.stop.client.message.MessageFactory;

@Slf4j
public class ConnectionHandler implements AutoCloseable {

	private static final int RECONNECTION_TRIES = 5;
	private static final int RECONNECTION_WAIT_SEC = 5;
	private static final int MESSAGE_BUFFER_LIMIT = 10;
	private static final int HEARTBEAT_DELAY_SEC = 20;

	private final int originalPort;
	private final Queue<Message> messageQueue = new LinkedList<>();
	private final AtomicBoolean pullMessagesRunning = new AtomicBoolean(false);
	private final AtomicBoolean heartBeatRunning = new AtomicBoolean(false);
	private Thread heartBeatWorker;
	private Thread pullMessagesWorker;

	private Socket socket;
	private BufferedInputStream in;
	private DataOutputStream out;
	private ConnectionState connectionState;

	public ConnectionHandler(Socket socket, int originalPort) throws IOException {
		setIO(socket);
		this.originalPort = originalPort;
		this.connectionState = ConnectionState.CONNECTED;

		heartBeat();
		pullMessages();
	}

	private void setIO(Socket socket) throws IOException {
		this.socket = socket;
		this.socket.setSoTimeout(3000);
		this.socket.setSoLinger(true, 0); // tells server socket with no delay
		this.socket.setKeepAlive(true);
		this.in = new BufferedInputStream(this.socket.getInputStream());
		this.out = new DataOutputStream(this.socket.getOutputStream());
	}

	public void send(Message message) {
		if (message == null) {
			return;
		}
		if (messageQueue.size() == MESSAGE_BUFFER_LIMIT) {
			throw new MessageBufferException("Buffer is full");
		}
		log.debug("Message added to queue");
		messageQueue.add(message);
	}

	public int read() throws IOException {
		final int read = in.read();
		if (read == -1) {
			reconnect();
			return read();
		}
		if (!ConnectionState.CONNECTED.equals(connectionState)) {
			throw new IOException("No longer connected");
		}
		return read;
	}

	public int read(byte[] b, int off, int len) throws IOException {
		final var read = in.read(b, off, len);
		if (read == -1) {
			reconnect();
			return read();
		}
		if (!ConnectionState.CONNECTED.equals(connectionState)) {
			throw new IOException("No longer connected");
		}
		return read;
	}

	private void pullMessages() {
		pullMessagesWorker = new Thread(() -> {
			pullMessagesRunning.set(true);
			log.debug("Pull messages thread started");
			while (pullMessagesRunning.get() && !Thread.currentThread().isInterrupted()) {
				if (messageQueue.isEmpty()) {
					try {
						Thread.sleep(TimeUnit.SECONDS.toMillis(1));
					} catch (InterruptedException e) {
						// time to shut down
						pullMessagesRunning.set(false);
						return;
					}
					continue;
				}
				log.debug("Message queue is not empty");
				var msg = messageQueue.peek();
				try {
					out.flush();
					out.write(msg.serialize());
					out.flush();
					messageQueue.poll();
					log.debug("Message sended and removed from the pool");
				} catch (IOException e) {
					log.error("Cound not send message {}: {}", msg, e.getMessage());
				} catch (Exception e) {
					log.error("Unexpected exception:", e);
				}
				while (ConnectionState.RECONNECTING.equals(connectionState)) {
					// wait reconnection
				}
			}
			log.debug("No longer connected, stop pulling messages");
		});

		pullMessagesWorker.start();
	}

	private void heartBeat() {
		heartBeatWorker = new Thread(() -> {
			heartBeatRunning.set(true);
			var beatMessage = MessageFactory.beat();
			while (heartBeatRunning.get() && !Thread.currentThread().isInterrupted()) {
				try {
					Thread.sleep(TimeUnit.SECONDS.toMillis(HEARTBEAT_DELAY_SEC));
				} catch (InterruptedException e) {
					// time to shut down
					heartBeatRunning.set(false);
					return;
				}
				try {
					out.flush();
					out.write(beatMessage.serialize());
					out.flush();
					log.debug("Hearbeat send");
				} catch (IOException e) {
					if (ConnectionState.RECONNECTING.equals(connectionState)) {
						log.debug("Starting reconnection");
						reconnect();
					}
				}
				while (ConnectionState.RECONNECTING.equals(connectionState)) {
					// wait reconnection
				}
			}
			log.debug("Stopping heartbeat");
		});

		heartBeatWorker.start();
	}

	private void reconnect() {
		log.debug("Reconnectig socket in {} tries", RECONNECTION_TRIES);
		connectionState = ConnectionState.RECONNECTING;
		int remaining = 0;

		while (remaining < RECONNECTION_TRIES) {
			try {
				var s = new Socket(socket.getInetAddress(), originalPort);
				setIO(s);
				break;
			} catch (IOException e) {
				log.debug("Reconnection error {}/{}: {}", remaining + 1, RECONNECTION_TRIES, e.getMessage());
				gentleSleep(RECONNECTION_WAIT_SEC);
				remaining++;
			}
		}
		if (remaining < RECONNECTION_TRIES) {
			connectionState = ConnectionState.CONNECTED;
			log.debug("Reconnected");
		} else {
			connectionState = ConnectionState.CLOSED;
			log.debug("Could not reconnect");
		}
	}

	private void gentleSleep(int seconds) {
		try {
			Thread.sleep(TimeUnit.SECONDS.toMillis(seconds));
		} catch (InterruptedException ignored) {
		}
	}

	@Override
	public void close() throws IOException {
		heartBeatRunning.set(false);
		if (!heartBeatWorker.isInterrupted()) heartBeatWorker.interrupt();
		pullMessagesRunning.set(false);
		if (!pullMessagesWorker.isInterrupted()) pullMessagesWorker.interrupt();
		connectionState = ConnectionState.CLOSED;
		socket.close();
		in.close();
		out.close();
	}

}
