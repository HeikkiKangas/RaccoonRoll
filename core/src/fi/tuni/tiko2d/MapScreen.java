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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Screen for displaying a map with all levels
 *
 * @author
 */

public class MapScreen extends ApplicationAdapter implements Screen {

    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private OrthographicCamera textCamera;
    private Stage stage;
    private Texture map1;
    private Texture map2;
    private float bgHeight;
    private float bgWidth;
    private InputMultiplexer multiplexer;

    private ImageButton ukButton1;
    private ImageButton ukButton2;
    private Group ukButtons;
    private Group buttons;

    private Texture notStarted;
    private Texture started;
    private Texture done;

    private float bgX;

    public MapScreen(RaccoonRoll game) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        map1 = new Texture("graphics/map1.png");
        map2 = new Texture("graphics/map2.png");

        notStarted = new Texture("graphics/worldmap/Nappipun.png");
        started = new Texture("graphics/worldmap/Nappikelt.png");
        done = new Texture("graphics/worldmap/Nappivih.png");

        int btnSize = (int) game.scaleVertical(75);

        stage = new Stage(new ScreenViewport(), batch);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new GestureDetector(new MapScroller()));

        bgHeight = game.scaleVertical(map1.getHeight());
        bgWidth = game.scaleVertical(map1.getWidth());

        Gdx.input.setInputProcessor(multiplexer);
        Gdx.input.setCatchBackKey(true);

        createButtons();
        bgX = game.scaleVertical(-600);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (game.DEBUGGING()) {
            MemoryDebug.memoryUsed(delta);
        }
        Gdx.gl.glClearColor(49f / 255, 36f / 255, 209f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(textCamera.combined);
        batch.begin();
        batch.draw(map1, bgX, 0, bgWidth, bgHeight);
        batch.draw(map2, bgX + bgWidth, 0, bgWidth, bgHeight);


        if (bgX > 0) {
            batch.draw(map1, bgX - bgWidth * 2, 0, bgWidth, bgHeight);
            batch.draw(map2, bgX - bgWidth, 0, bgWidth, bgHeight);
        } else if (bgX < -(bgWidth * 2 - Gdx.graphics.getWidth())) {
            batch.draw(map1, bgX + bgWidth * 2, 0, bgWidth, bgHeight);
            batch.draw(map2, bgX + bgWidth * 3, 0, bgWidth, bgHeight);
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
            buttons.moveBy(deltaX, 0);
            Gdx.app.log("buttons X", "" + buttons.getX());
            if (bgX > bgWidth * 2) {
                bgX -= bgWidth * 2;
                buttons.moveBy(-(bgWidth * 2), 0);
                Gdx.app.log("buttons X -", "" + buttons.getX());
            } else if (bgX < -bgWidth * 2) {
                bgX += bgWidth * 2;
                buttons.moveBy(bgWidth * 2, 0);
                Gdx.app.log("buttons X +", "" + buttons.getX());
            }

            return super.pan(x, y, deltaX, deltaY);
        }
    }

    private void createButtons() {
        buttons = new Group();
        /*
        buttons.setTransform(true);
        buttons.setScale(game.scaleVertical(1));
        */
        createUkButtons();
        stage.addActor(buttons);
    }

    private void createUkButtons() {
        ukButtons = new Group();

        ukButton1 = new ImageButton(new TextureRegionDrawable(notStarted));
        ukButton1.setPosition(game.scaleVertical(1300), game.scaleVertical(325));
        ukButton1.setRound(true);
        ukButton1.setTransform(true);
        ukButton1.setScale(game.scaleVertical(1));

        ukButton2 = new ImageButton(new TextureRegionDrawable(notStarted));
        ukButton2.setPosition(game.scaleVertical(1300) - bgWidth * 2, game.scaleVertical(325));
        ukButton2.setRound(true);
        ukButton2.setTransform(true);
        ukButton2.setScale(game.scaleVertical(1));

        ukButtons.addActor(ukButton1);
        ukButtons.addActor(ukButton2);

        ukButtons.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("UK", "Clicked");
            }
        });

        buttons.addActor(ukButtons);
    }
}
