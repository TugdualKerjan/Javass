package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

import java.util.StringJoiner;

/**
 * Class that consists on a set of static methods that will be used for the
 * Trick class . This methods do several things as creating new tricks in the
 * form of an integer and adding cards to this trick. In this class the Tricks
 * are represented as an Integer.
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class PackedTrick {
    public static final int INVALID = -1;
    private static final int BITS_USED_PER_CARD = 6;
    private static final int BITS_USED_PER_PLAYER = 2;
    private static final int BITS_USED_PER_TRUMP = 2;
    private static final int BITS_USED_PER_INDEX = 4;
    private static final int MINIMUM_INDEX = 0;
    private static final int INITIAL_INDEX = 0;
    private static final int MAXIMUM_CARDS_PLAYED_PER_TRICK = 4;
    private static final int NUMBER_OF_PLAYERS = 4;
    private static final int FIRST_CARD = 0;
    private static final int SECOND_CARD = 1;
    private static final int THIRD_CARD = 2;
    private static final int FOURTH_CARD = 3;
    private static final int EMPTY = 0;
    private static final int POSITION_INDEX_BITS_REP = 24;
    private static final int POSITION_PLAYER_BITS_REP = 28;
    private static final int POSITION_TRUMP_BITS_REP = 30;
    /*
     * Non instantiable class
     */
    private PackedTrick() {
    }

    /**
     * Checks if the packedTrick is valid
     *
     * @param pkTrick, the trick to verify
     * @return true if pkTrick valid
     */
    public static boolean isValid(int pkTrick) {
        int index = index(pkTrick);

        if (index >= Jass.TRICKS_PER_TURN || index < MINIMUM_INDEX) {
            return false;
        }

        int carte0 = card(pkTrick, FIRST_CARD);
        int carte1 = card(pkTrick, SECOND_CARD);
        int carte2 = card(pkTrick, THIRD_CARD);
        int carte3 = card(pkTrick, FOURTH_CARD);

        if (carte0 == PackedCard.INVALID && carte1 == PackedCard.INVALID
                && carte2 == PackedCard.INVALID
                && carte3 == PackedCard.INVALID) {
            return true;
        }

        if (PackedCard.isValid(carte0) && carte1 == PackedCard.INVALID
                && carte2 == PackedCard.INVALID
                && carte3 == PackedCard.INVALID) {
            return true;
        }
        if (PackedCard.isValid(carte0) && PackedCard.isValid(carte1)
                && carte2 == PackedCard.INVALID
                && carte3 == PackedCard.INVALID) {
            return true;
        }
        if (PackedCard.isValid(carte0) && PackedCard.isValid(carte1)
                && PackedCard.isValid(carte2) && carte3 == PackedCard.INVALID) {
            return true;
        }
        if (PackedCard.isValid(carte0) && PackedCard.isValid(carte1)
                && PackedCard.isValid(carte2) && PackedCard.isValid(carte3)) {
            return true;
        }

        return false;
    }

    /**
     * Create an empty packedTrick
     *
     * @param trump       the color of the trump Cards
     * @param firstPlayer the first player to play
     * @return int packedTrick
     */
    public static int firstEmpty(Color trump, PlayerId firstPlayer) {
        return Bits32.pack(PackedCard.INVALID, BITS_USED_PER_CARD,
                PackedCard.INVALID, BITS_USED_PER_CARD, PackedCard.INVALID,
                BITS_USED_PER_CARD, PackedCard.INVALID, BITS_USED_PER_CARD,
                INITIAL_INDEX, BITS_USED_PER_INDEX, firstPlayer.ordinal(),
                BITS_USED_PER_PLAYER, trump.ordinal(), BITS_USED_PER_TRUMP);
    }

    /**
     * Based of the old packedTrick create a fresh version
     *
     * @param pkTrick the packedTrick to base the new one on
     * @return packedTrick with just the winning player, index and trump
     */
    public static int nextEmpty(int pkTrick) {
        assert (isValid(pkTrick));
        if (isLast(pkTrick))
            return INVALID;

        return Bits32.pack(PackedCard.INVALID, BITS_USED_PER_CARD,
                PackedCard.INVALID, BITS_USED_PER_CARD, PackedCard.INVALID,
                BITS_USED_PER_CARD, PackedCard.INVALID, BITS_USED_PER_CARD,
                index(pkTrick) + 1, BITS_USED_PER_INDEX,
                winningPlayer(pkTrick).ordinal(), BITS_USED_PER_PLAYER,
                trump(pkTrick).ordinal(), BITS_USED_PER_TRUMP);
    }

    /**
     * Check if packedTrick is empty
     *
     * @param pkTrick the round to check
     * @return true if it is empty
     */
    public static boolean isEmpty(int pkTrick) {
        assert isValid(pkTrick);
        return size(pkTrick) == EMPTY;
    }

    /**
     * Check if packedTrick is full
     *
     * @param pkTrick the round to check
     * @return true if it is full
     */
    public static boolean isFull(int pkTrick) {
        assert isValid(pkTrick);
        return size(pkTrick) == MAXIMUM_CARDS_PLAYED_PER_TRICK;
    }

    /**
     * Check if the given is the last trick
     *
     * @param pkTrick round to check
     * @return true if last round
     */
    public static boolean isLast(int pkTrick) {
        assert isValid(pkTrick);
        return index(pkTrick) == Jass.TRICKS_PER_TURN - 1;
    }

    /**
     * Return the amount of cards in the packedTrick
     *
     * @param pkTrick the trick to count
     * @return int current amount of cards in that trick
     */
    public static int size(int pkTrick) {
        assert isValid(pkTrick);
        int compt = 0;
        for (int i = 0; i < MAXIMUM_CARDS_PLAYED_PER_TRICK; ++i) {
            if (PackedCard.isValid(card(pkTrick, i))) {
                compt += 1;
            }
        }
        return compt;
    }

    /**
     * Returns the color of the trump
     *
     * @param pkTrick round to get trump
     * @return color of trump (not orange)
     */
    public static Color trump(int pkTrick) {
        assert (isValid(pkTrick));
        int atout = Bits32.extract(pkTrick, POSITION_TRUMP_BITS_REP,
                BITS_USED_PER_TRUMP);
        return Card.Color.ALL.get(atout);
    }

    /**
     * Return the player who will play at a certain index
     *
     * @param pkTrick trick to check
     * @param index   of the player
     * @return player at that index
     */
    public static PlayerId player(int pkTrick, int index) {
        assert isValid(pkTrick);
        int player1 = Bits32.extract(pkTrick, POSITION_PLAYER_BITS_REP,
                BITS_USED_PER_PLAYER);
        int thePlayer = ((player1 + index) % NUMBER_OF_PLAYERS < 0)
                ? ((player1 + index) % NUMBER_OF_PLAYERS) + NUMBER_OF_PLAYERS
                : (player1 + index) % NUMBER_OF_PLAYERS;
        return PlayerId.ALL.get(thePlayer);

    }

    /**
     * Return the trick number
     *
     * @param pkTrick trick to check
     * @return index of this trick
     */
    public static int index(int pkTrick) {
        return Bits32.extract(pkTrick, POSITION_INDEX_BITS_REP,
                BITS_USED_PER_INDEX);
    }

    /**
     * Return the card at a certain index
     *
     * @param pkTrick trick with the card
     * @param index   of the card
     * @return card at that index
     */
    public static int card(int pkTrick, int index) {
        return Bits32.extract(pkTrick, index * BITS_USED_PER_CARD,
                BITS_USED_PER_CARD);
    }

    /**
     * Add a card to a trick
     *
     * @param pkTrick trick to add card to
     * @param pkCard  card to add
     * @return packedTrick with the card in it
     */
    public static int withAddedCard(int pkTrick, int pkCard) {
        assert isValid(pkTrick);
        assert PackedCard.isValid(pkCard);
        int cardIndex = size(pkTrick);
        return ((pkTrick & ~Bits32.mask(cardIndex * BITS_USED_PER_CARD,
                BITS_USED_PER_CARD))
                | (pkCard << cardIndex * BITS_USED_PER_CARD));
    }

    /**
     * Return base color used in the given trick, the first card is supposed to
     * have been played
     *
     * @param pkTrick trick to check
     * @return color used as base
     */
    public static Color baseColor(int pkTrick) {
        assert isValid(pkTrick);
        return PackedCard.color(card(pkTrick, FIRST_CARD));
    }

    /**
     * Return the cards that a certain hand can play taking into acount the
     * given trick
     *
     * @param pkTrick trick to compare to
     * @param pkHand  hand with cards
     * @return packedCardSet containing all cards you can use
     */
    public static long playableCards(int pkTrick, long pkHand) {
        // Check that the hand of cards is valid
        assert PackedCardSet.isValid(pkHand);
        // Check that the current game is valid
        assert isValid(pkTrick);
        // If first player to play then can play any card
        if (isEmpty(pkTrick)) {
            return pkHand;
        }

        // Cards currently in game
        int[] cartes = new int[]{card(pkTrick, FIRST_CARD),
                card(pkTrick, SECOND_CARD), card(pkTrick, THIRD_CARD),
                card(pkTrick, FOURTH_CARD)};

        // Basecolor and trump
        Color base = baseColor(pkTrick);
        Color atout = trump(pkTrick);

        // Find the best card
        int bestCard = cartes[0];
        for (int i = 1; i < size(pkTrick); i++) {
            if (!PackedCard.isBetter(atout, bestCard, cartes[i]))
                bestCard = cartes[i];
        }

        // Follow
        long baseCards = PackedCardSet.intersection(pkHand,
                PackedCardSet.subsetOfColor(pkHand, base));

        // Cut
        long trumpCards = PackedCardSet.intersection(pkHand,
                PackedCard.color(bestCard).equals(atout)
                        ? PackedCardSet.trumpAbove(bestCard)
                        : PackedCardSet.subsetOfColor(pkHand, atout));

        if (atout.equals(base)) {
            // Trump as base and no trumps apart from JACK means you can play
            // anycard
            int valet = PackedCard.pack(atout, Rank.JACK);
            if (PackedCardSet.size(baseCards) == 1
                    && PackedCardSet.get(baseCards, FIRST_CARD) == valet) {
                return pkHand;
            }
        } else {
            // If no base color cards, can play any card
            if (baseCards == PackedCardSet.EMPTY) {
                // If you don't have any trump cards that souscoupent
                if (trumpCards == PackedCardSet.EMPTY) {
                    // If you don't have any trump cards, nor base color cards
                    // then play anything
                    if (PackedCardSet.subsetOfColor(pkHand,
                            atout) == PackedCardSet.EMPTY) {
                        return pkHand;
                    }
                    // If you have trump cards
                    else {
                        // If you have any other cards apart from trump cards
                        // under the max one then
                        // play those
                        long possiblePlayableCards = PackedCardSet
                                .difference(pkHand, PackedCardSet.subsetOfColor(
                                        PackedCardSet.ALL_CARDS, atout));
                        if (possiblePlayableCards != PackedCardSet.EMPTY) {
                            return possiblePlayableCards;
                        }
                        // Otherwise play any card
                        else {
                            return pkHand;
                        }
                    }
                }
                // If you can cut with something
                else {
                    // You can play all cards apart from the ones that
                    // souscoupent
                    return PackedCardSet.union(
                            PackedCardSet.difference(pkHand,
                                    PackedCardSet.subsetOfColor(
                                            PackedCardSet.ALL_CARDS, atout)),
                            trumpCards);
                }
            }
        }

        // If the trump isn't base then play any card of baseColor or trumpCard
        long result = PackedCardSet.union(baseCards, trumpCards);

        // None of both? Play any card
        return (result == EMPTY) ? pkHand : result;
    }

    /**
     * Return how many points a packedTrick is worth
     *
     * @param pkTrick to calculate worth
     * @return amount of points worth
     */
    public static int points(int pkTrick) {
        assert isValid(pkTrick);
        int sum = 0;
        Color trump = trump(pkTrick);
        for (int i = 0; i < size(pkTrick); ++i) {
            sum += PackedCard.points(trump, card(pkTrick, i));
        }
        if (isLast(pkTrick))
            sum += Jass.LAST_TRICK_ADDITIONAL_POINTS;
        return sum;
    }

    /**
     * Return the player currently winning the given trick
     *
     * @param pkTrick trick to check
     * @return player currently winning
     */
    public static PlayerId winningPlayer(int pkTrick) {
        assert isValid(pkTrick);
        int[] cartes = new int[]{card(pkTrick, FIRST_CARD),
                card(pkTrick, SECOND_CARD), card(pkTrick, THIRD_CARD),
                card(pkTrick, FOURTH_CARD)};
        int carteGagnante = cartes[0];
        int indexCarteGagnante = 0;
        for (int i = 1; i < size(pkTrick); ++i) {
            if (PackedCard.isBetter(trump(pkTrick), cartes[i], carteGagnante)) {

                carteGagnante = cartes[i];
                indexCarteGagnante = i;
            }
        }

        return player(pkTrick, indexCarteGagnante);
    }

    /**
     * Display the played cards in packedTrick
     *
     * @param pkTrick trick to display
     * @return String that contains the information
     */
    public static String toString(int pkTrick) {
        assert isValid(pkTrick);
        StringJoiner s = new StringJoiner(",", "{", "}");
        for (int i = 0; i < size(pkTrick); i++) {
            s.add(PackedCard.toString(card(pkTrick, i)));
        }
        return "Trick: " + index(pkTrick) + ", trump: "
                + trump(pkTrick).toString() + ", first player: "
                + player(pkTrick, 0) + ", cards played: " + s.toString();
    }
}
