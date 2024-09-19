package br.com.gabrielmusskopf.stop.server;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StopServer {

	public static void main(String[] args) throws IOException {
		log.info("Playing STOP!");
		var lobby = new GameLobby();

		var server = new Server(lobby);
		server.start();
/*
		var scanner = new Scanner(System.in);

		var p1 = new Player();
		var p2 = new Player();

		var game = new Game(p1, p2, 1);
		Logger.info("Starting games with {0} rounds", game.getRoundsCount());

		while (game.hasRounds()) {
			game.nextRound();
			Logger.info("Starting round {0}", game.getCurrentRoundCount());

			System.out.println("player 1: ");
			game.answer(p1, "nome", scanner.nextLine());

			System.out.println("player 2: ");
			game.answer(p2, "nome", scanner.nextLine());

			Logger.info("Round {0} finished", game.getCurrentRoundCount());
		}

		Logger.info("Game is over");
 */
	}

}