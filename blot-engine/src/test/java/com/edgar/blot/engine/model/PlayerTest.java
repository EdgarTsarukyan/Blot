package com.edgar.blot.engine.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void receiveAndPlayCardMutatesHand() {
        Player player = new Player(1);
        Card card = new Card(Suit.HEARTS, Rank.ACE);

        player.receiveCard(card);
        assertEquals(1, player.getHand().size());
        assertTrue(player.getHand().contains(card));

        player.playCard(card);
        assertEquals(0, player.getHand().size());
        assertFalse(player.getHand().contains(card));
    }

    @Test
    void playingCardNotInHandThrows() {
        Player player = new Player(1);
        Card card = new Card(Suit.CLUBS, Rank.SEVEN);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> player.playCard(card)
        );
        assertTrue(ex.getMessage().contains("does not have card"));
    }

    @Test
    void hasSuitAndHasTrumpReflectCurrentHand() {
        Player player = new Player(1);
        Card spadeAce = new Card(Suit.SPADES, Rank.ACE);

        assertFalse(player.hasSuit(Suit.SPADES));
        assertFalse(player.hasTrump(Suit.SPADES));

        player.receiveCard(spadeAce);

        assertTrue(player.hasSuit(Suit.SPADES));
        assertTrue(player.hasTrump(Suit.SPADES));
        assertFalse(player.hasSuit(Suit.HEARTS));
    }

    @Test
    void handExposureIsUnmodifiable() {
        Player player = new Player(1);
        player.receiveCard(new Card(Suit.DIAMONDS, Rank.TEN));

        List<Card> exposedHand = player.getHand();
        assertThrows(UnsupportedOperationException.class, () -> exposedHand.add(
                new Card(Suit.CLUBS, Rank.ACE)
        ));
    }
}


