package ch.epfl.javass.gui;

import ch.epfl.javass.jass.TeamId;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;;

/**
 * ScoreBean is a class that represents the score and will be used in the
 * Graphical interface to attach observers to its attributes and then the
 * interface will change depending when this values change.(we will use javaFX)
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class ScoreBean {

    private final SimpleIntegerProperty[] turnPoints = new SimpleIntegerProperty[TeamId.COUNT];
    private final SimpleIntegerProperty[] gamePoints = new SimpleIntegerProperty[TeamId.COUNT];
    private final SimpleIntegerProperty[] totalPoints = new SimpleIntegerProperty[TeamId.COUNT];
    private final SimpleObjectProperty<TeamId> winningTeam = new SimpleObjectProperty<>();

    /**
     * Constructor of the class
     */
    public ScoreBean() {
        for (TeamId t : TeamId.ALL) {
            turnPoints[t.ordinal()] = new SimpleIntegerProperty();
            gamePoints[t.ordinal()] = new SimpleIntegerProperty();
            totalPoints[t.ordinal()] = new SimpleIntegerProperty();
        }
    }

    /**
     * Get turn points
     * 
     * @param team
     *            to get them from
     * @return turnPointsProperty
     */
    public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
        return turnPoints[team.ordinal()];
    }

    /**
     * Set turn points
     * 
     * @param team
     *            to set them to
     * @param newTurnPoints
     *            to set
     */
    public void setTurnPoints(TeamId team, int newTurnPoints) {
        turnPoints[team.ordinal()].set(newTurnPoints);
    }

    /**
     * Get game points
     * 
     * @param team
     *            to get them from
     * @return gamePointsProperty
     */
    public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
        return gamePoints[team.ordinal()];
    }

    /**
     * Set game points
     * 
     * @param team
     *            to set them to
     * @param newGamePoints
     *            to set
     */
    public void setGamePoints(TeamId team, int newGamePoints) {
        gamePoints[team.ordinal()].set(newGamePoints);
    }

    /**
     * Get total points
     * 
     * @param team
     *            to get them from
     * @return totalPointsProperty
     */
    public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
        return totalPoints[team.ordinal()];
    }

    /**
     * Set total points
     * 
     * @param team
     *            to set them to
     * @param newTotalPoints
     *            to set
     */
    public void setTotalPoints(TeamId team, int newTotalPoints) {
        totalPoints[team.ordinal()].set(newTotalPoints);
    }

    /**
     * Get the winning team
     * 
     * @return winningTeam
     */
    public ReadOnlyObjectProperty<TeamId> winningTeamProperty() {
        return winningTeam;
    }

    /**
     * Set winning team
     * 
     * @param winningTeam
     *            winning team
     */
    public void setWinningTeam(TeamId winningTeam) {
        this.winningTeam.set(winningTeam);
    }
}
