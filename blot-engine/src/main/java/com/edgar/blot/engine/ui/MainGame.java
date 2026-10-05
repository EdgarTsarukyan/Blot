package com.edgar.blot.engine.ui;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Root libGDX {@link Game} implementation for the Blot desktop client.
 *
 * <p>This class is responsible for bootstrapping the libGDX application
 * and creating the initial {@link GameScreen} that will in turn talk to
 * the domain model in {@code com.edgar.blot.engine.model}.</p>
 */

public class MainGame extends Game {

    private SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Стартуем С МЕНЮ (только кнопка Play)
        setScreen(new MainMenuScreen(this));
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (batch != null) {
            batch.dispose();
        }
    }
}


