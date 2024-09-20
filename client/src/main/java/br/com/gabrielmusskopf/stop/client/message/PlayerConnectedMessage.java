package br.com.gabrielmusskopf.stop.client.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
Header:
- status (1 byte)
Body:
(empty)
 */
@Getter
@RequiredArgsConstructor
public class PlayerConnectedMessage {

	private final RequestStatus status;

	public PlayerConnectedMessage(byte[] data) {
		this.status = RequestStatus.fromCode(data[0]);
	}

}
