package ch.epfl.javass.jass;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public class MctsPlayerTestOrigin {
    public static void main(String[] args) {
        CardSet hand = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Rank.EIGHT))
                .add(Card.of(Color.SPADE, Rank.NINE))
                .add(Card.of(Color.SPADE, Rank.TEN))
                .add(Card.of(Color.HEART, Rank.SIX))
                .add(Card.of(Color.HEART, Rank.SEVEN))
                .add(Card.of(Color.HEART, Rank.EIGHT))
                .add(Card.of(Color.HEART, Rank.NINE))
                .add(Card.of(Color.HEART, Rank.TEN))
                .add(Card.of(Color.HEART, Rank.JACK));
        TurnState turn = TurnState.initial(Color.SPADE, Score.INITIAL, PlayerId.PLAYER_1);
        turn = turn.withNewCardPlayedAndTrickCollected(Card.of(Color.SPADE, Rank.JACK));
        Player player = new MctsPlayer(PlayerId.PLAYER_2, 0, 100000);
        System.out.println(player.cardToPlay(turn, hand));
    }
}
