package com.edgar.blot.engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {

    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    /**
     * Deals (removes and returns) the top card from the deck.
     *
     * @return the dealt {@link Card}
     * @throws IllegalStateException if the deck is empty
     */
    public Card dealCard() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Cannot deal from an empty deck");
        }
        // Remove from the end to keep this O(1)
        return cards.remove(cards.size() - 1);
    }
}
