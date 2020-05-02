package ch.epfl.javass.jass;

import ch.epfl.javass.jass.Card.Color;

/**
 * This class represents a Turn, it's immutable, and is consituted by the Score, 
 * the Unplayed cards, and the current trick. 
 * 
 * @author Marcel Torne (299366)
 */
public final class TurnState {
	
	// Attributes
	private final long currentScore;
	private final long unplayedCards;
	private final int currentTrick;

	/**
	 * Constructor
	 * 
	 * @param score
	 * @param remainingCards
	 * @param trick
	 */
	private TurnState(long score, long remainingCards, int trick) {
		currentScore = score;
		unplayedCards = remainingCards;
		currentTrick = trick;
	}

	/**
	 * Initialise a TurnState with the unpacked version
	 * 
	 * @param trump
	 * @param score
	 * @param firstPlayer
	 * @return TurnState that was initialised
	 */
	public static TurnState initial(Color trump, Score score, PlayerId firstPlayer) {
		Trick trick = Trick.firstEmpty(trump, firstPlayer);
		return new TurnState(score.packed(), CardSet.ALL_CARDS.packed(), trick.packed());
	}

	/**
	 * Initialise a TurnState with the packed version
	 * 
	 * @param pkScore
	 * @param pkUnplayedCards
	 * @param pkTrick
	 * @return TurnState that was initialised
	 */
	public static TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick)
			throws IllegalArgumentException {
		if (!PackedScore.isValid(pkScore) || !PackedCardSet.isValid(pkUnplayedCards) || !PackedTrick.isValid(pkTrick)) {
			throw new IllegalArgumentException("entrees invalides");
		}
		return new TurnState(pkScore, pkUnplayedCards, pkTrick);
	}

	/**
	 * Return the packedScore
	 * 
	 * @return long representing the score
	 */
	public long packedScore() {
		return currentScore;
	}

	/**
	 * Return the packedUnplayedCards
	 * 
	 * @return long representing the unplayed cards
	 */
	public long packedUnplayedCards() {
		return unplayedCards;
	}

	/**
	 * Return the packedTrick
	 * 
	 * @return int representing the packed trick
	 */
	public int packedTrick() {
		return currentTrick;
	}

	/**
	 * Return the score
	 * 
	 * @return Score representing the score
	 */
	public Score score() {
		return Score.ofPacked(currentScore);
	}

	/**
	 * Return the unplayed cards
	 * 
	 * @return CardSet representing the unplayed cards
	 */
	public CardSet unplayedCards() {
		return CardSet.ofPacked(unplayedCards);
	}

	/**
	 * Return the current trick
	 * 
	 * @return Trick representing the current trick
	 */
	public Trick trick() {
		return Trick.ofPacked(currentTrick);
	}

	/**
	 * Is this the last trick bing played?
	 * 
	 * @return true if the last trick is being played
	 */
	public boolean isTerminal() {
		return currentTrick == Trick.INVALID.packed();
	}

	/**
	 * Get the ID of the player who has to play
	 * 
	 * @return PlayerId, player who needs to play
	 * @throws IllegalStateException if the trick is full
	 */
	public PlayerId nextPlayer() throws IllegalStateException {
		if (trick().isFull()) {
			throw new IllegalStateException("le pli est plein");
		}
		return trick().player(trick().size());
	}

	/**
	 * Return the TurnState with a new card added
	 * 
	 * @param card to add
	 * @throws IllegalStateException if the trick is full already
	 * @return TurnState with the new card added
	 */
	public TurnState withNewCardPlayed(Card card) {
		if (trick().isFull()) {
			throw new IllegalStateException("The trick is full");
		}
		return new TurnState(currentScore,
				unplayedCards().remove(card).packed(),
				trick().withAddedCard(card).packed());
	}

	/**
	 * Get the TurnState with the trick collected
	 * 
	 * @return TurnState for the new trick
	 * @throws IllegalStateException if the trick is not finished yet
	 */
	public TurnState withTrickCollected() throws IllegalStateException {
		if (!trick().isFull()) {
			throw new IllegalStateException("le pli n'est pas encore plein");
		}
		return new TurnState(
				score().withAdditionalTrick(this.trick().winningPlayer().team(), this.trick().points()).packed(),
				packedUnplayedCards(), trick().nextEmpty().packed());
	}

	/**
	 * Get the TurnState with the new card played and collect the trick if it is now
	 * full
	 * 
	 * @param card to play
	 * @return TurnState with the card added and the trick collected
	 * @throws IllegalStateException if the trick is not finished yet
	 */
	public TurnState withNewCardPlayedAndTrickCollected(Card card) {
		if (trick().isFull()) {
			throw new IllegalStateException("pli est plein");
		}
		TurnState turnCardPlayed = withNewCardPlayed(card);
		return (turnCardPlayed.trick().isFull()) ? turnCardPlayed.withTrickCollected() : turnCardPlayed;
	}
}
