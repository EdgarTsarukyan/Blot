package com.edgar.blot.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;


/**
 * Represents a single trick (round) of play consisting of up to four cards.
 * <p>
 * The order of play is preserved and used when determining the winner.
 */
public final class Trick {

    private final Map<Player, Card> playedCards = new LinkedHashMap<>();
    private final Suit trump;
    private final boolean noTrump;
    /**
     * Fixed seating / play order for the trick.
     * <p>
     * This list always contains exactly four players and is used for partner
     * detection. It is independent of the order in which cards are played in
     * {@link #playedCards}.
     */
    private final List<Player> trickOrder;

    /**
     * Creates a new trick.
     *
     * @param trump      the trump suit for the game, must not be {@code null}
     * @param noTrump    whether the game is played without trump (NT)
     * @param trickOrder the fixed order of players for this trick; must contain
     *                   exactly four distinct players and must not be {@code null}
     */
    public Trick(Suit trump, boolean noTrump, List<Player> trickOrder) {
        this.trump = Objects.requireNonNull(trump);
        this.noTrump = noTrump;
        Objects.requireNonNull(trickOrder, "trickOrder must not be null");
        if (trickOrder.size() != 4) {
            throw new IllegalArgumentException("trickOrder must contain exactly 4 players");
        }
        // Ensure all players in the trick order are distinct to avoid broken
        // partner detection and turn-order logic.
        if (trickOrder.stream().distinct().count() != 4) {
            throw new IllegalArgumentException("trickOrder must contain four distinct players");
        }
        this.trickOrder = List.copyOf(trickOrder);
    }

    /**
     * Plays a card for the given player in this trick.
     *
     * @param player the player who plays the card, must not be {@code null}
     * @param card   the card being played, must not be {@code null}
     * @throws IllegalStateException    if more than four cards are played
     * @throws IllegalArgumentException if the player has already played a card
     */
    public void play(Player player, Card card) {
        Objects.requireNonNull(player, "player must not be null");
        Objects.requireNonNull(card, "card must not be null");

        if (isComplete()) {
            throw new IllegalStateException("Cannot play a card into a completed trick");
        }

        // Same player may not play twice in the same trick.
        if (playedCards.containsKey(player)) {
            throw new IllegalArgumentException("Player has already played a card in this trick: " + player);
        }

        // Prevent the same Card instance from being played twice in a single
        // trick, even by different players. This works on object identity,
        // not logical equality, so distinct instances of the same rank/suit
        // are still allowed where tests expect this.
        for (Card played : playedCards.values()) {
            if (played == card) {
                throw new RuntimeException("Card has already been played in this trick: " + card);
            }
        }

        // Enforce strict turn order based on the fixed trickOrder. The first
        // card must be played by trickOrder[0], the second by trickOrder[1], etc.
        int expectedIndex = playedCards.size();
        Player expectedPlayer = trickOrder.get(expectedIndex);
        if (!expectedPlayer.equals(player)) {
            throw new RuntimeException("Player out of turn: expected " + expectedPlayer + " but was " + player);
        }

        if (playedCards.size() >= 4) {
            throw new IllegalStateException("Cannot play more than four cards in a trick");
        }
        if (!playedCards.isEmpty()) {
            validatePlay(player, card);
        }

        playedCards.put(player, card);
    }

    /**
     * Returns whether the trick is complete (all four cards were played).
     */
    public boolean isComplete() {
        return playedCards.size() == 4;
    }

    /**
     * Returns the winner of the trick according to Blot rules.
     *
     * @return the player who won the trick
     * @throws IllegalStateException if the trick is not complete
     */
    public Player getWinner() {
        if (!isComplete()) {
            throw new IllegalStateException("Cannot determine winner of an incomplete trick");
        }

        Entry<Player, Card> currentWinningEntry = determineCurrentWinner();
        return currentWinningEntry != null ? currentWinningEntry.getKey() : null;
    }

    /**
     * Returns the cards played in this trick in play order.
     */
    public List<Card> getCards() {
        return Collections.unmodifiableList(new ArrayList<>(playedCards.values()));
    }

    /**
     * Returns the currently winning card according to Blot rules based on the
     * cards that have already been played in this trick. May return {@code null}
     * when no cards have been played.
     */
    private Card getCurrentWinningCard() {
        Entry<Player, Card> entry = determineCurrentWinner();
        return entry != null ? entry.getValue() : null;
    }

    /**
     * Determines the currently winning player/card entry according to Blot rules.
     * This uses the exact same logic as {@link #getWinner()} but does not require
     * the trick to be complete and operates on the cards played so far.
     */
    private Entry<Player, Card> determineCurrentWinner() {
        if (playedCards.isEmpty()) {
            return null;
        }

        // Determine lead suit from the first card played.
        Card firstCard = playedCards.values().iterator().next();
        Suit leadSuit = firstCard.getSuit();

        Entry<Player, Card> currentWinner = null;
        Card winningCard = null;

        if (noTrump) {
            // No-trump game: only cards of the lead suit can win, ranked by standard order.
            for (Entry<Player, Card> entry : playedCards.entrySet()) {
                Card card = entry.getValue();
                if (card.getSuit() != leadSuit) {
                    continue;
                }
                if (winningCard == null ||
                        compareNonTrump(card.getRank(), winningCard.getRank()) > 0) {
                    winningCard = card;
                    currentWinner = entry;
                }
            }
        } else {
            // Trump game:
            // 1. If any trumps are played, highest trump wins.
            // 2. Otherwise, highest card of the lead suit wins (non-trump ranking).
            boolean anyTrumpPlayed = false;

            for (Card card : playedCards.values()) {
                if (card.getSuit() == trump) {
                    anyTrumpPlayed = true;
                    break;
                }
            }

            if (anyTrumpPlayed) {
                for (Entry<Player, Card> entry : playedCards.entrySet()) {
                    Card card = entry.getValue();
                    if (card.getSuit() != trump) {
                        continue;
                    }
                    if (winningCard == null ||
                            compareTrump(card.getRank(), winningCard.getRank()) > 0) {
                        winningCard = card;
                        currentWinner = entry;
                    }
                }
            } else {
                for (Entry<Player, Card> entry : playedCards.entrySet()) {
                    Card card = entry.getValue();
                    if (card.getSuit() != leadSuit) {
                        continue;
                    }
                    if (winningCard == null ||
                            compareNonTrump(card.getRank(), winningCard.getRank()) > 0) {
                        winningCard = card;
                        currentWinner = entry;
                    }
                }
            }
        }

        return currentWinner;
    }

    private int compareTrump(Rank r1, Rank r2) {
        return Integer.compare(trumpRankValue(r1), trumpRankValue(r2));
    }

    private int compareNonTrump(Rank r1, Rank r2) {
        return Integer.compare(nonTrumpRankValue(r1), nonTrumpRankValue(r2));
    }

    /**
     * Checks whether the given player currently holds at least one card of the
     * given suit.
     */
    private boolean hasSuit(Player player, Suit suit) {
        Objects.requireNonNull(player, "player must not be null");
        Objects.requireNonNull(suit, "suit must not be null");
        // Delegate to Player API to avoid duplicating hand traversal logic.
        return player.hasSuit(suit);
    }

    /**
     * Checks whether the given player has at least one trump card that is
     * strictly stronger than the current winning card.
     *
     * <p>If there is currently no winning card, or if the current winning card
     * is not a trump, no \"must overtake\" obligation applies.</p>
     */
    private boolean hasStrongerTrump(Player player, Card currentWinning) {
        Objects.requireNonNull(player, "player must not be null");

        // Only meaningful when the current winning card is itself a trump.
        if (currentWinning == null || currentWinning.getSuit() != trump) {
            return false;
        }

        for (Card card : player.getHand()) {
            if (card.getSuit() != trump) {
                continue;
            }
            if (compareTrump(card.getRank(), currentWinning.getRank()) > 0) {
                return true;
            }
        }

        return false;
    }

    private void validatePlay(Player player, Card card) {
        // First card of the trick is always allowed (guarded by caller).
        Card leadCard = playedCards.values().iterator().next();
        Suit leadSuit = leadCard.getSuit();

        if (noTrump) {
            validateNoTrumpPlay(player, card, leadSuit);
        } else {
            validateTrumpPlay(player, card, leadCard);
        }
    }

    private void validateNoTrumpPlay(Player player, Card card, Suit leadSuit) {
        if (hasSuit(player, leadSuit) && card.getSuit() != leadSuit) {
            throw new IllegalArgumentException("Player must follow lead suit " + leadSuit + " when possible");
        }
    }

    private void validateTrumpPlay(Player player, Card card, Card leadCard) {
        Suit leadSuit = leadCard.getSuit();
        Card currentWinning = getCurrentWinningCard();
        Entry<Player, Card> currentWinningEntry = determineCurrentWinner();
        Player currentWinningPlayer = currentWinningEntry != null ? currentWinningEntry.getKey() : null;

        boolean leadIsTrump = leadSuit == trump;

        if (!leadIsTrump) {
            // CASE A — lead card is non-trump
            if (hasSuit(player, leadSuit)) {
                // Player must follow lead suit if they have it.
                if (card.getSuit() != leadSuit) {
                    throw new IllegalArgumentException("Player must follow lead suit " + leadSuit + " when holding that suit");
                }
                return;
            }

            // Player does not have lead suit.
            boolean playerHasTrump = hasSuit(player, trump);
            if (!playerHasTrump) {
                // No lead suit and no trump: any card allowed.
                return;
            }

            // Player has trump.
            boolean partnerWinning = isPartner(player, currentWinningPlayer);
            if (partnerWinning) {
                // If partner is currently winning → any card allowed.
                return;
            }

            // Partner is not winning. If player has a stronger trump than current winning card,
            // they must play such trump.
            if (hasStrongerTrump(player, currentWinning)) {
                if (card.getSuit() != trump ||
                        currentWinning != null && compareTrump(card.getRank(), currentWinning.getRank()) <= 0) {
                    throw new IllegalArgumentException("Player must play a higher trump when holding one");
                }
                return;
            }

            // Otherwise (no stronger trump but at least one trump in hand):
            // if any trump has already been played in this trick and the
            // current winner is an opponent, player must still play a trump
            // and is not allowed to discard a non-trump.
            boolean anyTrumpPlayed = playedCards.values().stream().anyMatch(c -> c.getSuit() == trump);
            if (anyTrumpPlayed && card.getSuit() != trump) {
                throw new RuntimeException("Player with trump must not discard when a trump has already been played");
            }
            // If no trump has been played yet, any card is still allowed.
        } else {
            // CASE B — lead card is trump
            boolean playerHasTrump = hasSuit(player, trump);
            if (!playerHasTrump) {
                // If player has no trump → may play any card.
                return;
            }

            // Player has at least one trump.
            boolean hasStrongerTrump = hasStrongerTrump(player, currentWinning);
            if (hasStrongerTrump) {
                // If player has stronger trump than current winning card → must play such.
                if (card.getSuit() != trump ||
                        currentWinning != null && compareTrump(card.getRank(), currentWinning.getRank()) <= 0) {
                    throw new IllegalArgumentException("Player must play a higher trump when holding one");
                }
            } else {
                // Else → must play any trump.
                if (card.getSuit() != trump) {
                    throw new IllegalArgumentException("Player must follow trump when holding only weaker trumps");
                }
            }
        }
    }

    /**
     * Determines whether two players are partners based on their seating
     * positions within the trick. With four players taking turns, players in
     * positions (0, 2) are partners and players in positions (1, 3) are partners.
     */
    private boolean isPartner(Player a, Player b) {
        if (a == null || b == null) {
            return false;
        }

        int indexA = indexOfPlayerInOrder(a);
        int indexB = indexOfPlayerInOrder(b);

        if (indexA == -1 || indexB == -1) {
            return false;
        }

        return (indexA % 2) == (indexB % 2);
    }

    /**
     * Finds the index of the given player in the fixed {@link #trickOrder}.
     */
    private int indexOfPlayerInOrder(Player player) {
        for (int i = 0; i < trickOrder.size(); i++) {
            if (trickOrder.get(i).equals(player)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Trump strength ordering (high &rarr; low):
     * JACK &gt; NINE &gt; ACE &gt; TEN &gt; KING &gt; QUEEN &gt; EIGHT &gt; SEVEN.
     *
     * <p>The returned values are relative strength values used only for
     * comparison and have no relation to scoring.</p>
     */
    private int trumpRankValue(Rank rank) {
        return switch (rank) {
            case JACK -> 7;
            case NINE -> 6;
            case ACE -> 5;
            case TEN -> 4;
            case KING -> 3;
            case QUEEN -> 2;
            case EIGHT -> 1;
            case SEVEN -> 0;
        };
    }

    /**
     * Non-trump / standard strength ordering (high &rarr; low):
     * ACE &gt; TEN &gt; KING &gt; QUEEN &gt; JACK &gt; NINE &gt; EIGHT &gt; SEVEN.
     *
     * <p>The returned values are relative strength values used only for
     * comparison and have no relation to scoring.</p>
     */
    private int nonTrumpRankValue(Rank rank) {
        return switch (rank) {
            case ACE -> 7;
            case TEN -> 6;
            case KING -> 5;
            case QUEEN -> 4;
            case JACK -> 3;
            case NINE -> 2;
            case EIGHT -> 1;
            case SEVEN -> 0;
        };
    }
}


