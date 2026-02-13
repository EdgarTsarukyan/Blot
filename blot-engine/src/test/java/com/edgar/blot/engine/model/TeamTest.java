package com.edgar.blot.engine.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeamTest {

    private Team teamWithCards(Card... cards) {
        Player p1 = new Player(1);
        Player p2 = new Player(2);
        Team team = new Team(p1, p2);
        team.addWonCards(List.of(cards));
        return team;
    }

    @Test
    void basePointValuesInTrumpGame() {
        Team team = teamWithCards(
                new Card(Suit.CLUBS, Rank.ACE),   // 11
                new Card(Suit.CLUBS, Rank.TEN),   // 10
                new Card(Suit.CLUBS, Rank.KING),  // 4
                new Card(Suit.CLUBS, Rank.QUEEN), // 3
                new Card(Suit.CLUBS, Rank.JACK),  // trump jack will be overridden
                new Card(Suit.CLUBS, Rank.NINE),  // trump nine will be overridden
                new Card(Suit.CLUBS, Rank.SEVEN), // 0
                new Card(Suit.CLUBS, Rank.EIGHT)  // 0
        );

        int pointsNoTrumpFlagFalseNonTrumpSuit = team.calculatePoints(Suit.HEARTS, false);
        // In this configuration, CLUBS is non-trump; all cards use base points.
        int expectedBaseTotal =
                11 + // ACE
                10 + // TEN
                4 +  // KING
                3 +  // QUEEN
                2 +  // JACK
                0 +  // NINE
                0 +  // SEVEN
                0;   // EIGHT

        assertEquals(expectedBaseTotal, pointsNoTrumpFlagFalseNonTrumpSuit);
    }

    @Test
    void trumpBonusesAppliedForJackAndNine() {
        Team team = teamWithCards(
                new Card(Suit.HEARTS, Rank.JACK),
                new Card(Suit.HEARTS, Rank.NINE),
                new Card(Suit.HEARTS, Rank.ACE)
        );

        int pointsTrumpHearts = team.calculatePoints(Suit.HEARTS, false);

        int expected =
                20 + // trump JACK
                14 + // trump NINE
                11;  // ACE with normal base points

        assertEquals(expected, pointsTrumpHearts);
    }

    @Test
    void noTrumpAceIsWorthNineteen() {
        Team team = teamWithCards(
                new Card(Suit.SPADES, Rank.ACE),
                new Card(Suit.SPADES, Rank.TEN)
        );

        int pointsNoTrump = team.calculatePoints(Suit.CLUBS, true);

        int expected =
                19 + // ACE in no-trump
                10;  // TEN uses base points

        assertEquals(expected, pointsNoTrump);
    }
}


