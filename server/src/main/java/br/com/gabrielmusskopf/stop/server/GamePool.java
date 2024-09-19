package br.com.gabrielmusskopf.stop.server;

import java.util.LinkedList;
import java.util.Queue;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GamePool {

	private static final int GAMES_LIMIT = 2;
	private static final Queue<Game> GAMES = new LinkedList<>();
	private static final ThreadLocal<Game> WAITING_GAME = new ThreadLocal<>();

	public static Game getGame() {
		if (GAMES.size() >= GAMES_LIMIT) {
			log.info("New game cannot be created while {} games are being played", GAMES.size());
			return null;
		}
		// first person to request the game instance
		if (WAITING_GAME.get() == null) {
			log.info("New game was created and is now waiting for the other player");
			WAITING_GAME.set(new Game());
			return WAITING_GAME.get();
		}
		// second person to request the game instance
		// the game is not waiting anymore and it is added to running games
		log.info("There is already a waiting game, using that");
		var game = WAITING_GAME.get();
		WAITING_GAME.remove();
		GAMES.add(game);
		return game;
	}

	public static void endGame() {
		GAMES.poll();
	}

	public static int ongoingGamesCount() {
		return GAMES.size();
	}

}
