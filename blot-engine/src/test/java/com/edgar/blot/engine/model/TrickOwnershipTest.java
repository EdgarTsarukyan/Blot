package com.edgar.blot.engine.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trick card ownership validation")
class TrickOwnershipTest {

    private Player p(int id, Card... handCards) {
        Player player = new Player(id);
        for (Card c : handCards) {
            player.receiveCard(c);
        }
        return player;
    }

    private Trick newTrick(Suit trump, boolean noTrump, Player... players) {
        return new Trick(trump, noTrump, List.of(players));
    }

    @Test
    @DisplayName("Player cannot play a card that is not in their hand (via Player API)")
    void playerCannotPlayCardNotInHand() {
        Player player = new Player(1);
        Card notOwned = new Card(Suit.CLUBS, Rank.SEVEN);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> player.playCard(notOwned)
        );
        assertTrue(ex.getMessage().contains("does not have card"));
    }

    @Test
    @DisplayName("Playing a card removes it from hand and adds it to trick")
    void cardIsRemovedFromHandAfterPlay() {
        Card card = new Card(Suit.HEARTS, Rank.ACE);
        Player player = p(1, card);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.SPADES, false, player, p2, p3, p4);

        // Act: standard engine flow – card is removed from hand, then registered in the trick
        player.playCard(card);
        trick.play(player, card);

        // Assert: ownership updated and trick remembers the card
        assertFalse(player.getHand().contains(card), "Card should be removed from player's hand");
        assertEquals(0, player.getHand().size(), "Hand should be empty after playing single card");
        assertEquals(List.of(card), trick.getCards(), "Trick should contain the played card in order");
    }

    @Test
    @DisplayName("Same logical card must not be playable twice in a single trick")
    void sameCardCannotBePlayedTwice() {
        Card aceOfSpades = new Card(Suit.SPADES, Rank.ACE);

        // Both players are (wrongly) given the same logical card; engine should prevent this.
        Player p1 = p(1, aceOfSpades);
        Player p2 = p(2, aceOfSpades);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.HEARTS, false, p1, p2, p3, p4);

        // First play is allowed.
        p1.playCard(aceOfSpades);
        trick.play(p1, aceOfSpades);

        // Second play of the same logical card by another player should never be accepted.
        p2.playCard(aceOfSpades);

        assertThrows(RuntimeException.class,
                () -> trick.play(p2, aceOfSpades),
                "Engine should reject playing the same card twice in one trick");
    }
}


