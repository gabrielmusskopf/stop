package br.com.gabrielmusskopf.stop.server.exception;

public class InvalidCategoryException extends BaseException {

	public InvalidCategoryException() {
		super();
	}

	public InvalidCategoryException(String message) {
		super(message);
	}

	public InvalidCategoryException(String message, Object... params) {
		super(message, params);
	}

}
