package com.edgar.blot.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable identity, mutable hand domain entity representing a player.
 */
public final class Player {

    private final int id;
    private final List<Card> hand = new ArrayList<>();

    public Player(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Returns an unmodifiable view of the cards currently in the player's hand.
     */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    /**
     * Adds the given card to the player's hand.
     *
     * @param card the card to add, must not be {@code null}
     */
    public void receiveCard(Card card) {
        hand.add(Objects.requireNonNull(card));
    }

    /**
     * Removes the given card from the player's hand.
     *
     * @param card the card to remove, must not be {@code null}
     * @throws IllegalArgumentException if the player does not have the given card
     */
    public void playCard(Card card) {
        Card nonNullCard = Objects.requireNonNull(card);
        if (!hand.remove(nonNullCard)) {
            throw new IllegalArgumentException("Player does not have card: " + nonNullCard);
        }
    }

    /**
     * Checks whether the player has at least one card of the given suit.
     *
     * @param suit the suit to check, must not be {@code null}
     * @return {@code true} if the hand contains a card of the given suit
     */
    public boolean hasSuit(Suit suit) {
        Suit nonNullSuit = Objects.requireNonNull(suit);
        for (Card card : hand) {
            if (card.getSuit() == nonNullSuit) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the player has at least one trump card of the given trump suit.
     *
     * @param trumpSuit the trump suit, must not be {@code null}
     * @return {@code true} if the hand contains a card with the trump suit
     */
    public boolean hasTrump(Suit trumpSuit) {
        return hasSuit(trumpSuit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;
        return id == player.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return "Player{" +
                "id=" + id +
                ", handSize=" + hand.size() +
                '}';
    }
}