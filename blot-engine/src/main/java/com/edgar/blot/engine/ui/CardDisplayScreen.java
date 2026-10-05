package com.edgar.blot.engine.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.edgar.blot.engine.model.Card;
import com.edgar.blot.engine.model.Player;

import java.util.List;

/**
 * Screen that displays the dealt cards for all players.
 *
 * <p>This is a simple visualisation that arranges each player's hand
 * in a separate row and renders a basic card texture with the rank
 * and suit drawn on top.</p>
 */
public class CardDisplayScreen extends ScreenAdapter {

    private final List<Player> players;
    private final SpriteBatch batch;
    private final OrthographicCamera camera;

    private BitmapFont font;
    private Texture cardTexture;

    public CardDisplayScreen(MainGame mainGame, List<Player> players) {
        this.players = players;
        this.batch = mainGame.getBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // NOTE: You need to provide this texture in your assets folder
        // (e.g. core/assets/card.png) or adjust the path/name as needed.
        cardTexture = new Texture(Gdx.files.internal("assets/cards/card.png"));

        Gdx.app.log("CardDisplayScreen", "CardDisplayScreen shown");
    }

    @Override
    public void render(float delta) {
        handleInput();

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        font.draw(batch, "Blot – Dealt Hands", 20, 580);

        float cardWidth = 64f;
        float cardHeight = 96f;
        float horizontalSpacing = 10f;
        float verticalSpacing = 40f;

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);

            float rowY = 500f - i * (cardHeight + verticalSpacing);

            // Player label
            font.draw(batch, "Player " + player.getId(), 20, rowY + cardHeight + 20f);

            List<Card> hand = player.getHand();
            for (int j = 0; j < hand.size(); j++) {
                float x = 150f + j * (cardWidth + horizontalSpacing);
                float y = rowY;

                // Draw the card background
                batch.draw(cardTexture, x, y, cardWidth, cardHeight);

                // Draw simple text representation of the card on top
                Card card = hand.get(j);
                String label = card.getRank() + " " + card.getSuit();
                font.draw(batch, label, x + 4f, y + cardHeight / 2f);
            }
        }

        batch.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
        if (cardTexture != null) {
            cardTexture.dispose();
        }
    }
}


