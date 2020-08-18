package ch.epfl.javass.gui;

import ch.epfl.javass.jass.*;
import ch.epfl.javass.jass.Card.Color;
import javafx.application.Platform;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Class that represents a player and will be as its name says the adapter
 * between the Jass Game and the Graphical player graphical interface.
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class GraphicalPlayerAdapter implements Player {
    //    private boolean iWantRevenge;
    private final static int SIZE_QUEUE = 1;
    private final ScoreBean scoreBean;
    private final TrickBean trickBean;
    private final HandBean handBean;
    private final ArrayBlockingQueue<Card> commQueue;
    private GraphicalPlayer graphicalPlayer;

    /**
     * public constructor of the Graphical player adapter takes no arguments
     * because everything will be updated by the jass game.
     */
    public GraphicalPlayerAdapter() {
        scoreBean = new ScoreBean();
        trickBean = new TrickBean();
        handBean = new HandBean();
        commQueue = new ArrayBlockingQueue<>(SIZE_QUEUE);
    }

    /*
     * method that will inform of the state and the current hand to the
     * graphical player and after it will read and return with card he wants to
     * play.
     *
     * @see ch.epfl.javass.jass.Player#cardToPlay(ch.epfl.javass.jass.TurnState,
     * ch.epfl.javass.jass.CardSet)
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        Card toPlay;
        Platform.runLater(() -> {
            handBean.setPlayableCards(state.trick().playableCards(hand));
        });
        try {
            toPlay = commQueue.take();
            // we set all the cards to not playable after having played your
            // card because we don't want the player to be able to play any
            // cards after he played a card and until he is not asked again for
            // which card he wants to play
            Platform.runLater(() -> handBean.setPlayableCards(CardSet.EMPTY));
        } catch (InterruptedException e) {
            throw new IllegalStateException();
        }
        return toPlay;

    }

    /*
     * method that will create the graphical player and display it on the
     * computer. At the same time as informing the graphical player of the names
     * of all the players.
     *
     * @see ch.epfl.javass.jass.Player#setPlayers(ch.epfl.javass.jass.PlayerId,
     * java.util.Map)
     */
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        graphicalPlayer = new GraphicalPlayer(ownId, playerNames, scoreBean,
                trickBean, handBean, commQueue);
        Platform.runLater(() -> {
            graphicalPlayer.createStage().show();
        });
    }

    /*
     * will update the state of its current hand
     *
     * @see ch.epfl.javass.jass.Player#updateHand(ch.epfl.javass.jass.CardSet)
     */
    @Override
    public void updateHand(CardSet newHand) {
        Platform.runLater(() -> {
            handBean.setHand(newHand);
        });

    }

    /*
     * will update the new trum
     *
     * @see ch.epfl.javass.jass.Player#setTrump(ch.epfl.javass.jass.Card.Color)
     */
    @Override
    public void setTrump(Color trump) {
        Platform.runLater(() -> {
            trickBean.setTrump(trump);
        });
    }

    /*
     * will update the trick and set playable card to empty so as the graphical
     * player has to wait to play a new card
     *
     * @see ch.epfl.javass.jass.Player#updateTrick(ch.epfl.javass.jass.Trick)
     */
    @Override
    public void updateTrick(Trick newTrick) {
        Platform.runLater(() -> {
            trickBean.setTrick(newTrick);
        });
    }

    /*
     * will update the score for both teams
     *
     * @see ch.epfl.javass.jass.Player#updateScore(ch.epfl.javass.jass.Score)
     */
    @Override
    public void updateScore(Score score) {
        Platform.runLater(() -> {
            updateScoreTeam(score, TeamId.TEAM_1);
            updateScoreTeam(score, TeamId.TEAM_2);
        });

    }

    /*
     * will set the winning team and it will serve to know that the game is
     * finished
     *
     * @see
     * ch.epfl.javass.jass.Player#setWinningTeam(ch.epfl.javass.jass.TeamId)
     */
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        Platform.runLater(() -> {
            scoreBean.setWinningTeam(winningTeam);
        });

    }

    /**
     * will update the score with the given score to the corresponding team
     *
     * @param score (new score)
     * @param team
     */
    private void updateScoreTeam(Score score, TeamId team) {

        scoreBean.setGamePoints(team, score.gamePoints(team));
        scoreBean.setTotalPoints(team, score.totalPoints(team));
        scoreBean.setTurnPoints(team, score.turnPoints(team));
    }


    @Override
    public boolean doYouWantRevenge() {
        return graphicalPlayer.wantRevenge();
    }


}
