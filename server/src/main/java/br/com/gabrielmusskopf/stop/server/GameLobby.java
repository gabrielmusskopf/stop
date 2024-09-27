package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import br.com.gabrielmusskopf.stop.server.messages.MessageFactory;

@Slf4j
public class GameLobby {

	public void addClient(Player player) {
		final var game = GamePool.getGame();
		if (game == null) {
			//IDEA: maybe here client can go to a queue and wait until the server can handle new games
			player.gracefullyDisconnect();
			log.info("Disconnecting player {}", player.getHost());
			return;
		}

		if (!game.isEmpty() && !game.isStillWaiting()) {
			// if it is the second player, check if first still connected
			log.warn("Game {} is not ready anymore. Restarting it (removing all players)", game.getIdentifier());
			game.removeAllPlayers();
		}

		try {
			game.playerJoin(player);
			player.send(MessageFactory.playerConnectedSuccessfully());
			log.info("Player {} from {} connected to a game", player.getName(), player.getHost());

			if (game.hasEnoughPlayers()) {
				log.debug("Starting a new game thread for '{}'", game.getIdentifier());
				GamePool.start();
				new Thread(game).start();
			} else {
				player.send(MessageFactory.waitingPlayer());
			}
		} catch (IOException e) {
			log.error("Was not possible to send a message to client {}. Disconnecting him.", player.getName());
			game.removePlayer(player);
		}
	}
}
