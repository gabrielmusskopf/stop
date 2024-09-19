package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameLobby {

	public void addClient(Player player) throws IOException {
		final var game = GamePool.getGame();
		if (game == null) {
			//IDEA: maybe here client can go to a queue and wait until the server can handle new games
			//TODO: This need to be a type of message to the client knows what to do
			player.send("There are no games waiting for players and the server cannot handle new games. Please try again later.");
			player.disconnect();
			log.info("Disconnecting player {}", player.getHost());
			return;
		}


		game.playerJoin(player);

		if (game.hasEnoughPlayers()) {
			log.info("Starting a new game thread for '{}'", game.getIdentifier());
			new Thread(game).start();
		}
	}
}
