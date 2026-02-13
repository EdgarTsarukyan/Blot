package com.edgar.blot.engine.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void gameRequiresExactlyFourPlayers() {
        List<Player> threePlayers = List.of(new Player(0), new Player(1), new Player(2));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Game(threePlayers)
        );
        assertTrue(ex.getMessage().contains("4 players"));
    }

    @Test
    void gameAccumulatesScoresAcrossRounds() {
        Game game = new Game(List.of(
                new Player(0),
                new Player(1),
                new Player(2),
                new Player(3)
        ));

        assertFalse(game.isFinished());
        assertNull(game.getWinner());

        for (int i = 0; i < 20; i++) {
            game.playRound(Suit.HEARTS, false, 0);
        }

        assertTrue(game.getTeam1TotalScore() >= 0);
        assertTrue(game.getTeam2TotalScore() >= 0);
    }

    @Test
    void winnerIsOnlyAvailableOnceTargetScoreReached() {
        Game game = new Game(List.of(
                new Player(0),
                new Player(1),
                new Player(2),
                new Player(3)
        ));

        assertNull(game.getWinner());

        int safetyGuard = 200;
        while (!game.isFinished() && safetyGuard-- > 0) {
            game.playRound(Suit.SPADES, false, 0);
        }

        assertTrue(game.isFinished());
        assertNotNull(game.getWinner());
    }
}


