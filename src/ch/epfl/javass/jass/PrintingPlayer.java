package ch.epfl.javass.jass;

import java.util.Map;
/**
 * Class to create a PrintingPlayer responsible for printing everything 
 * that happens to him during the course of the game.
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class PrintingPlayer implements Player {
	private final Player underlyingPlayer;

	/**
	 * Constructor of the class assigns the underlying player to its attribute 
	 * @param underlyingPlayer
	 */
	public PrintingPlayer(Player underlyingPlayer) {
		this.underlyingPlayer = underlyingPlayer;
	}

	/**
	 * This method will print a message stating that it is his turn to play
	 * Will ask the underlying player which card he has to play and will print it
	 * @param current state of the game
	 * @param hand of the player
	 * @return the card that the underlying player wants to play
	 */
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
		System.out.print("C'est a moi de jouer...Je joue: ");
		Card c = underlyingPlayer.cardToPlay(state, hand);
		System.out.println(c);
		return c;
	}

	/**
	 * This method will print the name of each player and when it's him
	 * he will print a message saying so
	 * and call this same method for the underlying player
	 * @param ownId id of this player
	 * @param playerNames the names of all the player
	 */
	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		System.out.println("Les joueurs sont:");
		for (PlayerId player : PlayerId.ALL) {
			System.out.print(playerNames.get(player));
			if (player.equals(ownId)) {
				System.out.print(" (it's me)");
			}
			System.out.println();
		}
		underlyingPlayer.setPlayers(ownId, playerNames);
	}

	/**
	 * This method will print his new hand 
	 * and call this same method for the underlying player
	 * @param hand of the player
	 */
	@Override
	public void updateHand(CardSet newHand) {
		System.out.println("My new hand is:" + newHand.toString());
		underlyingPlayer.updateHand(newHand);
	}

	/**
	 * This method prints the trump
	 * and call this same method for the underlying player
	 * @param Color of the trump
	 */
	@Override
	public void setTrump(Card.Color trump) {
		System.out.println("Atout: " + trump.toString());
		underlyingPlayer.setTrump(trump);
	}

	/**
	 * This method prints the trick
	 * and call this same method for the underlying player
	 * @param current trick 
	 */
	@Override
	public void updateTrick(Trick newTrick) {
		System.out.println(newTrick.toString());
		underlyingPlayer.updateTrick(newTrick);
	}

	/**
	 * This method will print the Score
	 * and call this same method for the underlying player
	 * @param current score
	 */
	@Override
	public void updateScore(Score score) {
		System.out.println("Score: " + score.toString());
		underlyingPlayer.updateScore(score);
	}
}
