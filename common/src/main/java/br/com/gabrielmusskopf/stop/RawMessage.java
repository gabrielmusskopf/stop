package br.com.gabrielmusskopf.stop;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Optional;

import javax.swing.text.html.Option;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RawMessage {

	// common headers
	private final int size;
	private final MessageType type;

	// body
	private final byte[] data;

	public RawMessage(int size, int code, byte[] data) {
		this.size = size;
		this.type = MessageType.fromCode(code);
		this.data = data;
	}

	private RawMessage() {
		this.size = 0;
		this.type = MessageType.UNKNOWN;
		this.data = new byte[]{};
	}

	/**
	 * Read from {@link Readable}. The message read must have the expected format
	 * @param in a readable
	 * @return message read
	 * @throws IOException from {@link Readable}
	 */
	public static RawMessage readRawMessage(Readable in) throws IOException {
		var size = in.read();
		var typeCode = in.read();
		var data = in.read(size - 2);

		return new RawMessage(size, typeCode, data);
	}

	/**
	 * Read from {@link Readable} until timeout. The message read must have the expected format
	 * @param in a readable
	 * @return message read or message of type UNKNOWN if nothing was read whiting timeout
	 * @throws IOException from {@link Readable}
	 */
	public static RawMessage readRawMessageOrUnknown(Readable in) throws IOException {
		try {
			var size = in.read();
			var typeCode = in.read();
			var data = in.read(size - 2);

			return new RawMessage(size, typeCode, data);
		} catch (SocketTimeoutException e) {
			return RawMessage.unknown();
		}
	}

	private static RawMessage unknown() {
		return new RawMessage();
	}

}
