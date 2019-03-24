package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Screen for displaying a map with all levels
 *
 * @author Laura Kanerva
 */

public class MapScreen extends ApplicationAdapter implements Screen {

    RaccoonRoll game;
    SpriteBatch batch;
    OrthographicCamera worldCamera;
    OrthographicCamera textCamera;
    Stage stage;
    Texture background;
    float bgHeight;
    float bgWidth;

    private float bgX;

    public MapScreen(RaccoonRoll game) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        background = new Texture("graphics/mappi_large.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        bgHeight = game.scaleFromFHD(background.getHeight());
        bgWidth = game.scaleFromFHD(background.getWidth());

        Gdx.input.setInputProcessor(new GestureDetector(new MapScroller()));
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(textCamera.combined);
        batch.begin();
        batch.draw(background, bgX, 0, bgWidth, bgHeight);
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void hide() {

    }

    class MapScroller extends GestureDetector.GestureAdapter {
        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            Gdx.app.log("Panning", "X: " + x + "\nDeltaX: " + deltaX);
            bgX += deltaX;
            return super.pan(x, y, deltaX, deltaY);
        }
    }
}
