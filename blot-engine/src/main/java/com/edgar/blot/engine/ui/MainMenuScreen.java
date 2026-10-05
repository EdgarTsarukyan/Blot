package com.edgar.blot.engine.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import java.util.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.edgar.blot.engine.model.BlotGame;
import com.edgar.blot.engine.model.Player;

import java.util.Arrays;

public class MainMenuScreen implements Screen {

    private Stage stage;
    private Skin skin;
    private MainGame mainGame;

    public MainMenuScreen(MainGame mainGame) {
        this.mainGame = mainGame;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        skin = new Skin(Gdx.files.internal("assets/uiskin.json"));  // Загрузка skin

        // Кнопка "Play"
        TextButton playButton = new TextButton("Play", skin);
        playButton.setPosition(Gdx.graphics.getWidth() / 2 - playButton.getWidth() / 2, Gdx.graphics.getHeight() / 4);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // После нажатия переключаем на экран игры
                BlotGame blotGame = new BlotGame(createPlayers());  // Создаем объект игры
                mainGame.setScreen(new GameScreen(mainGame, blotGame)); // Переход к экрану игры
            }
        });

        stage.addActor(playButton);
        Gdx.gl.glClearColor(0, 0, 0, 1);  // Устанавливаем черный фон
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);  // Очищаем экран
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        // Этот метод вызывается, когда экран скрывается
    }

    @Override
    public void pause() {
        // Этот метод вызывается, когда приложение приостанавливается
    }

    @Override
    public void resume() {
        // Этот метод вызывается, когда приложение возобновляется
    }

    @Override
    public void dispose() {
        stage.dispose();  // Освобождаем ресурсы
    }

    private List<Player> createPlayers() {
        // Создание игроков
        return Arrays.asList(new Player(1), new Player(2), new Player(3), new Player(4));
    }
}
