package com.edgar.blot.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing a team of two players and the cards they have won.
 */
public final class Team {

    private final Player playerOne;
    private final Player playerTwo;
    private final List<Card> wonCards = new ArrayList<>();

    public Team(Player playerOne, Player playerTwo) {
        this.playerOne = Objects.requireNonNull(playerOne);
        this.playerTwo = Objects.requireNonNull(playerTwo);
    }

    /**
     * Returns the players in this team.
     */
    public List<Player> getPlayers() {
        return List.of(playerOne, playerTwo);
    }

    /**
     * Adds the given cards to the team's won cards.
     *
     * @param cards the cards to add, must not be {@code null}
     */
    public void addWonCards(List<Card> cards) {
        Objects.requireNonNull(cards, "cards must not be null");
        for (Card card : cards) {
            wonCards.add(Objects.requireNonNull(card, "card must not be null"));
        }
    }

    /**
     * Returns an unmodifiable view of the cards won by this team.
     */
    public List<Card> getWonCards() {
        return Collections.unmodifiableList(wonCards);
    }

    /**
     * Calculates the total points of the team's won cards.
     *
     * @param trump   the trump suit; may be {@code null} when {@code noTrump} is {@code true}
     * @param noTrump whether the game is played without a trump suit
     * @return the total number of points
     */
    public int calculatePoints(Suit trump, boolean noTrump) {
        int total = 0;
        for (Card card : wonCards) {
            total += pointsFor(card, trump, noTrump);
        }
        return total;
    }

    private int pointsFor(Card card, Suit trump, boolean noTrump) {
        Rank rank = card.getRank();

        if (noTrump) {
            // No-trump special: Ace is worth 19 instead of the normal 11.
            if (rank == Rank.ACE) {
                return 19;
            }
            return basePoints(rank);
        }

        // Trump game: apply trump bonuses first, otherwise fall back to base points.
        if (trump != null && card.getSuit() == trump) {
            if (rank == Rank.JACK) {
                return 20; // Trump J
            }
            if (rank == Rank.NINE) {
                return 14; // Trump 9
            }
        }

        return basePoints(rank);
    }

    private int basePoints(Rank rank) {
        return switch (rank) {
            case ACE -> 11;
            case TEN -> 10;
            case KING -> 4;
            case QUEEN -> 3;
            case JACK -> 2;
            case SEVEN, EIGHT, NINE -> 0;
        };
    }
}


