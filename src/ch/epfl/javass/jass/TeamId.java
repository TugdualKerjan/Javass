package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Team Id enum type that consists on two teams
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public enum TeamId {
	TEAM_1, TEAM_2;

	public static final List<TeamId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
	public static final int COUNT = ALL.size();

	/**
	 * Returns the opposite team, if Team1 returns Team2 and if Team2 returns Team1
	 * 
	 * @return the opposite team
	 */
	public TeamId other() {
		return (this.equals(TEAM_1)) ? TEAM_2 : TEAM_1;
	}
}
