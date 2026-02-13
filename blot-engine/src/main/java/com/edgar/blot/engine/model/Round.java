package com.edgar.blot.engine.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Domain entity representing a single round (8 tricks) of Blot for four players.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Owns the players participating in the round (fixed seating order).</li>
 *     <li>Initialises the two teams (players 0 &amp; 2 vs. players 1 &amp; 3).</li>
 *     <li>Constructs and shuffles a deck, then deals 8 cards to each player.</li>
 *     <li>Plays 8 tricks in sequence, tracking which team wins each trick.</li>
 *     <li>Provides a simple scoring view based on the teams' won cards.</li>
 * </ul>
 */
public final class Round {

    private final List<Player> players;
    private final Team team1;
    private final Team team2;
    private final Suit trump;
    private final boolean noTrump;

    /**
     * Index of the player in {@link #players} who leads the next trick.
     */
    private int startingPlayerIndex;

    /**
     * Winners of each trick in order (must contain 8 entries after a full round).
     */
    private final List<Player> trickWinners = new ArrayList<>(8);

    /**
     * Number of tricks won by each team in this round.
     */
    private int team1Tricks;
    private int team2Tricks;

    /**
     * Creates a new round.
     *
     * @param players             the four players seated in fixed order; must contain exactly four players
     * @param trump               the trump suit for the round; must not be {@code null}
     * @param noTrump             whether the round is played in no-trump mode
     * @param startingPlayerIndex index of the starting player in {@code players}; must be between 0 and 3
     */
    public Round(List<Player> players, Team team1, Team team2, Suit trump, boolean noTrump, int startingPlayerIndex) {
        Objects.requireNonNull(players, "players must not be null");
        if (players.size() != 4) {
            throw new IllegalArgumentException("Round requires exactly 4 players, got " + players.size());
        }
        this.players = List.copyOf(players);

        this.team1 = Objects.requireNonNull(team1, "team1 must not be null");
        this.team2 = Objects.requireNonNull(team2, "team2 must not be null");

        this.trump = Objects.requireNonNull(trump, "trump must not be null");
        this.noTrump = noTrump;

        if (startingPlayerIndex < 0 || startingPlayerIndex >= this.players.size()) {
            throw new IllegalArgumentException("startingPlayerIndex must be between 0 and 3");
        }
        this.startingPlayerIndex = startingPlayerIndex;

        // Shuffle deck and deal 8 cards to each player (32 cards total).
        Deck deck = new Deck();
        deck.shuffle();
        dealInitialHands(deck);
    }

    private void dealInitialHands(Deck deck) {
        List<Card> deckCards = deck.getCards();
        int index = 0;

        // Deal in simple round-robin order: 8 cards to each player.
        for (int i = 0; i < 8; i++) {
            for (Player player : players) {
                player.receiveCard(deckCards.get(index++));
            }
        }
    }

    /**
     * Plays the full round consisting of 8 tricks.
     * <p>
     * This method uses placeholder card-selection logic: each player simply plays
     * the first card in their hand. {@link Trick} enforces legality of plays.
     */
    public void playRound() {
        // Reset per-round state in case this instance is reused.
        trickWinners.clear();
        team1Tricks = 0;
        team2Tricks = 0;

        // One trick per card in each player's hand (8 cards).
        for (int i = 0; i < 8; i++) {
            List<Player> trickOrder = buildTrickOrder();
            Trick trick = new Trick(trump, noTrump, trickOrder);

            // Simple legal-play logic: for each player, try cards in hand order
            // until a card is accepted by the trick's validation rules.
            for (Player player : trickOrder) {
                boolean played = false;
                // Copy to avoid concurrent modification when we remove from hand.
                for (Card candidate : List.copyOf(player.getHand())) {
                    try {
                        trick.play(player, candidate);
                        player.playCard(candidate);
                        played = true;
                        break;
                    } catch (RuntimeException ex) {
                        // Try next candidate card.
                    }
                }
                if (!played) {
                    throw new IllegalStateException("No legal card available for player: " + player);
                }
            }

            Player winner = trick.getWinner();
            if (winner == null) {
                throw new IllegalStateException("Trick winner must not be null in a completed trick");
            }

            // Assign won cards to the winning team.
            Team winningTeam = isTeamOnePlayer(winner) ? team1 : team2;
            winningTeam.addWonCards(trick.getCards());

            // Track trick winner and per-team trick counts.
            trickWinners.add(winner);
            if (isTeamOnePlayer(winner)) {
                team1Tricks++;
            } else {
                team2Tricks++;
            }

            // Next trick starts with the winner of this trick.
            int winnerIndex = players.indexOf(winner);
            if (winnerIndex < 0) {
                throw new IllegalStateException("Winner of trick is not part of this round's players");
            }
            startingPlayerIndex = winnerIndex;
        }
    }

    private List<Player> buildTrickOrder() {
        List<Player> order = new ArrayList<>(4);
        for (int offset = 0; offset < players.size(); offset++) {
            int index = (startingPlayerIndex + offset) % players.size();
            order.add(players.get(index));
        }
        return order;
    }

    private boolean isTeamOnePlayer(Player player) {
        return team1.getPlayers().contains(player);
    }

    /**
     * Calculates the points for both teams at the end of the round.
     *
     * @return an immutable map of team to total points scored in this round
     */
    public Map<Team, Integer> calculateScore() {
        // Caput: one team wins all tricks. In that case apply special scoring
        // and ignore normal point calculation and last trick bonus.
        if (team1Tricks == 8) {
            return Map.of(
                    team1, 250,
                    team2, 0
            );
        }
        if (team2Tricks == 8) {
            return Map.of(
                    team1, 0,
                    team2, 250
            );
        }

        int team1Points = team1.calculatePoints(trump, noTrump);
        int team2Points = team2.calculatePoints(trump, noTrump);

        // Last trick bonus: team that wins the 8th trick receives +10 points.
        if (trickWinners.size() == 8) {
            Player lastTrickWinner = trickWinners.get(7);
            if (isTeamOnePlayer(lastTrickWinner)) {
                team1Points += 10;
            } else {
                team2Points += 10;
            }
        }

        return Map.of(
                team1, team1Points,
                team2, team2Points
        );
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public Suit getTrump() {
        return trump;
    }

    public boolean isNoTrump() {
        return noTrump;
    }

    public int getStartingPlayerIndex() {
        return startingPlayerIndex;
    }
}


