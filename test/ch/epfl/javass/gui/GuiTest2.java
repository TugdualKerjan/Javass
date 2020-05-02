package ch.epfl.javass.gui;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.TurnState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

public final class GuiTest2 extends Application {
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Map<PlayerId, String> playerNames = new EnumMap<>(PlayerId.class);
        PlayerId.ALL.forEach(p -> playerNames.put(p, p.name()));
        ScoreBean scoreBean = new ScoreBean();
        HandBean handBean = new HandBean();
        TrickBean trickBean = new TrickBean();
        GraphicalPlayer g = new GraphicalPlayer(PlayerId.PLAYER_2, playerNames, scoreBean, trickBean, handBean, new ArrayBlockingQueue<>(1));
        g.createStage().show();

        new AnimationTimer() {
            long now0 = 0;
            TurnState s = TurnState.initial(Color.SPADE,
                    Score.INITIAL,
                    PlayerId.PLAYER_3);
            CardSet d = CardSet.ALL_CARDS;

            @Override
            public void handle(long now) {
                if (now - now0 < 1_000_000_000L || s.isTerminal())
                    return;
                now0 = now;

                s = s.withNewCardPlayed(d.get(0));
                d = d.remove(d.get(0));
                trickBean.setTrump(s.trick().trump());
                trickBean.setTrick(s.trick());

                if (s.trick().isFull()) {
                    s = s.withTrickCollected();
                    for (TeamId t: TeamId.ALL)
                        scoreBean.setTurnPoints(t, s.score().turnPoints(t));
                }
            }
        }.start();
    }
}