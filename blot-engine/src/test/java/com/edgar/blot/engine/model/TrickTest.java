package com.edgar.blot.engine.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrickTest {

    private Player p(int id, Card... handCards) {
        Player p = new Player(id);
        for (Card c : handCards) {
            p.receiveCard(c);
        }
        return p;
    }

    private Trick newTrick(Suit trump, boolean noTrump, Player... players) {
        return new Trick(trump, noTrump, List.of(players));
    }

    @Nested
    @DisplayName("Winner determination")
    class WinnerTests {

        @Test
        @DisplayName("No-trump: highest card of lead suit wins")
        void noTrumpHighestLeadSuitWins() {
            Player p1 = p(1, new Card(Suit.SPADES, Rank.SEVEN));
            Player p2 = p(2, new Card(Suit.SPADES, Rank.ACE));
            Player p3 = p(3, new Card(Suit.HEARTS, Rank.ACE));
            Player p4 = p(4, new Card(Suit.SPADES, Rank.TEN));

            Trick trick = newTrick(Suit.CLUBS, true, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.SPADES, Rank.SEVEN));
            trick.play(p2, new Card(Suit.SPADES, Rank.ACE));
            trick.play(p3, new Card(Suit.HEARTS, Rank.ACE));
            trick.play(p4, new Card(Suit.SPADES, Rank.TEN));

            assertEquals(p2, trick.getWinner());
        }

        @Test
        @DisplayName("Trump: any trump beats non-trump lead; highest trump wins")
        void trumpGameHighestTrumpWins() {
            Player p1 = p(1, new Card(Suit.HEARTS, Rank.ACE));
            Player p2 = p(2, new Card(Suit.CLUBS, Rank.SEVEN));
            Player p3 = p(3, new Card(Suit.CLUBS, Rank.JACK));
            Player p4 = p(4, new Card(Suit.CLUBS, Rank.NINE));

            Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.HEARTS, Rank.ACE));     // lead, non-trump
            trick.play(p2, new Card(Suit.CLUBS, Rank.SEVEN));    // trump
            trick.play(p3, new Card(Suit.CLUBS, Rank.JACK));     // higher trump
            trick.play(p4, new Card(Suit.CLUBS, Rank.NINE));     // lower than JACK in trump ordering

            assertEquals(p3, trick.getWinner());
        }
    }

    @Nested
    @DisplayName("Rule validation - following suit in no-trump")
    class NoTrumpValidationTests {

        @Test
        @DisplayName("Player must follow lead suit in no-trump when possible")
        void mustFollowLeadSuitInNoTrump() {
            Player leader = p(1, new Card(Suit.SPADES, Rank.ACE));
            Player follower = p(2,
                    new Card(Suit.SPADES, Rank.SEVEN),
                    new Card(Suit.HEARTS, Rank.ACE)
            );
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, true, leader, follower, p3, p4);

            trick.play(leader, new Card(Suit.SPADES, Rank.ACE));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> trick.play(follower, new Card(Suit.HEARTS, Rank.ACE))
            );
            assertTrue(ex.getMessage().contains("follow lead suit"));
        }
    }

    @Nested
    @DisplayName("Rule validation - trump game obligations")
    class TrumpValidationTests {

        @Test
        @DisplayName("If player has lead suit in trump game, they must follow it")
        void mustFollowLeadSuitWhenHoldingItInTrumpGame() {
            Player leader = p(1, new Card(Suit.HEARTS, Rank.ACE));
            Player follower = p(2,
                    new Card(Suit.HEARTS, Rank.SEVEN),
                    new Card(Suit.CLUBS, Rank.JACK) // trump
            );
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, false, leader, follower, p3, p4);

            trick.play(leader, new Card(Suit.HEARTS, Rank.ACE));

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> trick.play(follower, new Card(Suit.CLUBS, Rank.JACK))
            );
            assertTrue(ex.getMessage().contains("follow lead suit"));
        }

        @Test
        @DisplayName("If partner is not winning and player has a higher trump, they must play that higher trump")
        void mustPlayHigherTrumpWhenHoldingIt() {
            Player p1 = p(1, new Card(Suit.HEARTS, Rank.ACE));                    // lead, non-trump
            Player p2 = p(2, new Card(Suit.CLUBS, Rank.JACK));                    // strong trump
            Player p3 = p(3, new Card(Suit.CLUBS, Rank.SEVEN));                   // weak trump (current winner)
            Player p4 = p(4,
                    new Card(Suit.CLUBS, Rank.NINE),                               // trump, higher than 7 but lower than J
                    new Card(Suit.DIAMONDS, Rank.ACE)
            );

            Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.HEARTS, Rank.ACE));       // lead
            trick.play(p2, new Card(Suit.DIAMONDS, Rank.ACE));     // has trump but chooses off-suit, allowed (no lead suit, partner not winning yet)
            trick.play(p3, new Card(Suit.CLUBS, Rank.SEVEN));      // first trump, current winner

            // p4 holds a stronger trump (9) than current winning card (7),
            // so playing a non-trump card should be illegal.
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> trick.play(p4, new Card(Suit.DIAMONDS, Rank.ACE))
            );
            assertTrue(ex.getMessage().contains("higher trump"));
        }

        @Test
        @DisplayName("When holding only weaker trumps, player must play a trump (cannot discard non-trump)")
        void mustPlayAnyTrumpWhenOnlyWeakerTrumps() {
            Player p1 = p(1, new Card(Suit.CLUBS, Rank.JACK));       // strong trump lead
            Player p2 = p(2,
                    new Card(Suit.CLUBS, Rank.SEVEN),                // only weaker trump
                    new Card(Suit.HEARTS, Rank.ACE)
            );
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.CLUBS, Rank.JACK));         // lead trump, current winner

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> trick.play(p2, new Card(Suit.HEARTS, Rank.ACE))
            );
            assertTrue(ex.getMessage().contains("follow trump"));
        }

        @Test
        @DisplayName("If player has no lead suit and no trumps, any card is allowed")
        void anyCardAllowedWithoutLeadSuitOrTrump() {
            Player leader = p(1, new Card(Suit.HEARTS, Rank.ACE));
            Player follower = p(2, new Card(Suit.SPADES, Rank.SEVEN)); // no hearts, no trump
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, false, leader, follower, p3, p4);

            trick.play(leader, new Card(Suit.HEARTS, Rank.ACE));

            assertDoesNotThrow(() -> trick.play(follower, new Card(Suit.SPADES, Rank.SEVEN)));
        }
    }
}


