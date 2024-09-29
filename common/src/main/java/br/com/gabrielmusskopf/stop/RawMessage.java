package br.com.gabrielmusskopf.stop;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public class RawMessage {

	// common headers
	private final int size;
	private final MessageType type;

	// body
	private final byte[] data;

	public RawMessage(int size, int code, byte[] data) {
		log.debug("New RawMessage with {size={}, code={}}", size, code);
		this.size = size;
		this.type = MessageType.fromCode(code);
		this.data = data;
	}

	private RawMessage() {
		this(MessageType.UNKNOWN);
	}

	private RawMessage(MessageType messageType) {
		this.size = 0;
		this.type = messageType;
		this.data = new byte[] {};
	}

	/**
	 * Read from {@link Readable} until timeout. The message read must have the expected format
	 *
	 * @param in a readable
	 * @return message read or message of type UNKNOWN if nothing was read whiting timeout
	 * @throws IOException from {@link Readable}
	 */
	public static RawMessage readRawMessageOrUnknown(Readable in) throws IOException {
		try {
			return readRawMessage(in);
		} catch (SocketTimeoutException e) {
			return RawMessage.unknown();
		}
	}

	/**
	 * Read from {@link Readable}. The message read must have the expected format
	 *
	 * @param in a readable
	 * @return message read
	 * @throws IOException from {@link Readable}
	 */
	public static RawMessage readRawMessage(Readable in) throws IOException {
		try {
			var size = readOrDie(in);
			var typeCode = readOrDie(in);
			var data = in.read(size - 2);

			return new RawMessage(size, typeCode, data);
		} catch (ConnectException e) {
			return new RawMessage(MessageType.CONNECTION_CLOSED);
		}
	}

	private static int readOrDie(Readable in) throws IOException {
		int read = in.read();
		if (read == -1) {
			throw new ConnectException("Server no longer connected");
		}
		return read;
	}

	public static RawMessage unknown() {
		return new RawMessage();
	}

}
