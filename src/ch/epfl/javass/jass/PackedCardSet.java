package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits64;
import java.util.StringJoiner;

/**
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class PackedCardSet {
	/*
	 * Non instantiable class
	 */
	private PackedCardSet() {
	}

	public static final long EMPTY = 0L;
	public static final long ALL_CARDS = 0b0000_0001_1111_1111_0000_0001_1111_1111_0000_0001_1111_1111_0000_0001_1111_1111L;
	private static final long[] trumpAbove = { 0b0001_1111_1110L, 0b0001_1111_1100L, 0b0001_1111_1000L,
			0b0001_1110_1000L, 0b0001_1010_1000L, 0b0001_0010_1000L, 0b0000_0010_1000L, 0b0000_0010_0000L, 0L };

	/**
	 * Checks if the packedCardSet is valid
	 * 
	 * @param pkCardSet, the set to verify
	 * @return true if pkCardSet valid
	 */
	public static boolean isValid(long pkCardSet) {
		return (pkCardSet & ~ALL_CARDS) == 0;
	}

	/**
	 * Return the set of cards having a higher rank that the pkCard knowing its a
	 * trump
	 * 
	 * @param pkCard the card to check
	 * @return long cards higher than the one given
	 */
	public static long trumpAbove(int pkCard) {
		assert PackedCard.isValid(pkCard);
		int rank = PackedCard.rank(pkCard).trumpOrdinal();
		int color = PackedCard.color(pkCard).ordinal();
		return trumpAbove[rank] << color * 16;
	}

	/**
	 * Create a packedSetCard with only the specified card in it
	 * 
	 * @param pkCard card to put in empty pack
	 * @return packedCardSet with the single card
	 */
	public static long singleton(int pkCard) {
		assert PackedCard.isValid(pkCard);
		return 1L<<pkCard;
	}

	/**
	 * Check if packedCardSet is empty
	 * 
	 * @param pkCardSet the set to check
	 * @return true if it is empty
	 */
	public static boolean isEmpty(long pkCardSet) {
		return pkCardSet == 0;
	}

	/**
	 * Return the amount of cards in the packedCardSet
	 * 
	 * @param pkCardSet the set to count
	 * @return int amount of cards in that set
	 */
	public static int size(long pkCardSet) {
		assert (isValid(pkCardSet));

		return Long.bitCount(pkCardSet);
	}

	/**
	 * Return the nth card specified by the index number
	 * 
	 * @param pkCardSet set to get the card from
	 * @param index     the nth card to take
	 * @return packedCard at that position
	 */
	public static int get(long pkCardSet, int index) {
		assert (isValid(pkCardSet));

		//Remove a certain amount of 1s at the start
		for (int i = 0; i < index; i++) {
			pkCardSet -= Long.lowestOneBit(pkCardSet);
		}

		//Return the remaining one by calculating the rank and color
		return (((int) (Long.numberOfTrailingZeros(pkCardSet) / 16d)) << 4)
				+ ((Long.numberOfTrailingZeros(pkCardSet)) % 16);
	}

	/**
	 * Adds said card to pkCardSet
	 * 
	 * @param pkCardSet set to add card to
	 * @param pkCard    card to add
	 * @return pkCardSet with this cardç
	 */
	public static long add(long pkCardSet, int pkCard) {
		assert isValid(pkCardSet);
		assert PackedCard.isValid(pkCard);

		return pkCardSet | singleton(pkCard);
	}

	/**
	 * Removes said card from pkCardSet
	 * 
	 * @param pkCardSet set to remove card from
	 * @param pkCard    card to remove
	 * @return pkCardSet without this card
	 */
	public static long remove(long pkCardSet, int pkCard) {
		assert isValid(pkCardSet);
		assert PackedCard.isValid(pkCard);

		return difference(pkCardSet, singleton(pkCard));
	}

	/**
	 * Returns true if said card is in pkCardSet
	 * 
	 * @param pkCardSet set to check
	 * @param pkCard    card to check
	 * @return true if card is in this set
	 */
	public static boolean contains(long pkCardSet, int pkCard) {
		assert isValid(pkCardSet);
		assert PackedCard.isValid(pkCard);

		return Long.bitCount(pkCardSet & singleton(pkCard)) == 1;
	}

	/**
	 * Returns a set containing all cards not included in the packedCardSet
	 * 
	 * @param pkCardSet set to take inverse of
	 * @return packedCardSet with all inverse cards
	 */
	public static long complement(long pkCardSet) {
		assert isValid(pkCardSet);

		return ~pkCardSet & ALL_CARDS;
	}

	/**
	 * Returns a set containing all cards in either set1 and set2
	 * 
	 * @param pkCardSet1 the first set
	 * @param pkCardSet2 the second set
	 * @return packedCardSet with all cards present in either set
	 */
	public static long union(long pkCardSet1, long pkCardSet2) {
		assert isValid(pkCardSet1);
		assert isValid(pkCardSet2);

		return pkCardSet1 | pkCardSet2;
	}

	/**
	 * Returns a set containing all cards in both set1 and set2
	 * 
	 * @param pkCardSet1 the first set
	 * @param pkCardSet2 the second set
	 * @return packedCardSet with all cards present in both sets
	 */
	public static long intersection(long pkCardSet1, long pkCardSet2) {
		assert isValid(pkCardSet1);
		assert isValid(pkCardSet2);

		return pkCardSet1 & pkCardSet2;
	}

	/**
	 * Returns a set containing all cards present in set1 but not set2
	 * 
	 * @param pkCardSet1 the first set
	 * @param pkCardSet2 the second set
	 * @return packedCardSet with all cards present in only set 1
	 */
	public static long difference(long pkCardSet1, long pkCardSet2) {
		assert isValid(pkCardSet1);
		assert isValid(pkCardSet2);

		return pkCardSet1 & complement(pkCardSet2);
	}

	/**
	 * Return the packedCardSet with just the cards of a certain color
	 * 
	 * @param pkCardSet set to take color from
	 * @param color     to return
	 * @return packedCardSet with just the cards of the color specified
	 */
	public static long subsetOfColor(long pkCardSet, Card.Color color) {
		return pkCardSet & Bits64.mask(color.ordinal() * 16, 16);
	}

	/**
	 * Display your cards in packedCardSet
	 * 
	 * @param pkCardSet set to display
	 * @return String that contains the information
	 */
	public static String toString(long pkCardSet) {
		StringJoiner s = new StringJoiner(",", "{", "}");
		for (int i = 0; i < size(pkCardSet); i++) {
			s.add(PackedCard.toString(get(pkCardSet, i)));
		}
		return s.toString();
	}
}
