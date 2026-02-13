package com.edgar.blot.engine.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Trick turn order validation")
class TrickTurnOrderTest {

    private Player p(int id) {
        return new Player(id);
    }

    private Trick newTrick(Suit trump, boolean noTrump, Player... players) {
        return new Trick(trump, noTrump, List.of(players));
    }

    @Test
    @DisplayName("Player cannot start trick out of turn (must be first in trick order)")
    void playerCannotStartTrickOutOfTurn() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

        // Act / Assert
        assertThrows(RuntimeException.class,
                () -> trick.play(p2, new Card(Suit.HEARTS, Rank.ACE)));
    }

    @Test
    @DisplayName("Player cannot skip the next player in trick order")
    void playerCannotSkipNextPlayerInOrder() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.DIAMONDS, false, p1, p2, p3, p4);

        // Arrange: first player plays correctly
        trick.play(p1, new Card(Suit.HEARTS, Rank.SEVEN));

        // Act / Assert: third player should not be allowed to play before second
        assertThrows(RuntimeException.class,
                () -> trick.play(p3, new Card(Suit.HEARTS, Rank.EIGHT)));
    }

    @Test
    @DisplayName("Players can play in exact trick order without error")
    void playersCanPlayInExactOrder() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.SPADES, false, p1, p2, p3, p4);

        assertDoesNotThrow(() -> {
            trick.play(p1, new Card(Suit.HEARTS, Rank.SEVEN));
            trick.play(p2, new Card(Suit.HEARTS, Rank.EIGHT));
            trick.play(p3, new Card(Suit.HEARTS, Rank.NINE));
            trick.play(p4, new Card(Suit.HEARTS, Rank.TEN));
        });
    }

    @Test
    @DisplayName("Same player cannot play twice in the same trick")
    void samePlayerCannotPlayTwice() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.HEARTS, false, p1, p2, p3, p4);

        trick.play(p1, new Card(Suit.CLUBS, Rank.ACE));

        assertThrows(IllegalArgumentException.class,
                () -> trick.play(p1, new Card(Suit.CLUBS, Rank.KING)));
    }

    @Test
    @DisplayName("After trick is complete, no further cards can be played")
    void cannotPlayAfterTrickComplete() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.SPADES, false, p1, p2, p3, p4);

        trick.play(p1, new Card(Suit.SPADES, Rank.SEVEN));
        trick.play(p2, new Card(Suit.SPADES, Rank.EIGHT));
        trick.play(p3, new Card(Suit.SPADES, Rank.NINE));
        trick.play(p4, new Card(Suit.SPADES, Rank.TEN));

        // Guard: trick is indeed complete
        assertThrows(IllegalStateException.class,
                () -> trick.play(p1, new Card(Suit.SPADES, Rank.JACK)));
    }
}


