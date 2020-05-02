package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;
/**
* This class is an immutable class and consits on the representation of a Trick
* This class is mainly using the static methods from PackedTrick.
* 
* @author Tugdual Kerjan (297804)
* @author Marcel Torne (299366)
*/
public final class Trick {

	public static final Trick INVALID = new Trick(PackedTrick.INVALID);

	private final int pkTrick;

	/**
	 * Private constructor
	 * 
	 * @param integer representation of Trick that we want to create (packedTrick) 
	 */
	private Trick(int packedTrick) {
		pkTrick = packedTrick;
	}
	
	
	/**
	 * Create an empty trick
	 * 
	 * @param trump       the color of the trump Cards
	 * @param firstPlayer the first player to play
	 * @return a new object from the class Trick which is empty
	 */
	public static Trick firstEmpty(Color trump, PlayerId firstPlayer) {
		return new Trick(PackedTrick.firstEmpty(trump, firstPlayer));
	}
	/**
	 * Creates a trick corresponding to the packTrick
	 * 
	 * @param integer representing packedTrick 
	 * @return a new Trick with the corresponding value of the packedTrick 
	 * given as a parameter
	 */
	public static Trick ofPacked(int packedTrick) {
		Preconditions.checkArgument(PackedTrick.isValid(packedTrick));
		return new Trick(packedTrick);
	}
	/**
	 * Method to get the packed value of the team
	 * 
	 * @return the integer corresponding to the integer representation of the curret Trick
	 */
	public int packed() {
		return pkTrick;
	}

	/**
	 * Based of then current Trick creates the following trick empty
	 * 
	 * @return Trick with just the winning player, index and trump(no cards played yet)
	 */	
	public Trick nextEmpty() {
		if (!PackedTrick.isFull(this.packed()))
			throw new IllegalStateException();
		if (PackedTrick.nextEmpty(pkTrick) == PackedTrick.INVALID) {
			return INVALID;
		}
		return Trick.ofPacked(PackedTrick.nextEmpty(pkTrick));
	}

	/**
	 * Check if this Trick is empty
	 * 
	 * @return true if it is empty, false if not
	 */
	public boolean isEmpty() {
		return PackedTrick.isEmpty(pkTrick);
	}
	/**
	 * Check if this Trick is full
	 * 
	 * @return true if it is full, false if not
	 */
	public boolean isFull() {
		return PackedTrick.isFull(pkTrick);
	}

	/**
	 * Check if this is the last trick
	 * 
	 * @return true if last round
	 */
	public boolean isLast() {
		return PackedTrick.isLast(pkTrick);
	}

	/**
	 * Return the amount of cards in the current Trick
	 * 
	 * @return int current amount of cards in this trick
	 */
	public int size() {
		return PackedTrick.size(pkTrick);
	}

	/**
	 * Returns the color of the trump of this Trick
	 * 
	 * @return color of trump of this Trick
	 */
	public Color trump() {
		return PackedTrick.trump(pkTrick);
	}
	
	/**
	 * Return the trick number of this trick
	 * 
	 * @return index of this trick
	 */
	public int index() {
		return PackedTrick.index(pkTrick);
	}
	
	/**
	 * Return the player who will play at a certain index
	 * 
	 * @param pkTrick trick to check
	 * @param index   of the player
	 * @return player at that index
	 */
	public PlayerId player(int index) {
		Preconditions.checkIndex(index, 4);
		return PackedTrick.player(pkTrick, index);
	}
	/**
	 * Return the card that has been played at a certain index inside the trick
	 * 
	 * @param index   of the card
	 * @return card at that index
	 */
	public Card card(int index) {
		Preconditions.checkIndex(index, size());
		return Card.ofPacked(PackedTrick.card(pkTrick, index));
	}

	/**
	 * Add a card to the current trick
	 * 
	 * @param Card to add
	 * @return a new Trick with the card in it
	 */
	public Trick withAddedCard(Card c) {
		if (isFull())
			throw new IllegalStateException();
		return Trick.ofPacked(PackedTrick.withAddedCard(pkTrick, c.packed()));
	}
	
	/**
	 * Return base color used in the current trick, the first card is supposed to
	 * have been played
	 * 
	 * @return color corresponding to the base
	 */
	public Color baseColor() {
		if (isEmpty())
			throw new IllegalStateException();
		return PackedTrick.baseColor(pkTrick);
	}
	
	/**
	 * Return the cards that a certain hand can play taking into account the current trick
	 * 
	 * @param CardSet hand with cards
	 * @return CardSet containing all cards you can use
	 */
	public CardSet playableCards(CardSet hand) {
		if (isFull())
			throw new IllegalStateException();
		return CardSet.ofPacked(PackedTrick.playableCards(pkTrick, hand.packed()));
	}

	/**
	 * Return how many points the current Trick is worth
	 * 
	 * @return int amount of points worth
	 */
	public int points() {
		return PackedTrick.points(pkTrick);
	}

	/**
	 * Return the player currently winning this trick
	 * 
	 * @return PlayerId of player currently winning
	 */
	public PlayerId winningPlayer() {
		if (isEmpty())
			throw new IllegalStateException();
		return PackedTrick.winningPlayer(pkTrick);
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Trick)) {
			return false;
		}
		return ((Trick) o).packed() == this.packed();

	}

	@Override
	public int hashCode() {
		return this.packed();
	}

	@Override
	public String toString() {
		return PackedTrick.toString(pkTrick);
	}
}
