package com.edgar.blot.engine.ui;

import com.edgar.blot.engine.model.BlotGame;
import com.edgar.blot.engine.model.Player;

import java.util.List;

/**
 * Helper for creating a minimal {@link } instance suitable for
 * driving the libGDX demo UI.
 *
 * <p>This keeps all demo/wiring concerns out of the pure domain model
 * package and can be replaced later by real game setup / menus.</p>
 */
final class DemoGameFactory {

    private DemoGameFactory() {
        // Utility class
    }

    /**
     * Creates a new {@link BlotGame} with four players.
     *
     * <p>The UI is responsible for dealing cards / playing rounds.
     * This factory now only wires up the domain model without
     * mutating game state so that the "Play" button can start the
     * game and deal the cards explicitly.</p>
     */
    static BlotGame createDemoGame() {
        Player p1 = new Player(1);
        Player p2 = new Player(2);
        Player p3 = new Player(3);
        Player p4 = new Player(4);

        return new BlotGame(List.of(p1, p2, p3, p4));
    }
}


