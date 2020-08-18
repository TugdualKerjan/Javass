package ch.epfl.javass.jass;

import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

import java.util.*;
//Check again this class with the corrections

//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

/**
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class JassGame {

    // Random number generators
    private final Random shuffleRng;
    private final Random trumpRng;
    // Maps of players and their hands
    private final Map<PlayerId, Player> players;
    // Current trump
    private Color trump;
    private boolean firstTime;
    private Map<PlayerId, CardSet> handPlayer;
    private Map<PlayerId, String> playerNames;

    // First player of a trick
    private PlayerId firstPlayerOfTurn;

    // State and score
    private TurnState state;
    private Score score;

    /**
     * Jass game assigns the variables to the object, creates a first turn,
     * assigns randomly the hands to each player, and updates the state of the
     * game to each player.
     *
     * @param rngSeed
     * @param playerIdtoPlayer
     * @param playerNames
     */
    public JassGame(long rngSeed, Map<PlayerId, Player> playerIdtoPlayer,
                    Map<PlayerId, String> playerNames) {
        // Random number generator
        Random rng = new Random(rngSeed);
        this.shuffleRng = new Random(rng.nextLong());
        this.trumpRng = new Random(rng.nextLong());

        // Initial score
        score = Score.INITIAL;

        // New hands of players
        handPlayer = new HashMap<PlayerId, CardSet>();

        // Players
        players = Collections.unmodifiableMap(new HashMap<>(playerIdtoPlayer));

        // State
        state = TurnState.ofPackedComponents(0, PackedCardSet.ALL_CARDS, 0);

        firstTime = true;
        // save immutable copy of playernames and give this one as argument
        // Tell the players about the others
        this.playerNames = Collections
                .unmodifiableMap(new HashMap<>(playerNames));
        for (PlayerId player : PlayerId.ALL) {
            playerIdtoPlayer.get(player).setPlayers(player, this.playerNames);
        }

        // Setup the turn, state and update players on the trick and score
        nextTurn();
        state = TurnState.initial(trump, score, firstPlayerOfTurn);
        // updateScoreForPlayers();
        // updateTrickForPlayers();
    }

    /**
     * Game over if one of the teams has more than Jass.WINNING_POINTS
     *
     * @return true if a team has more than the required points to win the game
     */
    public boolean isGameOver() {
        boolean gameOver = state.score().totalPoints(TeamId.TEAM_1) >= Jass.WINNING_POINTS
                || state.score()
                .totalPoints(TeamId.TEAM_2) >= Jass.WINNING_POINTS;
        if (firstTime && gameOver) {
            firstTime = false;
            if (state.score()
                    .totalPoints(TeamId.TEAM_1) >= Jass.WINNING_POINTS)
                setWinningTeam(TeamId.TEAM_1);
            else
                setWinningTeam(TeamId.TEAM_2);
        }

        return gameOver;
    }

    /**
     * Analises the current situation of the game: Checks if the game is over,
     * if we riched the end of the trick, and if we have to create a new trick,
     * the same for the turn. When this first part finished it informs to each
     * player of the current situation of the game. And finally if the game
     * isn't over: the method make all of the players play.
     */
    public void advanceToEndOfNextTrick() {

        if (isGameOver()) {
            return;
        }
        // If it's full then collect the trick
        if (state.trick().isFull())
            state = state.withTrickCollected();


        // If it's terminal then new turn
        if (state.isTerminal()) {
            nextTurn();
            state = TurnState.initial(trump, state.score().nextTurn(),
                    firstPlayerOfTurn);
        }
        updateScoreForPlayers();
        // Update the players

        updateTrickForPlayers();
        // If it's over then return


        if (isGameOver()) {
            return;
        }


        // Make the players play
        for (int i = 0; i < PlayerId.COUNT; ++i) {
            PlayerId nextPlayer = state.nextPlayer();
            makePlayerPlay(nextPlayer);
        }

    }

    private void setWinningTeam(TeamId t) {
        for (PlayerId player : PlayerId.values()) {
            players.get(player).setWinningTeam(t);
        }
    }

    /**
     * Create a deck of all cards, shuffle them and assign each 9 cards to a
     * player, then assign the firstplayer of the following turn (if it's not
     * the first turn of the game)
     */
    private void nextTurn() {
        // Shuffle cards
        List<Card> deck = new ArrayList<Card>();
        for (int i = 0; i < CardSet.ALL_CARDS.size(); ++i) {
            deck.add(CardSet.ALL_CARDS.get(i));
        }
        Collections.shuffle(deck, shuffleRng);

        // Choose new trump
        trump = Color.ALL.get(trumpRng.nextInt(Color.ALL.size()));

        // Clear handPlayer
        handPlayer.clear();

        int index = 0;
        for (PlayerId player : PlayerId.ALL) {
            // For each player get 9 cards
            List<Card> newHand = new ArrayList<Card>();
            for (int i = 0; i < Jass.HAND_SIZE; ++i) {
                newHand.add(deck.get(i + index));
            }

            // Update trump and hand of player
            players.get(player).setTrump(trump);
            players.get(player).updateHand(CardSet.of(newHand));
            handPlayer.put(player, CardSet.of(newHand));

            // Check if this is the first turn and if so if this player has the
            // DIAMOND SEVEN (First player)
            if (newHand.contains(Card.of(Color.DIAMOND, Rank.SEVEN))
                    && state.score().totalPoints(TeamId.TEAM_1) == 0
                    && state.score().totalPoints(TeamId.TEAM_2) == 0) {
                firstPlayerOfTurn = player;
            }
            index += Jass.HAND_SIZE;
        }

        // If it isn't the first turn then next player is the next one in
        // PlayerId
        if (state.score().totalPoints(TeamId.TEAM_1)
                + state.score().totalPoints(TeamId.TEAM_2) != 0) {
            int ordinal = (firstPlayerOfTurn.ordinal() + 1)
                    % PlayerId.ALL.size();
            firstPlayerOfTurn = PlayerId.ALL.get(
                    (ordinal >= 0) ? ordinal : ordinal + PlayerId.ALL.size());
        }
    }

    /**
     * Make the player play a card and update
     *
     * @param player to player a card
     */
    private void makePlayerPlay(PlayerId player) {
        // Get the card to play
        Card cardPlayed = players.get(player).cardToPlay(state,
                handPlayer.get(player));

        // Update the state
        this.state = state.withNewCardPlayed(cardPlayed);

        // Set the new hand of that player
        CardSet newCardSet = handPlayer.get(player).remove(cardPlayed);
        handPlayer.put(player, newCardSet);
        players.get(player).updateHand(newCardSet);

        // Update the other players on this new card played
        updateTrickForPlayers();
        // updateScoreForPlayers();
    }

    /**
     * Update all players on tricks
     */
    private void updateTrickForPlayers() {
        for (PlayerId player : PlayerId.ALL) {
            players.get(player).updateTrick(state.trick());
        }
    }

    /**
     * Update all players on scores
     */
    private void updateScoreForPlayers() {
        for (PlayerId player : PlayerId.ALL) {
            players.get(player).updateScore(state.score());
        }
    }
}
