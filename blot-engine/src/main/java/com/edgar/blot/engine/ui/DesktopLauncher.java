package com.edgar.blot.engine.ui;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Desktop LWJGL3 launcher for the Blot libGDX demo.
 *
 * <p>This is the class you run from your IDE to start the game window.</p>
 */
public final class DesktopLauncher {

    private DesktopLauncher() {
        // no instances
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Blot – libGDX Demo");
        config.setWindowedMode(800, 600);
        config.useVsync(true);
        config.setForegroundFPS(60);

        new Lwjgl3Application(new MainGame(), config);
    }
}


