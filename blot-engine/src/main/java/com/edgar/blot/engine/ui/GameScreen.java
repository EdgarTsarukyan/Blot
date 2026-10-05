package com.edgar.blot.engine.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.*;
import com.edgar.blot.engine.model.*;

import java.util.*;

public class GameScreen extends ScreenAdapter {

    private final MainGame mainGame;
    private final BlotGame game;
    private final SpriteBatch batch;

    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private float timeSinceStart = 0f;
    private int dealStage = -1;
    private boolean dealingFinished = false;

    private static final float STAGE_DURATION = 2f;
    private static final float TRICK_CLEAR_DELAY = 2f;

    private final int[] clockwiseDealOrder = new int[]{3, 1, 2, 0};

    private final Map<String, Texture> cardTextures = new HashMap<>();

    private static final float CARD_WIDTH = 130f;
    private static final float CARD_HEIGHT = 195f;
    private static final float OVERLAP = 0.6f;
    private static final float SIDE_OVERLAP = 0.55f;

    private float trickClearTimer = 0f;

    public GameScreen(MainGame mainGame, BlotGame game) {
        this.mainGame = mainGame;
        this.game = game;
        this.batch = mainGame.getBatch();
    }

    @Override
    public void show() {
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 720, camera);
        viewport.apply(true);

        game.prepareRound(1);

        loadCardTextures();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void loadCardTextures() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                String fileName = getFileName(rank, suit);
                Texture texture = new Texture(Gdx.files.internal("assets/cards/" + fileName));
                cardTextures.put(fileName, texture);
            }
        }
    }

    private String getFileName(Rank rank, Suit suit) {
        String suitStr = switch (suit) {
            case HEARTS -> "hearts";
            case SPADES -> "spades";
            case CLUBS -> "clubs";
            case DIAMONDS -> "diamonds";
        };

        String rankStr = switch (rank) {
            case ACE -> "A";
            case KING -> "K";
            case QUEEN -> "Q";
            case JACK -> "J";
            case TEN -> "10";
            case NINE -> "09";
            case EIGHT -> "08";
            case SEVEN -> "07";
        };

        return "card_" + suitStr + "_" + rankStr + ".png";
    }

    @Override
    public void render(float delta) {
        handleInput();
        handleDealing(delta);
        handleTrickClear(delta);

        if (dealingFinished) {
            if (game.getPhase() == BlotGame.GamePhase.BIDDING) {
                handleBiddingClick();
            } else {
                handleCardClick();
            }
        }

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        if (game.getPhase() == BlotGame.GamePhase.BIDDING && dealingFinished) {
            drawBiddingPanelBackground();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float centerX = worldWidth / 2f;

        if (game.getPlayers().size() >= 4) {
            drawHandTop(0, centerX, worldHeight - 200);
            drawHandBottom(1, centerX, -20);

            drawSideRotated(2, 110, worldHeight / 2f, 90);
            drawSideRotated(3, worldWidth - 110, worldHeight / 2f, -90);
        }

        drawTableCards();
        drawInfo(worldHeight);

        if (game.getPhase() == BlotGame.GamePhase.BIDDING && dealingFinished) {
            drawBiddingPanel();
        }

        batch.end();
    }

    private void handleDealing(float delta) {
        timeSinceStart += delta;

        if (dealingFinished) return;

        int currentStage = (int) (timeSinceStart / STAGE_DURATION);

        if (currentStage != dealStage && currentStage < clockwiseDealOrder.length) {
            game.dealToPlayer(clockwiseDealOrder[currentStage], 8);
            dealStage = currentStage;
        }

        if (currentStage >= clockwiseDealOrder.length) {
            dealingFinished = true;
        }
    }

    private void handleBiddingClick() {
        if (!Gdx.input.justTouched()) return;

        Vector2 touch = getTouchWorld();

        int currentPlayer = game.getCurrentPlayerIndex();
        int nextBidPoints = getNextBidPoints();

        PanelBounds panel = getBiddingPanelBounds();

        float x = panel.x;
        float y = panel.y;

        if (isInside(touch, x + 20, y + 35, 80, 30)) {
            game.makeBid(currentPlayer, Suit.HEARTS, nextBidPoints);
            return;
        }

        if (isInside(touch, x + 110, y + 35, 80, 30)) {
            game.makeBid(currentPlayer, Suit.SPADES, nextBidPoints);
            return;
        }

        if (isInside(touch, x + 200, y + 35, 80, 30)) {
            game.makeBid(currentPlayer, Suit.CLUBS, nextBidPoints);
            return;
        }

        if (isInside(touch, x + 290, y + 35, 100, 30)) {
            game.makeBid(currentPlayer, Suit.DIAMONDS, nextBidPoints);
            return;
        }

        if (isInside(touch, x + 165, y + 5, 100, 30)) {
            game.passBid(currentPlayer);
        }
    }

    private int getNextBidPoints() {
        BlotGame.Bid highestBid = game.getHighestBid();
        return highestBid == null ? 8 : highestBid.points() + 1;
    }

    private void handleCardClick() {
        if (!dealingFinished) return;
        if (game.isTrickCompleted()) return;
        if (!Gdx.input.justTouched()) return;

        int currentPlayer = game.getCurrentPlayerIndex();
        List<Card> hand = game.getVisibleSortedHand(currentPlayer);

        if (hand.isEmpty()) return;

        Card clickedCard = findClickedCard(currentPlayer, hand);

        if (clickedCard != null) {
            game.playCard(currentPlayer, clickedCard);
        }
    }

    private Card findClickedCard(int playerIndex, List<Card> hand) {
        Vector2 touch = getTouchWorld();

        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        float cx = w / 2f;

        float step = CARD_WIDTH * OVERLAP;

        if (playerIndex == 1) {
            float y = -20;
            float startX = cx - ((hand.size() - 1) * step + CARD_WIDTH) / 2f;

            for (int i = hand.size() - 1; i >= 0; i--) {
                float x = startX + i * step;

                if (isInside(touch, x, y, CARD_WIDTH, CARD_HEIGHT)) {
                    return hand.get(i);
                }
            }
        }

        if (playerIndex == 0) {
            float y = h - 200;
            float startX = cx - ((hand.size() - 1) * step + CARD_WIDTH) / 2f;

            for (int i = hand.size() - 1; i >= 0; i--) {
                float x = startX + i * step;

                if (isInside(touch, x, y, CARD_WIDTH, CARD_HEIGHT)) {
                    return hand.get(i);
                }
            }
        }

        if (playerIndex == 2) {
            float x = 110;
            float sideStep = CARD_WIDTH * SIDE_OVERLAP;
            float totalHeight = (hand.size() - 1) * sideStep + CARD_HEIGHT;
            float startY = h / 2f + totalHeight / 2f - CARD_HEIGHT;

            for (int i = hand.size() - 1; i >= 0; i--) {
                float y = startY - i * sideStep;
                CardBounds bounds = getRotatedCardBounds(x, y);

                if (isInside(touch, bounds.x, bounds.y, bounds.width, bounds.height)) {
                    return hand.get(i);
                }
            }
        }

        if (playerIndex == 3) {
            float x = w - 110;
            float sideStep = CARD_WIDTH * SIDE_OVERLAP;
            float totalHeight = (hand.size() - 1) * sideStep + CARD_HEIGHT;
            float startY = h / 2f + totalHeight / 2f - CARD_HEIGHT;

            for (int i = hand.size() - 1; i >= 0; i--) {
                float y = startY - i * sideStep;
                CardBounds bounds = getRotatedCardBounds(x, y);

                if (isInside(touch, bounds.x, bounds.y, bounds.width, bounds.height)) {
                    return hand.get(i);
                }
            }
        }

        return null;
    }

    private CardBounds getRotatedCardBounds(float x, float y) {
        float centerX = x + CARD_WIDTH / 2f;
        float centerY = y + CARD_HEIGHT / 2f;

        float rotatedWidth = CARD_HEIGHT;
        float rotatedHeight = CARD_WIDTH;

        return new CardBounds(
                centerX - rotatedWidth / 2f,
                centerY - rotatedHeight / 2f,
                rotatedWidth,
                rotatedHeight
        );
    }

    private void handleTrickClear(float delta) {
        if (!game.isTrickCompleted()) {
            trickClearTimer = 0f;
            return;
        }

        trickClearTimer += delta;

        if (trickClearTimer >= TRICK_CLEAR_DELAY) {
            game.clearCompletedTrick();
            trickClearTimer = 0f;
        }
    }

    private PanelBounds getBiddingPanelBounds() {
        int player = game.getCurrentPlayerIndex();

        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        return switch (player) {
            case 0 -> new PanelBounds(w / 2f - 230, h - 330, 460, 120);
            case 1 -> new PanelBounds(w / 2f - 230, 210, 460, 120);
            case 2 -> new PanelBounds(150, h / 2f - 60, 460, 120);
            case 3 -> new PanelBounds(w - 610, h / 2f - 60, 460, 120);
            default -> new PanelBounds(w / 2f - 230, h / 2f - 60, 460, 120);
        };
    }

    private void drawBiddingPanelBackground() {
        PanelBounds panel = getBiddingPanelBounds();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.12f, 0.14f, 0.22f, 1f);
        shapeRenderer.rect(panel.x, panel.y, panel.width, panel.height);

        shapeRenderer.setColor(0.22f, 0.30f, 0.48f, 1f);
        shapeRenderer.rect(panel.x + 6, panel.y + 6, panel.width - 12, panel.height - 12);

        shapeRenderer.setColor(0.34f, 0.43f, 0.68f, 1f);
        shapeRenderer.rect(panel.x + 12, panel.y + 12, panel.width - 24, panel.height - 24);

        shapeRenderer.end();
    }

    private void drawBiddingPanel() {
        PanelBounds panel = getBiddingPanelBounds();

        float x = panel.x;
        float y = panel.y;

        font.draw(batch, "Player " + game.getCurrentPlayerIndex() + " bidding", x + 20, y + 100);
        font.draw(batch, "Next points: " + getNextBidPoints(), x + 20, y + 78);

        drawButton("HEARTS", x + 20, y + 35);
        drawButton("SPADES", x + 110, y + 35);
        drawButton("CLUBS", x + 200, y + 35);
        drawButton("DIAMONDS", x + 290, y + 35);
        drawButton("PASS", x + 165, y + 5);
    }

    private void drawButton(String text, float x, float y) {
        font.draw(batch, "[" + text + "]", x + 8, y + 22);
    }

    private Vector2 getTouchWorld() {
        Vector2 touch = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touch);
        return touch;
    }

    private boolean isInside(Vector2 point, float x, float y, float width, float height) {
        return point.x >= x &&
                point.x <= x + width &&
                point.y >= y &&
                point.y <= y + height;
    }

    private void drawInfo(float worldHeight) {
        float x = 20;
        float y = worldHeight - 20;
        float lineHeight = 30;

        font.draw(batch, "Team 1: " + game.getTeam1TotalScore(), x, y);
        y -= lineHeight;

        font.draw(batch, "Team 2: " + game.getTeam2TotalScore(), x, y);
        y -= lineHeight;

        font.draw(batch, " WE" + game.getWeScore(), x, y);
        y -= lineHeight;

        font.draw(batch, " THEY" + game.getTheyScore(), x, y);
        y -= lineHeight;

        font.draw(batch, "Phase: " + game.getPhase(), x, y);
        y -= lineHeight;

        font.draw(batch, "Turn: " + game.getCurrentPlayerIndex(), x, y);
        y -= lineHeight;

        BlotGame.Bid highestBid = game.getHighestBid();

        if (highestBid != null) {
            font.draw(
                    batch,
                    "Highest bid: P"
                            + highestBid.playerIndex()
                            + " "
                            + highestBid.suit()
                            + " "
                            + highestBid.points(),
                    x,
                    y
            );
        } else {
            font.draw(batch, "Highest bid: none", x, y);
        }

        y -= lineHeight;

        if (game.isTrickCompleted()) {
            font.draw(
                    batch,
                    "Trick winner: " + game.getLastTrickWinnerIndex(),
                    x,
                    y
            );
        }
    }

    private void drawTableCards() {
        for (BlotGame.PlayedCard playedCard : game.getTableCards()) {
            Card card = playedCard.card();
            int playerIndex = playedCard.playerIndex();

            Texture texture = cardTextures.get(getFileName(card.getRank(), card.getSuit()));
            TableCardRenderData renderData = getTableRenderData(playerIndex);

            if (renderData.rotation == 0f) {
                batch.draw(texture, renderData.x, renderData.y, CARD_WIDTH, CARD_HEIGHT);
            } else {
                batch.draw(texture,
                        renderData.x,
                        renderData.y,
                        CARD_WIDTH / 2f,
                        CARD_HEIGHT / 2f,
                        CARD_WIDTH,
                        CARD_HEIGHT,
                        1f,
                        1f,
                        renderData.rotation,
                        0,
                        0,
                        texture.getWidth(),
                        texture.getHeight(),
                        false,
                        false);
            }
        }
    }

    private TableCardRenderData getTableRenderData(int playerIndex) {
        float cx = viewport.getWorldWidth() / 2f;
        float cy = viewport.getWorldHeight() / 2f;

        return switch (playerIndex) {
            case 0 -> new TableCardRenderData(cx - CARD_WIDTH / 2f, cy + 20, 0f);
            case 1 -> new TableCardRenderData(cx - CARD_WIDTH / 2f, cy - 120, 0f);
            case 2 -> new TableCardRenderData(cx - 180, cy - CARD_HEIGHT / 2f, 90f);
            case 3 -> new TableCardRenderData(cx + 60, cy - CARD_HEIGHT / 2f, -90f);
            default -> new TableCardRenderData(cx - CARD_WIDTH / 2f, cy - CARD_HEIGHT / 2f, 0f);
        };
    }

    private void drawHandTop(int playerIndex, float centerX, float y) {
        drawHand(playerIndex, centerX, y);
    }

    private void drawHandBottom(int playerIndex, float centerX, float y) {
        drawHand(playerIndex, centerX, y);
    }

    private void drawHand(int playerIndex, float centerX, float y) {
        List<Card> hand = game.getVisibleSortedHand(playerIndex);

        float step = CARD_WIDTH * OVERLAP;
        float totalWidth = (hand.size() - 1) * step + CARD_WIDTH;
        float startX = centerX - totalWidth / 2f;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            Texture texture = cardTextures.get(getFileName(card.getRank(), card.getSuit()));
            batch.draw(texture, startX + i * step, y, CARD_WIDTH, CARD_HEIGHT);
        }
    }

    private void drawSideRotated(int playerIndex, float x, float centerY, float rotation) {
        List<Card> hand = game.getVisibleSortedHand(playerIndex);

        float step = CARD_WIDTH * SIDE_OVERLAP;
        float totalHeight = (hand.size() - 1) * step + CARD_HEIGHT;
        float startY = centerY + totalHeight / 2f - CARD_HEIGHT;

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            Texture texture = cardTextures.get(getFileName(card.getRank(), card.getSuit()));

            float y = startY - i * step;

            batch.draw(texture,
                    x,
                    y,
                    CARD_WIDTH / 2f,
                    CARD_HEIGHT / 2f,
                    CARD_WIDTH,
                    CARD_HEIGHT,
                    1f,
                    1f,
                    rotation,
                    0,
                    0,
                    texture.getWidth(),
                    texture.getHeight(),
                    false,
                    false);
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        for (Texture texture : cardTextures.values()) {
            texture.dispose();
        }
    }

    private record PanelBounds(float x, float y, float width, float height) {
    }

    private record CardBounds(float x, float y, float width, float height) {
    }

    private static class TableCardRenderData {
        final float x;
        final float y;
        final float rotation;

        TableCardRenderData(float x, float y, float rotation) {
            this.x = x;
            this.y = y;
            this.rotation = rotation;
        }
    }
}