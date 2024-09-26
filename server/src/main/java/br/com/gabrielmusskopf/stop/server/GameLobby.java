package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.server.messages.MessageFactory;

@Slf4j
public class GameLobby {

	public void addClient(Player player) throws IOException {
		final var game = GamePool.getGame();
		if (game == null) {
			//IDEA: maybe here client can go to a queue and wait until the server can handle new games
			player.disconnect();
			log.info("Disconnecting player {}", player.getHost());
			return;
		}

		// TODO: [bug] when one player connect and disconnect before the other, this game must be deleted (or something else)
		game.playerJoin(player);
		player.send(MessageFactory.playerConnectedSuccessfully());

		if (game.hasEnoughPlayers()) {
			log.debug("Starting a new game thread for '{}'", game.getIdentifier());
			new Thread(game).start();
		} else {
			player.send(MessageFactory.waitingPlayer());
		}
	}
}
