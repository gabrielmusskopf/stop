package br.com.gabrielmusskopf.stop.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StopUserAction implements UserAction {

	private final Action action = Action.STOP;

}
