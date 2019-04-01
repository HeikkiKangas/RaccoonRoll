package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
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
    private InputMultiplexer multiplexer;

    private float bgX;

    public MapScreen(RaccoonRoll game) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        background = new Texture("graphics/Karttapienid.jpg");

        stage = new Stage(new ScreenViewport());

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new GestureDetector(new MapScroller()));

        bgHeight = game.scaleVertical(background.getHeight());
        bgWidth = game.scaleVertical(background.getWidth());

        Gdx.input.setInputProcessor(multiplexer);
        Gdx.input.setCatchBackKey(true);

        bgX = -300;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(49f / 255, 36f / 255, 209f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(textCamera.combined);
        batch.begin();
        batch.draw(background, bgX, 0, bgWidth, bgHeight);


        if (bgX > 0) {
            batch.draw(background, bgX - bgWidth, 0, bgWidth, bgHeight);
        } else if (bgX < -(bgWidth - Gdx.graphics.getWidth())) {
            batch.draw(background, bgX + bgWidth, 0, bgWidth, bgHeight);
        }

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.setScreen(new MenuScreen(game));
            dispose();
        }
    }

    @Override
    public void hide() {

    }

    class MapScroller extends GestureDetector.GestureAdapter {
        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (game.DEBUGGING()) {
                Gdx.app.log("Panning",
                        "\nX: " + x + "\nDeltaX: " + deltaX + "\nbgX: " + bgX);
            }
            bgX += deltaX;

            if (bgX > bgWidth) {
                bgX -= bgWidth;
            } else if (bgX < -bgWidth) {
                bgX += bgWidth;
            }

            return super.pan(x, y, deltaX, deltaY);
        }
    }
}
