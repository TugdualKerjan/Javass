package ch.epfl.javass.jass;

import ch.epfl.javass.jass.Card.Color;

import java.util.Map;

/**
 * Interface implemented by the players with several default methods that are used
 * to inform the player about the situation of the game.
 * And the most important method which is also abstract, cardToPlay
 * that will return the card this player wants to play
 * <p>
 * Note: in this interface most of the methods are declared as default
 * and they don't do anything, for this reason what we explain
 * for each of these methods is just what this methods are supposed to do in the classes
 * that implement this interface, Player).
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public interface Player {
    /**
     * Method that somehow depending on the player
     * that implements this interface will return the
     * card he wants to play
     *
     * @param current state
     * @param hand    of the player
     * @return the card the player wants to play
     */
    abstract Card cardToPlay(TurnState state, CardSet hand);

    /**
     * Method that will inform the player of its PlayerId
     * in addition to the names of each player
     * <p>
     * robably do in the classes that
     * implement Player).
     *
     * @param ownId       of the player
     * @param playerNames of each player
     */
    default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
    }

    /**
     * Method that will inform the player of his newHand
     *
     * @param newHand as a CardSet
     */
    default void updateHand(CardSet newHand) {
    }

    /**
     * Method that will inform the player of the atout of the trick
     *
     * @param trump as color
     */
    default void setTrump(Color trump) {
    }

    /**
     * Method that will inform the player of the situation of the trick
     *
     * @param newTrick that is being played
     */
    default void updateTrick(Trick newTrick) {
    }

    /**
     * Method that will inform the player of the new score
     *
     * @param score of the game
     */
    default void updateScore(Score score) {
    }

    /**
     * Method that will be called when the game finishes
     * and will inform the player of the team who won the game
     *
     * @param winningTeam (TeamId) the team who has won the game
     */
    default void setWinningTeam(TeamId winningTeam) {
    }

    default boolean doYouWantRevenge() {
        return true;
    }

    default void updateRevenge(boolean b) {
    }
}
