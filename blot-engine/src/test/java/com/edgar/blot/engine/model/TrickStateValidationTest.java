package com.edgar.blot.engine.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trick state machine validation")
class TrickStateValidationTest {

    private Player p(int id) {
        return new Player(id);
    }

    private Trick newTrick(Suit trump, boolean noTrump, Player... players) {
        return new Trick(trump, noTrump, List.of(players));
    }

    @Test
    @DisplayName("getWinner() before four cards are played throws")
    void getWinnerBeforeFourCardsThrows() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

        trick.play(p1, new Card(Suit.HEARTS, Rank.SEVEN));
        trick.play(p2, new Card(Suit.HEARTS, Rank.EIGHT));
        trick.play(p3, new Card(Suit.HEARTS, Rank.NINE));

        assertThrows(IllegalStateException.class, trick::getWinner);
    }

    @Test
    @DisplayName("getWinner() is stable after trick completion")
    void getWinnerAfterCompleteIsStable() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.SPADES, false, p1, p2, p3, p4);

        trick.play(p1, new Card(Suit.SPADES, Rank.SEVEN));
        trick.play(p2, new Card(Suit.SPADES, Rank.EIGHT));
        trick.play(p3, new Card(Suit.SPADES, Rank.NINE));
        trick.play(p4, new Card(Suit.SPADES, Rank.TEN));

        assertTrue(trick.isComplete(), "Trick should report completion after four cards");

        // Winner must be deterministic and stable across calls.
        Player firstWinner = trick.getWinner();
        for (int i = 0; i < 10; i++) {
            assertSame(firstWinner, trick.getWinner(), "Winner should not change across calls");
        }
    }

    @Test
    @DisplayName("No plays allowed once winner is determined / trick is complete")
    void cannotPlayAfterWinnerDetermined() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.HEARTS, false, p1, p2, p3, p4);

        trick.play(p1, new Card(Suit.HEARTS, Rank.SEVEN));
        trick.play(p2, new Card(Suit.HEARTS, Rank.EIGHT));
        trick.play(p3, new Card(Suit.HEARTS, Rank.NINE));
        trick.play(p4, new Card(Suit.HEARTS, Rank.TEN));

        // Accessing the winner should not reopen the trick.
        assertNotNull(trick.getWinner());

        assertThrows(IllegalStateException.class,
                () -> trick.play(p1, new Card(Suit.HEARTS, Rank.JACK)));
    }

    @Test
    @DisplayName("Trick completion flag reflects number of cards played")
    void trickReportsCompletionCorrectly() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.DIAMONDS, false, p1, p2, p3, p4);

        assertFalse(trick.isComplete(), "New trick should be incomplete");

        trick.play(p1, new Card(Suit.DIAMONDS, Rank.SEVEN));
        assertFalse(trick.isComplete(), "One card played is not complete");

        trick.play(p2, new Card(Suit.DIAMONDS, Rank.EIGHT));
        assertFalse(trick.isComplete(), "Two cards played is not complete");

        trick.play(p3, new Card(Suit.DIAMONDS, Rank.NINE));
        assertFalse(trick.isComplete(), "Three cards played is not complete");

        trick.play(p4, new Card(Suit.DIAMONDS, Rank.TEN));
        assertTrue(trick.isComplete(), "Four cards played should mark trick as complete");
    }

    @Test
    @DisplayName("Cannot play more than four cards in a single trick")
    void cannotPlayMoreThanFourCards() {
        Player p1 = p(1);
        Player p2 = p(2);
        Player p3 = p(3);
        Player p4 = p(4);

        Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

        trick.play(p1, new Card(Suit.CLUBS, Rank.SEVEN));
        trick.play(p2, new Card(Suit.CLUBS, Rank.EIGHT));
        trick.play(p3, new Card(Suit.CLUBS, Rank.NINE));
        trick.play(p4, new Card(Suit.CLUBS, Rank.TEN));

        assertTrue(trick.isComplete());

        assertThrows(IllegalStateException.class,
                () -> trick.play(p1, new Card(Suit.CLUBS, Rank.JACK)));
    }
}


