package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

/**
 * HandBean is a class that contains two observable attributes: the hand and the
 * playable cards and its use is in the Graphical interface, we will be able to
 * attach observers to this two attributes (we will be using javaFX).
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class HandBean {

    private final ObservableList<Card> hand;
    private final ObservableSet<Card> playableCards;

    /**
     * Constructor of the class
     */
    public HandBean() {
        hand = FXCollections.observableArrayList();
        for (int i = 0; i < Jass.HAND_SIZE; ++i) {
            hand.add(null);
        }
        playableCards = FXCollections.observableSet();
    }

    /**
     * Get the hand
     *
     * @return ObservableList of cards the player currently has
     */
    public ObservableList<Card> handProperty() {
        return FXCollections.unmodifiableObservableList(hand);
    }

    /**
     * Set the hand of the player
     *
     * @param newHand cardSet to set
     */
    public void setHand(CardSet newHand) {
        // If new hand then replace all
        if (newHand.size() == Jass.HAND_SIZE) {
            for (int i = 0; i < newHand.size(); i++) {
                hand.set(i, newHand.get(i));
            }
            // Else only set to null the ones that aren't null and not in
            // the hand anymore
        } else {
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i) != null && !newHand.contains(hand.get(i))) {
                    hand.set(i, null);
                }
            }
        }
    }

    /**
     * Get the playable cards
     *
     * @return ObservableSet of cards the player can currently play
     */
    public ObservableSet<Card> playableCards() {
        return FXCollections.unmodifiableObservableSet(playableCards);
    }

    /**
     * define what cards the player can currently play
     *
     * @param newPlayableCards cards the player can play
     */
    public void setPlayableCards(CardSet newPlayableCards) {
        playableCards.clear();
        for (int i = 0; i < newPlayableCards.size(); i++) {
            playableCards.add(newPlayableCards.get(i));
        }
    }
}
