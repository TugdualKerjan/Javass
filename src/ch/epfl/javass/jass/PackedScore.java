package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;

/**
 * Class that consits of a group of satic methods that will be used to create Scores
 * and do several things with them such as setting the points for the next trick or turn,
 * or adding points or identifying how many points the winning team has.
 * In this class the scores are represented as a long.
 * 
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 *
 */
public final class PackedScore {
	// Initial value at the start of a game is 0
	public static final long INITIAL = 0L;
	
	private static final int AMOUNT_OF_TRICKS_INDEX = 0;
	private static final int AMOUNT_OF_TRICKS_LENGTH = 4;
	private static final int AMOUNT_OF_TURNPOINTS_INDEX = 4;
    private static final int AMOUNT_OF_TURNPOINTS_LENGTH = 9;
    private static final int AMOUNT_OF_GAMEPOINTS_INDEX = 13;
    private static final int AMOUNT_OF_GAMEPOINTS_LENGTH = 11;
    private static final int AMOUNT_OF_LEAD0_INDEX = 24;
    private static final int AMOUNT_OF_LEAD0_LENGTH = 8;
    private static final int GAME_POINTS_UPPERBOUND = 2000;
    private static final int MAX_POINTS_PER_TRICK = 257;
    private static final int BIT_LENGTH_SCORE_PER_TEAM = 32;

	private PackedScore() {
	}

	/**
	 * Checks if the packed score is valid
	 * 
	 * @param pkScore score you want to verify
	 * @return true if valid
	 */
	public static boolean isValid(long pkScore) {
		long nbPlis1 = Bits64.extract(pkScore, AMOUNT_OF_TRICKS_INDEX, AMOUNT_OF_TRICKS_LENGTH);
		long nbPlis2 = Bits64.extract(pkScore, BIT_LENGTH_SCORE_PER_TEAM + AMOUNT_OF_TRICKS_INDEX, AMOUNT_OF_TRICKS_LENGTH);
		long pointsTour1 = Bits64.extract(pkScore, AMOUNT_OF_TURNPOINTS_INDEX, AMOUNT_OF_TURNPOINTS_LENGTH);
		long pointsTour2 = Bits64.extract(pkScore, BIT_LENGTH_SCORE_PER_TEAM + AMOUNT_OF_TURNPOINTS_INDEX, AMOUNT_OF_TURNPOINTS_LENGTH);
		long pointsPartie1 = Bits64.extract(pkScore, AMOUNT_OF_GAMEPOINTS_INDEX, AMOUNT_OF_GAMEPOINTS_LENGTH);
		long pointsPartie2 = Bits64.extract(pkScore, BIT_LENGTH_SCORE_PER_TEAM + AMOUNT_OF_GAMEPOINTS_INDEX, AMOUNT_OF_GAMEPOINTS_LENGTH);
		long leading0s1 = Bits64.extract(pkScore, AMOUNT_OF_LEAD0_INDEX, AMOUNT_OF_LEAD0_LENGTH);
		long leading0s2 = Bits64.extract(pkScore, BIT_LENGTH_SCORE_PER_TEAM + AMOUNT_OF_LEAD0_INDEX, AMOUNT_OF_LEAD0_LENGTH);

		return nbPlis1 >= 0  && nbPlis1 <= Jass.TRICKS_PER_TURN && nbPlis2 >= 0 && nbPlis2 <= Jass.TRICKS_PER_TURN && pointsTour1 >= 0 && pointsTour1 <= MAX_POINTS_PER_TRICK
				&& pointsTour2 >= 0 && pointsTour2 <= MAX_POINTS_PER_TRICK && pointsPartie1 >= 0 && pointsPartie1 <= GAME_POINTS_UPPERBOUND
				&& pointsPartie2 >= 0 && pointsPartie2 <= GAME_POINTS_UPPERBOUND && leading0s1 == 0 && leading0s2 == 0;
	}

	/**
	 * Packs the current game info into the packedPoints long
	 * 
	 * @param turnTricks1 plis that team1 won
	 * @param turnPoints1 points earned by team1
	 * @param gamePoints1 total points earned by team1
	 * @param turnTricks2 plis that team2 won
	 * @param turnPoints2 points earned by team2
	 * @param gamePoints2 total points earned by team2
	 * @return long representing game info
	 */
	public static long pack(int turnTricks1, int turnPoints1, int gamePoints1, int turnTricks2, int turnPoints2,
			int gamePoints2) {
		long points1 = (long) Bits32.pack(turnTricks1, AMOUNT_OF_TRICKS_LENGTH, turnPoints1, AMOUNT_OF_TURNPOINTS_LENGTH, gamePoints1, AMOUNT_OF_GAMEPOINTS_LENGTH);
		long points2 = (long) Bits32.pack(turnTricks2, AMOUNT_OF_TRICKS_LENGTH, turnPoints2, AMOUNT_OF_TURNPOINTS_LENGTH, gamePoints2, AMOUNT_OF_GAMEPOINTS_LENGTH);
		long packedPoints = Bits64.pack(points1, BIT_LENGTH_SCORE_PER_TEAM, points2, BIT_LENGTH_SCORE_PER_TEAM);

		assert isValid(packedPoints);

		return packedPoints;
	}

	/**
	 * Gives you the amount of tricks that team won in current turn
	 * 
	 * @param pkScore packed score of the current game
	 * @param t       team
	 * @return int amount of tricks that team won in current turn
	 */
	public static int turnTricks(long pkScore, TeamId t) {
		assert isValid(pkScore);
		return (t.equals(TeamId.TEAM_1)) ? 
		        (int) Bits64.extract(pkScore, AMOUNT_OF_TRICKS_INDEX, AMOUNT_OF_TRICKS_LENGTH) 
		        : (int) Bits64.extract(pkScore, 32 + AMOUNT_OF_TRICKS_INDEX, AMOUNT_OF_TRICKS_LENGTH);
	}

	/**
	 * Gives you the amount of points the specified team won in current turn
	 * 
	 * @param pkScore packed score of the current game
	 * @param t       team
	 * @return int amount of points of that team in current turn
	 */
	public static int turnPoints(long pkScore, TeamId t) {
		assert isValid(pkScore);
		return (t.equals(TeamId.TEAM_1)) ? 
		        (int) Bits64.extract(pkScore, AMOUNT_OF_TURNPOINTS_INDEX, AMOUNT_OF_TURNPOINTS_LENGTH) 
		        : (int) Bits64.extract(pkScore, 32 + AMOUNT_OF_TURNPOINTS_INDEX, AMOUNT_OF_TURNPOINTS_LENGTH);
	}

	/**
	 * Gives you the amount of points the specified team won in previous games
	 * 
	 * @param pkScore packed score of the current game
	 * @param t       team
	 * @return int amount of points of that team won in previous games
	 */
	public static int gamePoints(long pkScore, TeamId t) {
		assert isValid(pkScore);
		return (t.equals(TeamId.TEAM_1)) ? 
		        (int) Bits64.extract(pkScore, AMOUNT_OF_GAMEPOINTS_INDEX, AMOUNT_OF_GAMEPOINTS_LENGTH)
				: (int) Bits64.extract(pkScore, 32 + AMOUNT_OF_GAMEPOINTS_INDEX, AMOUNT_OF_GAMEPOINTS_LENGTH);
	}

	/**
	 * Gives you the amount of points the specified team won in total
	 * 
	 * @param pkScore packed score of the current game
	 * @param t       team
	 * @return int amount of points of that team won in total
	 */
	public static int totalPoints(long pkScore, TeamId t) {
		assert isValid(pkScore);
		return turnPoints(pkScore, t) + gamePoints(pkScore, t);
	}

	/**
	 * Updates the packed score of the current game when a trick is won
	 * 
	 * @param pkScore     packed score of the current game
	 * @param winningTeam team that won the trick
	 * @param trickPoints amount of points to give them
	 * @return long the new packed score of the current game
	 */
	public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
		assert isValid(pkScore);

		int nbPlis = turnTricks(pkScore, winningTeam);
		int nbPointsTour = turnPoints(pkScore, winningTeam);
		int nbPointsJeu = gamePoints(pkScore, winningTeam);

		nbPlis += 1;
		nbPointsTour += trickPoints;

		if (nbPlis == Jass.TRICKS_PER_TURN) {
			nbPointsTour += Jass.MATCH_ADDITIONAL_POINTS;
		}

		TeamId losingTeam = winningTeam.other();

		return (winningTeam.equals(TeamId.TEAM_1))
				? pack(nbPlis, nbPointsTour, nbPointsJeu, turnTricks(pkScore, losingTeam),
						turnPoints(pkScore, losingTeam), gamePoints(pkScore, losingTeam))
				: pack(turnTricks(pkScore, losingTeam), turnPoints(pkScore, losingTeam),
						gamePoints(pkScore, losingTeam), nbPlis, nbPointsTour, nbPointsJeu);
	}

	/**
	 * Updates the packed score for a new round
	 * 
	 * @param pkScore packed score of the current game
	 * @return long representing the fresh packed score for a new round
	 */
	public static long nextTurn(long pkScore) {
		assert isValid(pkScore);

		return pack(0, 0, totalPoints(pkScore, TeamId.TEAM_1), 0, 0, totalPoints(pkScore, TeamId.TEAM_2));
	}

	/**
	 * Returns the current packed score as a String
	 * 
	 * @param pkScore the score to transform to a string
	 * @return String representing the information in packed score
	 */
	public static String toString(long pkScore) {
		assert isValid(pkScore);
		// Team1
		int nbPlis1 = turnTricks(pkScore, TeamId.TEAM_1);
		int nbPointsTour1 = turnPoints(pkScore, TeamId.TEAM_1);
		int nbPointsJeu1 = gamePoints(pkScore, TeamId.TEAM_1);

		// Team2
		int nbPlis2 = turnTricks(pkScore, TeamId.TEAM_2);
		int nbPointsTour2 = turnPoints(pkScore, TeamId.TEAM_2);
		int nbPointsJeu2 = gamePoints(pkScore, TeamId.TEAM_2);

		String representation = "(" + nbPlis1 + "," + nbPointsTour1 + "," + nbPointsJeu1 + ")/(" + nbPlis2 + ","
				+ nbPointsTour2 + "," + nbPointsJeu2 + ")";

		return representation;
	}

}
