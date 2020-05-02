package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javass.Preconditions;

/**
 * Class to create a new card, along with its packedValue Doesn't have public
 * constructor, but rather two static methods to create cards Methods like
 * color(), rank(), isBetter(), points(), toString() call their equivalent in
 * PackedCard, passing packedValue
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class Card {

	// Card packed as int
	private final int pkCard;
	
	private final static int[] TRUMP_ORDINALS= { 0,1,2,7,3,8,4,5,6};
	
	/**
	 * Private constructor
	 * 
	 * @param packedCard the pack to store
	 */
	private Card(int packedCard) {
		pkCard = packedCard;
	}

	/**
	 * Returns a Card of that color and rank
	 * 
	 * @param c the Color we want the card to have
	 * @param r the Rank we want the card to have
	 * @return Card
	 */
	public static Card of(Color c, Rank r) {
		return new Card(PackedCard.pack(c, r));
	}

	/**
	 * Returns a Card based of a packed int
	 * 
	 * @param packed the compressed int
	 * @return Card
	 * @throws IllegalArgumentException if the given integer does not represent a valid Card
	 */
	public static Card ofPacked(int packed) {
		Preconditions.checkArgument(PackedCard.isValid(packed));
			return new Card(packed);
	}

	/**
	 * Returns the packed int of the card
	 * 
	 * @return packedValue of this card
	 */
	public int packed() {
		return pkCard;
	}

	/**
	 * Return color of Card
	 * 
	 * @return Color of this card
	 */
	public Color color() {
		return PackedCard.color(pkCard);
	}

	/**
	 * Return rank of Card
	 * 
	 * @return Rank of this Card
	 */
	public Rank rank() {
		return PackedCard.rank(pkCard);
	}

	/**
	 * Returns true if this card is better than a given card
	 * 
	 * @param trump the 'atout'
	 * @param that  card to compare to
	 * @return true if this card is better
	 */
	public boolean isBetter(Color trump, Card that) {
		return PackedCard.isBetter(trump, this.packed(), that.packed());
	}

	/**
	 * Returns the value(points) of this card
	 * 
	 * @param trump the 'atout'
	 * @return int points this card is worth
	 */
	public int points(Color trump) {
		return PackedCard.points(trump, this.packed());
	}

	/**
	 * Returns true if that0 is equivalent to this object
	 * 
	 * @param that0 card to compare to
	 * @return true if this Cards are equivalent
	 */
	@Override
	public boolean equals(Object thatO) {
		if (thatO instanceof Card) {
			if (this.packed() == ((Card) thatO).packed()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the packedValue of this card
	 * 
	 * @return int packedValue that is the same for a given Color and Rank
	 */
	@Override
	public int hashCode() {
		return this.packed();
	}

	/**
	 * Returns a string representing this Card
	 * 
	 * @return String representing this card
	 */
	@Override
	public String toString() {
		return PackedCard.toString(this.packed());
	}

	/**
	 * @author Marcel Torne (299366)
	 */
	public enum Color {

		SPADE("\u2660"), HEART("\u2661"), DIAMOND("\u2662"), CLUB("\u2663");

		private String symbol;

		public static final List<Color> ALL = Collections.unmodifiableList(Arrays.asList(values()));
		public static final int COUNT = ALL.size();

		// Constructor of the class
		private Color(String symbol) {
			this.symbol = symbol;
		}

		@Override
		public String toString() {
			return symbol;
		}
	}

	/**
	 * @author Marcel Torne (299366)
	 */
	public enum Rank {

		SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"), TEN("10"), JACK("J"), QUEEN("Q"), KING("K"), ACE("A");


		public static final List<Rank> ALL = Collections.unmodifiableList(Arrays.asList(values()));
		public static final int COUNT = ALL.size();
		
		private String representation;

		// Constructor of class
		private Rank(String representation) {
			this.representation = representation;
		}

		/**
		 * Gives you the value of the card when it is color trump
		 * knowing that 0 is the least powerful card and 8 the most
		 * 
		 * @return the value of the card when it's color is trump, knowing it's rank
		 */
		public final int trumpOrdinal() {
			return TRUMP_ORDINALS[this.ordinal()];
		}

		@Override
		public String toString() {
			return representation;
		}
	}

}
