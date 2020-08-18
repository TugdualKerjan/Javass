package ch.epfl.javass.jass;

import java.util.SplittableRandom;
import java.util.StringJoiner;

/**
 * Monte carlo tree search allows to create a player that can predict the
 * probability of winning based off a created tree of possible future random
 * hands
 *
 * @author Tugdual Kerjan (297804)
 * @author Marcel Torne (299366)
 */
public final class MctsPlayer implements Player {

    private final static double CONSTANT_C = 40;
    private final static int MIN_ITERATIONS_ALLOWED = 9;
    private final int ownId;
    private final SplittableRandom rng;
    private final int iterations;
    private Node origin;

    /**
     * Constructor of a player that uses the Monte Carlo Tree Search Algorithm to play each card.
     *
     * @param ownId
     * @param rngSeed
     * @param iterations
     * @throws IllegalArgumentExcception if we give a number of iterations less than 9
     */
    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
        if (iterations < MIN_ITERATIONS_ALLOWED) {
            throw new IllegalArgumentException("Less than 9 iterations");
        }
        this.ownId = ownId.ordinal();
        this.rng = new SplittableRandom(rngSeed);
        this.iterations = iterations;
    }

    /**
     * method that chooses the best Card to play making use of the MCTS
     * algorithm given the hand of the current player and the state of the turn
     *
     * @param state of the turn and hand of the player
     * @return best Card to play
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        // Original node is latest card played
        origin = new Node(rng, ownId, TeamId.TEAM_1, state, hand.packed(),
                state.trick().playableCards(hand).packed());
        for (int i = 0; i < iterations; i++) {
            origin.iterate();
        }
        return origin.bestCardToPlay();
    }

    private static class Node {
        private final TurnState state;
        private final SplittableRandom random;
        private final int ownId;
        private final TeamId team;

        private Node[] children;
        private long unSimulatedCards;
        private double totalPreviousPointsObtained = 0;
        private int randomSimluatedGames = 0;
        private long playerHand;


        /**
         * Constructor of a Node
         *
         * @param random number generator (to simulate the turn),Id of the
         *               player(to check who should play the following cards)
         *               parent Node(to add the points from the bottom to the top),
         *               state of the Node(for the simulation and playable cards of
         *               the child hand of the player (for the simulation),
         *               unsimulated cards that are the different possibilities for
         *               the childs
         */
        private Node(SplittableRandom random, int ownId, TeamId team,
                     TurnState state, long playerHand, long unsimulatedCards) {
            this.playerHand = playerHand;
            this.team = team;
            this.ownId = ownId;
            this.random = random;
            this.state = state;
            this.unSimulatedCards = unsimulatedCards;
            this.children = new Node[PackedCardSet.size(unsimulatedCards)];
        }

        /**
         * Based off the players hand add the next possible card Additional
         * checking if the future state of the child is terminal because there
         * will be no future player
         * <p>
         * Adds the child into the parent array at the last empty space
         *
         * @return Node created
         */
        private Node addChild() {
            int childCard = PackedCardSet.get(unSimulatedCards, 0);
            TurnState newState = state.withNewCardPlayedAndTrickCollected(
                    Card.ofPacked(childCard));
            Node child;
            if (newState.isTerminal()) {
                child = new Node(random, ownId, state.nextPlayer().team(),
                        newState, PackedCardSet.EMPTY, PackedCardSet.EMPTY);
            } else {
                child = (newState.nextPlayer().ordinal() == ownId)
                        ? new Node(random, ownId, state.nextPlayer().team(),
                        newState,
                        PackedCardSet.remove(playerHand, childCard),
                        PackedTrick.playableCards(
                                newState.packedTrick(),
                                PackedCardSet.remove(playerHand,
                                        childCard)))
                        : new Node(random, ownId, state.nextPlayer().team(),
                        newState,
                        PackedCardSet.remove(playerHand, childCard),
                        PackedTrick.playableCards(
                                newState.packedTrick(),
                                PackedCardSet.difference(
                                        newState.packedUnplayedCards(),
                                        PackedCardSet.remove(playerHand,
                                                childCard))));

            }
            children[childNodeSize()] = child;
            unSimulatedCards = PackedCardSet.remove(unSimulatedCards,
                    childCard);
            return child;
        }

        /**
         * Will compute the values of the MCTS formula for every child given a
         * constant c and will return the index of the one with the highest
         * value.
         * <p>
         * We use c=40 for the proper MCTS formula and c=0 to get the index of
         * the card with the maximum ratio of points per simulated turn.
         *
         * @param constantC
         * @return the index of the child in the array with the highest value of
         * the MCTS formula
         */
        private int bestChildNodeIndex(double constantC) {
            int bestIndex = 0;
            double bestScore = 0.0;
            double score;
            for (int n = 0; n < children.length; n++) {
                if (bestScore <= (score = formulaV(children[n], constantC))) {
                    bestIndex = n;
                    bestScore = score;
                }
            }
            return bestIndex;
        }

        /**
         * MCTS formula
         *
         * @param child     to which we apply the formula
         * @param constantC
         * @return the result of the formula for the given child
         */
        private double formulaV(Node child, double constantC) {
            if (child == null)
                return Double.POSITIVE_INFINITY;
            return ((child.totalPreviousPointsObtained)
                    / ((double) child.randomSimluatedGames))
                    + (constantC
                    * Math.sqrt(2 * (Math.log(randomSimluatedGames))
                    / (child.randomSimluatedGames)));
        }

        /**
         * isFull if all children nodes have been created
         *
         * @return true if there is a child in each entry of the children array,
         * false if not.
         */
        private boolean isFull() {
            return -1 == this.childNodeSize();
        }

        /**
         * Computes how many children have been added into the children array of
         * the current node
         *
         * @return the number of children in the children array of the current
         * node and -1 if it is full
         */
        private int childNodeSize() {
            int index = -1;
            for (int i = 0; i < children.length; i++) {
                if (children[i] == null) {
                    index = i;
                    break;
                }
            }
            return index;
        }

        /**
         * bestCardToPlay returns the Card of childnodes with the best V
         *
         * @return Card with the best V
         */
        private Card bestCardToPlay() {
            int bestIndex = bestChildNodeIndex(0);
            if (children[bestIndex] == null) return state.unplayedCards().get(0);
            return state.unplayedCards().difference(children[bestIndex].state.unplayedCards()).get(0);
        }

        /**
         * Simulates a turn from the current Node until the turn reaches the
         * end.
         * <p>
         * if it is the turn of the current Player we pick a random card from
         * the playable cards of his hand if not we pick a random card from the
         * playable cards of the rest.
         *
         * @return the final score of this simulation of the turn
         */
        private Score simulateTurn() {
            long othersCards = PackedCardSet
                    .difference(state.packedUnplayedCards(), playerHand);
            long playerCards = this.playerHand;
            TurnState tempTurn = state;
            while (!tempTurn.isTerminal()) {
                if (tempTurn.nextPlayer().ordinal() == ownId) {
                    long playable = PackedTrick
                            .playableCards(tempTurn.packedTrick(), playerCards);
                    int cardToPlay = PackedCardSet.get(playable,
                            random.nextInt(PackedCardSet.size(playable)));
                    playerCards = PackedCardSet.remove(playerCards, cardToPlay);
                    tempTurn = tempTurn.withNewCardPlayedAndTrickCollected(
                            Card.ofPacked(cardToPlay));
                } else {
                    long playable = PackedTrick
                            .playableCards(tempTurn.packedTrick(), othersCards);
                    int cardToPlay = PackedCardSet.get(playable,
                            random.nextInt(PackedCardSet.size(playable)));
                    othersCards = PackedCardSet.remove(othersCards, cardToPlay);
                    tempTurn = tempTurn.withNewCardPlayedAndTrickCollected(
                            Card.ofPacked(cardToPlay));
                }
            }
            return tempTurn.score();
        }


        /**
         * Recursively calls this function to the best child of this node for
         * the score of the simulation.
         * <p>
         * if the node isn't full then add the child, simulate it and increase
         * its points / simulated turns if the node has no possible children
         * then simulate it otherwise this means the node has a child and thus
         * go get the score of that child by calling this method
         *
         * @return randomScore of the simulated node
         */
        private Score iterate() {
            Score score;
            if (!isFull()) {
                Node child = addChild();
                score = child.simulateTurn();
                child.totalPreviousPointsObtained += score
                        .turnPoints(child.team);
                child.randomSimluatedGames++;
            } else if (children.length == 0) {
                score = simulateTurn();
            } else {
                score = children[bestChildNodeIndex(CONSTANT_C)].iterate();
            }
            randomSimluatedGames++;
            totalPreviousPointsObtained += score.turnPoints(team);
            return score;
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(" | ", "", "");
            for (Node node : children) {
                if (node != null) {
                    joiner.add(state.unplayedCards()
                            .difference(node.state.unplayedCards()).toString());
                }
            }
            return joiner.toString();
        }
    }
}
