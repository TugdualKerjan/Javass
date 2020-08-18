package ch.epfl.javass.jass;

import ch.epfl.javass.Preconditions;

/**
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class Score {

    public static final Score INITIAL = new Score(0L);
    private long packedScore;

    // Immutable
    private Score(long packedScore) {
        this.packedScore = packedScore;
    }

    /**
     * Method that is used as a constructor (because the class Score is Immutable
     *
     * @param pkScore (long that represents the Score of both teams packed into a string of 64 bits)
     * @return a new Instance of Score
     * @throws IllegalArgumentException if pkScore doesn't represent a valid Score
     */
    public static Score ofPacked(long pkScore) {
        if (PackedScore.isValid(pkScore))
            return new Score(pkScore);
        else
            throw new IllegalArgumentException();

    }

    /**
     * Returns packed score
     *
     * @return long packed score
     */
    public long packed() {
        return packedScore;
    }

    /**
     * Gives the amount of tricks won by a team
     *
     * @param t team
     * @return int amount of tricks won by that team
     */
    public int turnTricks(TeamId t) {
        return PackedScore.turnTricks(packedScore, t);
    }

    /**
     * Gives the amount of points won by a team in current round
     *
     * @param t team
     * @return int amount of points won by that team in current round
     */
    public int turnPoints(TeamId t) {
        return PackedScore.turnPoints(packedScore, t);
    }

    /**
     * Gives the amount of points won by a team in previous games
     *
     * @param t team
     * @return int amount of points won by that team in previous games
     */
    public int gamePoints(TeamId t) {
        return PackedScore.gamePoints(packedScore, t);
    }

    /**
     * Gives the amount of points won by a team in total
     *
     * @param t team
     * @return int amount of points won by that team in total
     */
    public int totalPoints(TeamId t) {
        return PackedScore.totalPoints(packedScore, t);
    }

    /**
     * Adds points to the winning team
     *
     * @param winningTeam team to assign points to
     * @param trickPoints amount of points to give them
     * @return Score updated
     * @throws IllegalArgumentException if the number of trickPoints is negative
     */
    public Score withAdditionalTrick(TeamId winningTeam, int trickPoints) {
        Preconditions.checkArgument(!(trickPoints < 0));
        long points = PackedScore.withAdditionalTrick(packedScore, winningTeam, trickPoints);
        return new Score(points);
    }

    /**
     * Resets points earned and tricks won
     *
     * @return Score updated
     */
    public Score nextTurn() {
        long points = PackedScore.nextTurn(packedScore);
        return new Score(points);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Score) {
            return ((Score) o).packed() == this.packedScore;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.packedScore);
    }

    @Override
    public String toString() {
        return PackedScore.toString(packedScore);
    }
}
