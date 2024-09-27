package br.com.gabrielmusskopf.stop.server;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GamePool {

	private static final int GAMES_LIMIT = 2;
	private static int ongoingGames = 0;
	private static Game waitingGame;

	public static Game getGame() {
		if (ongoingGames >= GAMES_LIMIT) {
			log.info("New game cannot be created while {} games are being played", ongoingGames);
			return null;
		}
		// first person to request the game instance
		if (waitingGame == null) {
			log.info("New game was created and is now waiting for the other player");
			waitingGame = new Game();
			return waitingGame;
		}
		// second person to request the game instance
		// the game is not waiting anymore, and it is added to running games
		log.info("There is already a waiting game, using that");
		return waitingGame;
	}

	public static void start() {
		ongoingGames++;
		waitingGame = null;
	}

	public static void endGame() {
		ongoingGames--;
	}

	public static int ongoingGamesCount() {
		return ongoingGames;
	}

}
