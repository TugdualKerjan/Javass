package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * TrickBean is a class that represents a trick but it uses properties of javafx
 * because it will be very useful in the graphical interface to attach observers
 * and then make the interface change when these change.
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class TrickBean {

    private final SimpleObjectProperty<Color> trump = new SimpleObjectProperty<Color>();
    private final SimpleObjectProperty<PlayerId> winningPlayer = new SimpleObjectProperty<PlayerId>();
    private final ObservableMap<PlayerId, Card> trick;

    /**
     * public constructor of class
     */
    public TrickBean() {
        trick = FXCollections.<PlayerId, Card> observableHashMap();
        for (PlayerId p : PlayerId.ALL) {
            trick.put(p, null);
        }
    }

    /**
     * Get the current trump
     * 
     * @return trump
     */
    public SimpleObjectProperty<Color> trumpProperty() {
        return trump;
    }

    /**
     * Set the current trump
     * 
     * @param trump
     */
    public void setTrump(Color trump) {
        this.trump.set(trump);
    }

    /**
     * Get the map of which player has which card
     * 
     * @return ObservableMap of players and their played card
     */
    public ObservableMap<PlayerId, Card> trickProperty() {
        return FXCollections.unmodifiableObservableMap(trick);
    }

    /**
     * Set the trick
     * 
     * @param newTrick
     */
    public void setTrick(Trick newTrick) {
        // Set which player is currently leading the trick?
        winningPlayer
                .set((!newTrick.isEmpty()) ? newTrick.winningPlayer() : null);
        // Which cards have been played?
        for (int i = 0; i < newTrick.size(); ++i) {
            trick.put(newTrick.player(i), newTrick.card(i));
        }
        for (int i = newTrick.size(); i < PlayerId.COUNT; ++i) {
            trick.put(newTrick.player(i), null);
        }
    }

    /**
     * Get the currently winning player of the trick
     * 
     * @return winning player
     */
    public SimpleObjectProperty<PlayerId> winningPlayer() {
        return winningPlayer;
    }
}
