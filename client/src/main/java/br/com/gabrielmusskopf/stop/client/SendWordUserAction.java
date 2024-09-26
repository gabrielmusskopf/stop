package br.com.gabrielmusskopf.stop.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SendWordUserAction implements UserAction {

	private final Action action = Action.SEND_WORD;
	private final int category;
	private final String word;

}
