package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;

/**
 * Class that consists of static methods that allows us to check things like validity,
 * pack cards, extract their Color or Rank, their value, and if one is better
 * than another, as well as transform the into a String
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class PackedCard {

    public static final int INVALID = 0b111111;
    //Constants
    private final static int COLOR_INDEX = 4;
    private final static int COLOR_LENGTH = 2;
    private final static int RANK_INDEX = 0;
    private final static int RANK_LENGTH = 4;
    //Maps the index of the cards to their respective points, trump and non trump
    private static int[][] points = {{0, 0, 0, 14, 10, 20, 3, 4, 11, Integer.MAX_VALUE},
            {0, 0, 0, 0, 10, 2, 3, 4, 11, Integer.MAX_VALUE}};
    // Non instantiable
    private PackedCard() {
    }

    /**
     * Checks if the packed Card is valid
     *
     * @param pkCard
     * @return the boolean of the validity of pkCard
     */
    public static boolean isValid(int pkCard) {
        int rank = Bits32.extract(pkCard, RANK_INDEX, RANK_LENGTH);
        int bitsInutilises = Bits32.extract(pkCard, RANK_LENGTH + COLOR_LENGTH, Integer.SIZE - (RANK_LENGTH + COLOR_LENGTH));
        return (bitsInutilises == 0 && rank >= 0 && rank < Card.Rank.COUNT);
    }

    /**
     * Returns the packed Card
     *
     * @param c the card Color
     * @param r the card Rank
     * @return int representing packed card
     */
    public static int pack(Card.Color c, Card.Rank r) {
        int v1 = r.ordinal();
        int v2 = c.ordinal();
        int packedCard = Bits32.pack(v1, RANK_LENGTH, v2, COLOR_LENGTH);

        return packedCard;
    }

    /**
     * Returns the color of the card (Spades ... Hearts)
     *
     * @param pkCard
     * @return Color of that card
     */
    public static Card.Color color(int pkCard) {
        assert isValid(pkCard);
        int color = Bits32.extract(pkCard, COLOR_INDEX, COLOR_LENGTH);
        return Card.Color.ALL.get(color);
    }

    /**
     * Returns the rank of a card (6, 7 ... A)
     *
     * @param pkCard
     * @return Rank of that card
     */
    public static Card.Rank rank(int pkCard) {
        assert isValid(pkCard);
        int rank = Bits32.extract(pkCard, RANK_INDEX, RANK_LENGTH);
        return Card.Rank.ALL.get(rank);
    }

    /**
     * Returns true if the left card is better than the right one
     *
     * @param Color   trump
     * @param pkCardL (integer that represents a card)
     * @param pkCardR (integer that represents a card)
     * @return True if left card is better than right
     */
    public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
        assert isValid(pkCardL);
        assert isValid(pkCardR);

        if (color(pkCardL).equals(color(pkCardR))) {
            if (!color(pkCardL).equals(trump))
                return (rank(pkCardL).ordinal() > rank(pkCardR).ordinal());
            else
                return (rank(pkCardL).trumpOrdinal() > rank(pkCardR).trumpOrdinal());
        } else
            return color(pkCardL).equals(trump);
    }

    /**
     * Returns the amount of points a card is worth
     *
     * @param Color  trump
     * @param pkCard (integer that represents a card)
     * @return Points this card is worth
     */
    public static int points(Card.Color trump, int pkCard) {
        assert isValid(pkCard);
        //Get the set of points depending on trump or not and if the ordinal of the rank is not between 1 and 8 then get the 9th number, 'default'
        return points[color(pkCard).equals(trump) ? 0 : 1][(rank(pkCard).ordinal() < 0 || rank(pkCard).ordinal() > Card.Rank.COUNT - 1)
                ? Card.Rank.COUNT
                : rank(pkCard).ordinal()];
    }

    /**
     * Returns the symbol and rank
     *
     * @param pkCard (integer that represents a card)
     * @return a String following our representation for each card
     */
    public static String toString(int pkCard) {
        assert isValid(pkCard);
        String string = color(pkCard).toString() + rank(pkCard).toString();
        return string;
    }

}
