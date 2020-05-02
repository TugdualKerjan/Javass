package ch.epfl.javass.jass;

import java.util.List;

/**
 * Class to create a new CardSet, final and immutable
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class CardSet {

	// packed version of the set
	private final long pkCardSet;

	// Empty and full sets
	public static final CardSet EMPTY = ofPacked(PackedCardSet.EMPTY);
	public static final CardSet ALL_CARDS = ofPacked(PackedCardSet.ALL_CARDS);

	/**
	 * Private constructor
	 * 
	 * @param packedCardSet the set to store
	 */
	private CardSet(long packedCardSet) {
		pkCardSet = packedCardSet;
	}

	/**
	 * Returns a CardSet containing the cards specified
	 * 
	 * @param cards in the set
	 * @return CardSet with the specified cards
	 */
	public static CardSet of(List<Card> cards) {
		long packed = 0L;
		for (Card card : cards) {
			packed = PackedCardSet.add(packed, card.packed());
		}
		return new CardSet(packed);
	}

	/**
	 * Returns CardSet based of the packed value
	 * 
	 * @param packed long representing set
	 * @return CardSet with cards represented in packed
	 * 
	 * @throws IllegalArgumentException if the long given as 
	 * a parameter doesn't represent a valid CardSet
	 */
	public static CardSet ofPacked(long packed) {
		if (!PackedCardSet.isValid(packed)) {
			throw new IllegalArgumentException("Invalid CardSet");
		}
		return new CardSet(packed);
	}

	/**
	 * Returns the CardSet as a long
	 * 
	 * @return long representing the CardSet
	 */
	public long packed() {
		return pkCardSet;
	}

	/**
	 * Check if the CardSet is empty
	 * 
	 * @return true if this is empty
	 */
	public boolean isEmpty() {
		return PackedCardSet.isEmpty(pkCardSet);
	}

	/**
	 * Return the amount of cards in the CardSet
	 * 
	 * @return int amount of cards in CardSet
	 */
	public int size() {
		return PackedCardSet.size(pkCardSet);
	}

	/**
	 * Return the card at that 'depth' of the set
	 * 
	 * @param index the card index to get
	 * @return Card at that index
	 */
	public Card get(int index) {
		return Card.ofPacked(PackedCardSet.get(pkCardSet, index));
	}

	/**
	 * Add a card to the CardSet
	 * 
	 * @param card to add
	 * @return CardSet with the new card
	 */
	public CardSet add(Card card) {
		return ofPacked(PackedCardSet.add(pkCardSet, card.packed()));
	}

	/**
	 * Remove a card to the CardSet
	 * 
	 * @param card to remove
	 * @return CardSet without the card
	 */
	public CardSet remove(Card card) {
		return ofPacked(PackedCardSet.remove(pkCardSet, card.packed()));
	}

	/**
	 * Checks if a card is in CardSet
	 * 
	 * @param card to check
	 * @return true if it is in the CardSet
	 */
	public boolean contains(Card card) {
		return PackedCardSet.contains(pkCardSet, card.packed());
	}

	/**
	 * Return the CardSet opposite to this one
	 * 
	 * @return CardSet containing all cards not in this set
	 */
	public CardSet complement() {
		return ofPacked(PackedCardSet.complement(pkCardSet));
	}

	/**
	 * Return set containing all cards contained in either this or that CardSet
	 * 
	 * @param that the other CardSet to check
	 * @return CardSet with all cards in either this or that CardSet
	 */
	public CardSet union(CardSet that) {
		return ofPacked(PackedCardSet.union(pkCardSet, that.packed()));
	}

	/**
	 * Return set containing all cards contained in both this and that CardSet
	 * 
	 * @param that the other CardSet to check
	 * @return CardSet with all cards in both this and that CardSet
	 */
	public CardSet intersection(CardSet that) {
		return ofPacked(PackedCardSet.intersection(pkCardSet, that.packed()));
	}

	/**
	 * Return set containing only cards contained in this but not that CardSet
	 * 
	 * @param that the other CardSet to check
	 * @return CardSet with all cards in this but not that CardSet
	 */
	public CardSet difference(CardSet that) {
		return ofPacked(PackedCardSet.difference(pkCardSet, that.packed()));
	}

	/**
	 * Return the CardSet containing only the cards of a certain color in this
	 * CardSet
	 * 
	 * @param color to retrieve
	 * @return CardSet containing only the cards of a certain color in this CardSet
	 */
	public CardSet subsetOfColor(Card.Color color) {
		return ofPacked(PackedCardSet.subsetOfColor(pkCardSet, color));
	}

	@Override
	public boolean equals(Object thatO) {
		if (thatO instanceof CardSet) {
			if (pkCardSet == ((CardSet) thatO).packed()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(pkCardSet);
	}

	@Override
	public String toString() {
		return PackedCardSet.toString(pkCardSet);
	}
}
