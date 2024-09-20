package br.com.gabrielmusskopf.stop.server.messages;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import lombok.RequiredArgsConstructor;

import br.com.gabrielmusskopf.stop.server.MessageType;

/*
Header:
- msg length (4 bytes)
- type (4 bytes)
Body:
- variable size
 */
@RequiredArgsConstructor
public abstract class MessageBuilder implements Message {

	public static Builder of(MessageType messageType) {
		return new Builder(messageType);
	}

	@RequiredArgsConstructor
	public static class Builder {

		private static final int MESSAGE_LENGTH_SIZE = 1;
		private static final int MESSAGE_TYPE_SIZE = 1;

		private final ByteArrayOutputStream data = new ByteArrayOutputStream();
		private final MessageType messageType;

		public Builder put(int i) {
			return put((byte) i);
		}

		public Builder put(char c) {
			return put((byte) c);
		}

		public Builder put(byte b) {
			data.write(b);
			return this;
		}

		public Builder put(byte[] bytes) {
			data.writeBytes(bytes);
			return this;
		}

		public byte[] build() {
			var size = MESSAGE_LENGTH_SIZE + MESSAGE_TYPE_SIZE + data.size();
			var buffer = ByteBuffer.allocate(size);
			buffer.put((byte) size);
			buffer.put((byte) messageType.getCode());
			buffer.put(data.toByteArray());

			return buffer.array();
		}

	}

}
