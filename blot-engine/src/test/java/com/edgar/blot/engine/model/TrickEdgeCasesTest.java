package com.edgar.blot.engine.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Trick lead suit & trump edge cases")
class TrickEdgeCasesTest {

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

    @Nested
    @DisplayName("Lead suit edge cases")
    class LeadSuitEdgeCases {

        @Test
        @DisplayName("Lead suit is trump – highest trump wins, even if played last")
        void leadSuitIsTrumpHighestTrumpWins() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.HEARTS, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.HEARTS, Rank.SEVEN));  // lead trump
            trick.play(p2, new Card(Suit.HEARTS, Rank.EIGHT));  // higher trump
            trick.play(p3, new Card(Suit.HEARTS, Rank.NINE));   // higher trump
            trick.play(p4, new Card(Suit.HEARTS, Rank.JACK));   // highest trump

            assertEquals(p4, trick.getWinner(), "Last and strongest trump should win");
        }

        @Test
        @DisplayName("All cards different suits in no-trump – lead suit wins")
        void allCardsDifferentSuitsNoTrump() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, true, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.HEARTS, Rank.TEN));      // lead suit
            trick.play(p2, new Card(Suit.CLUBS, Rank.ACE));
            trick.play(p3, new Card(Suit.SPADES, Rank.ACE));
            trick.play(p4, new Card(Suit.DIAMONDS, Rank.ACE));

            assertEquals(p1, trick.getWinner(), "In no-trump, only lead suit cards can win");
        }

        @Test
        @DisplayName("All cards different suits in trump game – trump beats lead suit")
        void allCardsDifferentSuitsTrumpGame() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.HEARTS, Rank.TEN));          // lead suit
            trick.play(p2, new Card(Suit.CLUBS, Rank.SEVEN));         // trump
            trick.play(p3, new Card(Suit.SPADES, Rank.ACE));
            trick.play(p4, new Card(Suit.DIAMONDS, Rank.ACE));

            assertEquals(p2, trick.getWinner(), "Any trump should beat non-trump lead when present");
        }

        @Test
        @DisplayName("Only one player follows lead suit – that player wins")
        void onlyOnePlayerFollowsLeadSuit() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.SPADES, true, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.HEARTS, Rank.SEVEN));        // lead suit
            trick.play(p2, new Card(Suit.CLUBS, Rank.ACE));           // off-suit
            trick.play(p3, new Card(Suit.SPADES, Rank.ACE));          // off-suit
            trick.play(p4, new Card(Suit.HEARTS, Rank.TEN));          // only player following lead suit

            assertEquals(p4, trick.getWinner(), "Only player following lead suit should win");
        }

        @Test
        @DisplayName("Last player can still determine winner")
        void lastPlayerDeterminesWinner() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.DIAMONDS, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.HEARTS, Rank.SEVEN));
            trick.play(p2, new Card(Suit.HEARTS, Rank.EIGHT));
            trick.play(p3, new Card(Suit.DIAMONDS, Rank.SEVEN));      // low trump
            trick.play(p4, new Card(Suit.DIAMONDS, Rank.JACK));       // stronger trump played last

            assertEquals(p4, trick.getWinner(), "Stronger trump played last should still win");
        }
    }

    @Nested
    @DisplayName("Trump edge cases")
    class TrumpEdgeCases {

        @Test
        @DisplayName("Multiple trumps played – highest trump wins regardless of position")
        void multipleTrumpsHighestWins() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.SPADES, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.HEARTS, Rank.ACE));          // non-trump lead
            trick.play(p2, new Card(Suit.SPADES, Rank.SEVEN));        // trump
            trick.play(p3, new Card(Suit.SPADES, Rank.NINE));         // stronger trump
            trick.play(p4, new Card(Suit.SPADES, Rank.JACK));         // strongest trump

            assertEquals(p4, trick.getWinner(), "Highest trump should win, not earliest");
        }

        @Test
        @DisplayName("Player with trump but no lead suit should not be allowed to discard when stronger trump exists")
        void playerHasTrumpButTriesDiscard() {
            Player p1 = p(1, new Card(Suit.HEARTS, Rank.ACE));                     // lead, non-trump
            Player p2 = p(2, new Card(Suit.SPADES, Rank.SEVEN));                   // weak trump
            Player p3 = p(3, new Card(Suit.SPADES, Rank.JACK));                    // strong trump (current winner)
            Player p4 = p(4,
                    new Card(Suit.SPADES, Rank.NINE),                              // trump higher than 7, lower than J
                    new Card(Suit.DIAMONDS, Rank.ACE));                            // discard candidate

            Trick trick = newTrick(Suit.SPADES, false, p1, p2, p3, p4);

            // p1 leads, p2 discards off-suit (no lead suit, has trump but no stronger one yet – allowed by current rules)
            trick.play(p1, new Card(Suit.HEARTS, Rank.ACE));
            trick.play(p2, new Card(Suit.DIAMONDS, Rank.SEVEN));

            // p3 plays first trump, becomes current winner
            trick.play(p3, new Card(Suit.SPADES, Rank.JACK));

            // p4 now has a trump that can overtake 7 but not J.
            // Discarding off-suit should be rejected by stricter rules.
            assertThrows(RuntimeException.class,
                    () -> trick.play(p4, new Card(Suit.DIAMONDS, Rank.ACE)),
                    "Player with trump should not be allowed to discard in this configuration");
        }

        @Test
        @DisplayName("Player with only weaker trump must still play a trump when lead is trump")
        void playerWithOnlyWeakerTrumpMustPlayIt() {
            Player p1 = p(1, new Card(Suit.CLUBS, Rank.JACK));         // strong trump lead
            Player p2 = p(2,
                    new Card(Suit.CLUBS, Rank.SEVEN),                  // only weaker trump
                    new Card(Suit.DIAMONDS, Rank.ACE));                // off-suit
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.CLUBS, Rank.JACK));           // lead trump

            assertThrows(IllegalArgumentException.class,
                    () -> trick.play(p2, new Card(Suit.DIAMONDS, Rank.ACE)),
                    "Player with only weaker trump must follow trump, not discard");
        }

        @Test
        @DisplayName("Player with stronger trump must overtake when lead is trump")
        void playerWithStrongerTrumpMustOvertake() {
            Player p1 = p(1, new Card(Suit.CLUBS, Rank.NINE));         // lead trump
            Player p2 = p(2,
                    new Card(Suit.CLUBS, Rank.JACK),                   // stronger trump
                    new Card(Suit.DIAMONDS, Rank.ACE));                // discard candidate
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, false, p1, p2, p3, p4);

            trick.play(p1, new Card(Suit.CLUBS, Rank.NINE));           // lead trump, current winner

            // p2 holds a stronger trump than the current winning card and must overtake,
            // not discard a non-trump card.
            assertThrows(IllegalArgumentException.class,
                    () -> trick.play(p2, new Card(Suit.DIAMONDS, Rank.ACE)),
                    "Player holding stronger trump than current winner must play the trump when lead is trump");
        }

        @Test
        @DisplayName("No lead suit and no trump – free play allowed")
        void noLeadSuitAndNoTrumpFreePlayAllowed() {
            Player leader = p(1, new Card(Suit.HEARTS, Rank.ACE));
            Player follower = p(2, new Card(Suit.SPADES, Rank.SEVEN));              // no hearts, no trump
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.CLUBS, false, leader, follower, p3, p4);

            trick.play(leader, new Card(Suit.HEARTS, Rank.ACE));

            assertDoesNotThrow(() -> trick.play(follower, new Card(Suit.SPADES, Rank.SEVEN)),
                    "Follower with no lead suit and no trump must be allowed to discard any card");
        }
    }

    @Nested
    @DisplayName("Negative argument scenarios")
    class NegativeScenarios {

        @Test
        @DisplayName("Null card is rejected")
        void nullCardRejected() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.HEARTS, false, p1, p2, p3, p4);

            assertThrows(NullPointerException.class, () -> trick.play(p1, null));
        }

        @Test
        @DisplayName("Null player is rejected")
        void nullPlayerRejected() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);
            Player p4 = p(4);

            Trick trick = newTrick(Suit.SPADES, false, p1, p2, p3, p4);

            assertThrows(NullPointerException.class, () -> trick.play(null, new Card(Suit.SPADES, Rank.ACE)));
        }

        @Test
        @DisplayName("Duplicate player in trick order should be rejected")
        void duplicatePlayerInTrickOrderRejected() {
            Player p1 = p(1);
            Player p2 = p(2);
            Player p3 = p(3);

            // p1 appears twice; strict validation should reject this configuration.
            assertThrows(IllegalArgumentException.class,
                    () -> newTrick(Suit.CLUBS, false, p1, p2, p3, p1));
        }
    }
}


