package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Player Id enum type that represents the Identification of each player
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public enum PlayerId {
    PLAYER_1, PLAYER_2, PLAYER_3, PLAYER_4;

    public static final List<PlayerId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    public static final int COUNT = ALL.size();

    /**
     * Returns the team in which the player is Player 1 and 3 are in team 1 while
     * player 2 and 4 are in team2
     *
     * @return the team of the player
     */
    public TeamId team() {
        return (this.equals(PLAYER_1) || this.equals(PLAYER_3)) ? TeamId.TEAM_1 : TeamId.TEAM_2;
    }
}
