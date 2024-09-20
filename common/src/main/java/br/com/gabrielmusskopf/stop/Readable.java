package br.com.gabrielmusskopf.stop;

import java.io.IOException;

public interface Readable {

	int read() throws IOException;

	byte[] read(int size) throws IOException;

}
