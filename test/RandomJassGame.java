

import java.util.HashMap;
import java.util.Map;

import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.PrintingPlayer;
import ch.epfl.javass.jass.RandomPlayer;
import ch.epfl.javass.net.RemotePlayerClient;
/**
 * This class consists on a main method that will simulate a JassGame
 * At this moment we are using Mcts player and a PrintingPlayer as a random player
 * vs a distant player and another random player
 * 
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class RandomJassGame {
    public static void main(String[] args) {
        Map<PlayerId, Player> players = new HashMap<>();
        Map<PlayerId, String> playerNames = new HashMap<>();

        for (int i = 1; i < 1000; i++) {
            for (PlayerId pId : PlayerId.ALL) {
                Player player = new RandomPlayer(2019);

                if (pId == PlayerId.PLAYER_1) {
                    player = (Player) new MctsPlayer(pId, 0, 100);
                } else if (pId == PlayerId.PLAYER_3) {
                    player = (Player) new PrintingPlayer(player);
                } else if (pId == PlayerId.PLAYER_4) {
                    RemotePlayerClient playerConnect;
                    try {
                        playerConnect = new RemotePlayerClient("128.179.136.32");
                        player = (Player) playerConnect;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } 
                }
                players.put(pId, player);
                playerNames.put(pId, pId.name());
            }

            JassGame g = new JassGame(2019, players, playerNames);
            while (!g.isGameOver()) {
                g.advanceToEndOfNextTrick();
            }
        }
    }
}