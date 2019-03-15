package fi.tuni.tiko2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class TutorialScreen implements Screen {
    RaccoonRoll game;

    public TutorialScreen(RaccoonRoll game) {
        this.game = game;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "TutorialScreen");
        }
    }
}
