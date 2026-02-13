package com.edgar.blot.engine.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Domain entity representing a full Blot game made up of multiple rounds.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Owns the four players participating in the game (fixed seating order).</li>
 *     <li>Tracks cumulative team scores across rounds.</li>
 *     <li>Plays rounds until one team reaches 301 points.</li>
 * </ul>
 * <p>
 * This class intentionally keeps the logic simple and does not implement bidding yet.
 */
public final class Game {

    private static final int TARGET_SCORE = 301;

    private int team1TotalScore;
    private int team2TotalScore;
    private final List<Player> players;
    private final Team team1;
    private final Team team2;

    /**
     * Creates a new game with the given players.
     *
     * @param players the four players seated in fixed order; must contain exactly four players
     */
    public Game(List<Player> players) {
        Objects.requireNonNull(players, "players must not be null");
        if (players.size() != 4) {
            throw new IllegalArgumentException("Game requires exactly 4 players, got " + players.size());
        }
        this.players = List.copyOf(players);

        // Teams are fixed by seating: (0, 1) vs. (2, 3).
        this.team1 = new Team(this.players.get(0), this.players.get(1));
        this.team2 = new Team(this.players.get(2), this.players.get(3));
    }

    /**
     * Plays a single round with the given trump configuration and starting player.
     * <p>
     * This method:
     * <ul>
     *     <li>Creates a new {@link Round}.</li>
     *     <li>Invokes {@link Round#playRound()} to play all tricks.</li>
     *     <li>Retrieves the round score via {@link Round#calculateScore()}.</li>
     *     <li>Adds the points to the cumulative team scores.</li>
     * </ul>
     *
     * @param trump               the trump suit for this round; must not be {@code null}
     * @param noTrump             whether this round is played in no-trump mode
     * @param startingPlayerIndex index of the starting player in {@code players}; must be between 0 and 3
     */
    public void playRound(Suit trump, boolean noTrump, int startingPlayerIndex) {
        Objects.requireNonNull(trump, "trump must not be null");

        Round round = new Round(players, team1, team2, trump, noTrump, startingPlayerIndex);
        round.playRound();

        Map<Team, Integer> roundScore = round.calculateScore();

        Integer team1Points = roundScore.getOrDefault(team1, 0);
        Integer team2Points = roundScore.getOrDefault(team2, 0);

        team1TotalScore += team1Points;
        team2TotalScore += team2Points;
    }

    /**
     * Returns whether the game is finished.
     *
     * @return {@code true} if either team has reached or exceeded 301 points
     */
    public boolean isFinished() {
        return team1TotalScore >= TARGET_SCORE || team2TotalScore >= TARGET_SCORE;
    }

    /**
     * Returns the winning team, or {@code null} if the game is not finished.
     *
     * @return the winning {@link Team}, or {@code null} if {@link #isFinished()} is {@code false}
     */
    public Team getWinner() {
        if (!isFinished()) {
            return null;
        }

        if (team1TotalScore >= TARGET_SCORE && team1TotalScore >= team2TotalScore) {
            return team1;
        }
        if (team2TotalScore >= TARGET_SCORE && team2TotalScore >= team1TotalScore) {
            return team2;
        }

        // In the unlikely event of an exact tie at or above the target, no winner is declared yet.
        return null;
    }

    public int getTeam1TotalScore() {
        return team1TotalScore;
    }

    public int getTeam2TotalScore() {
        return team2TotalScore;
    }

    public List<Player> getPlayers() {
        return players;
    }
}


