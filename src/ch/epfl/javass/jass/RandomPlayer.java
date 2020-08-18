package ch.epfl.javass.jass;

import java.util.Random;

/**
 * This class consists on a RandomPlayer that implements the interface
 * Player. And as its name says its principal function is to play randomly.
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class RandomPlayer implements Player {
    private final Random rng;

    /**
     * Constructor of a RandomPlayer
     *
     * @param rngSeed
     */
    public RandomPlayer(long rngSeed) {
        this.rng = new Random(rngSeed);
    }

    /**
     * Chooses a card randomly from his hand and plays it
     *
     * @param currentState
     * @param hand
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        CardSet playable = state.trick().playableCards(hand);
        return playable.get(rng.nextInt(playable.size()));
    }
}
