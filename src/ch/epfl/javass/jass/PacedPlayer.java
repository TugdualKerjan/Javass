package ch.epfl.javass.jass;

import java.util.Map;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

/**
 * Class to create a PacedPlayer responsible for clow playing
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class PacedPlayer implements Player {

	private final Player underlyingPlayer;
	private final long minTime;

	
	/**
     * Makes the underlying player take the same amount of time as a typical player to play
     * 
     * @param underlyingPlayer player to be paced
     * @param minTime time to pace
     */
    public PacedPlayer(Player underlyingPlayer, double minTime) {
        Preconditions.checkArgument(minTime > 0);
        this.underlyingPlayer = underlyingPlayer;
        this.minTime = (long) minTime * 1000; //Seconds to milliseconds
    }
    
    
	/* 
	 * cardToPlay asks the underlying player for a card to play and then waits for the remaining time
	 */
	@Override
	public Card cardToPlay(TurnState state, CardSet hand) {
	    //Causes the player to spend a certain amount of time for each card
		double currentTime = System.currentTimeMillis();
		Card card = underlyingPlayer.cardToPlay(state, hand);
		try {
		    long totalTime;
		    if((totalTime = (minTime - ((long) System.currentTimeMillis() - (long) currentTime))) > 0) {
		        Thread.sleep(totalTime);
		    }
		} catch (InterruptedException e) {}
		return card;	
	}

	

	//All methods underneath call the respective underlyingPlayer method
	@Override
	public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
		underlyingPlayer.setPlayers(ownId, playerNames);
	}	

	@Override
	public void setTrump(Color trump) {
		underlyingPlayer.setTrump(trump);
	}

	@Override
	public void setWinningTeam(TeamId winningTeam) {
		underlyingPlayer.setWinningTeam(winningTeam);
	}

	@Override
	public void updateHand(CardSet newHand) {
		underlyingPlayer.updateHand(newHand);
	}

	@Override
	public void updateScore(Score score) {
		underlyingPlayer.updateScore(score);
	}
	
	@Override
	public void updateTrick(Trick newTrick) {
		underlyingPlayer.updateTrick(newTrick);
	}
}
