package br.com.gabrielmusskopf.stop;

public abstract class BytesUtils {

	/**
	 * Read n bytes from data. Allocates a new byte[n], copy the data to this buffer and build a String from it.
	 *
	 * @param data     src data
	 * @param n        how many bytes to read
	 * @param starting from which byte should start reading
	 * @return String from the bytes read
	 */
	public static String readString(byte[] data, int n, int starting) {
		return new String(readN(data, n, starting));
	}

	/**
	 * Read n bytes from data. Allocates a new byte[n] and copy the data to this buffer.
	 *
	 * @param data     src data
	 * @param n        how many bytes to read
	 * @param starting from which byte should start reading
	 * @return bytes read
	 */
	public static byte[] readN(byte[] data, int n, int starting) {
		byte[] buff = new byte[n];
		System.arraycopy(data, starting, buff, 0, buff.length);
		return buff;
	}

}
