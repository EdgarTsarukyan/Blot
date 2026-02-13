package com.edgar.blot.engine.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void deckShouldContain32Cards() {
        Deck deck = new Deck();
        assertEquals(32, deck.getCards().size());
    }

    @Test
    void deckContainsExactlyOneOfEachCard() {
        Deck deck = new Deck();
        List<Card> cards = deck.getCards();

        assertEquals(32, cards.size());
        assertEquals(32, Set.copyOf(cards).size(), "Deck should not contain duplicates");

        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                Card expected = new Card(suit, rank);
                assertTrue(cards.contains(expected), "Missing card: " + expected);
            }
        }
    }

    @Test
    void getCardsReturnsUnmodifiableView() {
        Deck deck = new Deck();

        List<Card> cards = deck.getCards();
        assertThrows(UnsupportedOperationException.class, () -> cards.add(
                new Card(Suit.CLUBS, Rank.ACE)
        ));
    }

    @Test
    void shuffleReordersCardsButPreservesComposition() {
        Deck deck = new Deck();

        List<Card> original = List.copyOf(deck.getCards());
        deck.shuffle();
        List<Card> shuffled = deck.getCards();

        assertEquals(32, shuffled.size());
        assertEquals(Set.copyOf(original), Set.copyOf(shuffled), "Shuffle must preserve card multiset");
    }
}