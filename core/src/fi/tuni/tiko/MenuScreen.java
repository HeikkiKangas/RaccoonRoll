package fi.tuni.tiko;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Screen for displaying main menu
 *
 * @author Laura Kanerva
 */

public class MenuScreen extends ApplicationAdapter implements Screen {
    RaccoonRoll game;
    SpriteBatch batch;
    OrthographicCamera worldCamera;
    OrthographicCamera textCamera;
    Skin skin;
    TextButton play;
    TextButton options;
    TextButton about;
    Stage stage;
    I18NBundle menuBundle;
    float buttonHeight;
    float titleWidth;
    float titleHeight;
    float bgWidth;
    float bgHeight;
    Texture title;
    Texture background;
    private Music backgroundMusic;

    /**
     * Sets up the main menu
     *
     * @param game  main game class
     */

    public MenuScreen(RaccoonRoll game) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        title = new Texture("graphics/mainmenu/Logoiso2.png");
        background = new Texture("graphics/mainmenu/Valikontausta.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        menuBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MenuBundle"), game.getLocale());

        titleWidth = game.scaleFromFHD(title.getWidth());
        titleHeight = game.scaleFromFHD(title.getHeight());

        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/backgroundMusic/main_menu_loop.mp3"));
        backgroundMusic.setVolume(game.getMusicVolume());
        backgroundMusic.play();
    }

    @Override
    public void show() {
        Table table = new Table();
        table.setFillParent(true);
        //table.setDebug(true);
        stage.addActor(table);

        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));

        play = new TextButton(menuBundle.get("playButton"), skin);
        options = new TextButton(menuBundle.get("optionsButton"), skin);
        about = new TextButton(menuBundle.get("aboutButton"), skin);

        float padding = game.scaleFromFHD(300);
        table.row().pad(padding, 0, 0, 0);
        table.right();
        table.padRight(padding);
        buttonHeight = game.scaleFromFHD(200f);
        float scaledButtonPadding = game.scaleFromFHD(25f);
        table.add(play).width(Value.percentWidth(0.25f, table)).height(buttonHeight);
        table.row().padTop(scaledButtonPadding);
        table.add(options).uniformX().fillX().height(buttonHeight);
        table.row().padTop(scaledButtonPadding);
        table.add(about).uniformX().fillX().height(buttonHeight);

        play.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Play", "Button clicked");
                game.setScreen(new MazeScreen(game, "tutorial"));
                backgroundMusic.stop();
            }
        });

        options.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Options", "Button clicked");
                game.setScreen(new OptionsScreen(game));
                backgroundMusic.stop();
            }
        });

        about.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("About", "Button clicked");
                game.setScreen(new AboutScreen(game));
                //for testing purposes
                //game.setScreen(new LevelCompletedScreen(game));
                backgroundMusic.stop();
            }
        });
    }

    /**
     * Draws the background, title and buttons
     *
     * @param delta time since last frame was drawn
     */

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(textCamera.combined);
        batch.begin();
        batch.draw(background, 0, 0, bgWidth, bgHeight);
        batch.draw(title, Gdx.graphics.getWidth() / 2 - titleWidth / 2, Gdx.graphics.getHeight() - titleHeight, titleWidth, titleHeight);
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void hide() {

    }

    /**
     * Should be called when window resizes but doesn't
     *
     * @param width  not used
     * @param height not used
     */

    @Override
    public void resize(int width, int height) {
        // change the stage's viewport when the screen size is changed
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes used assets
     */

    @Override
    public void dispose() {
        // dispose of assets when not needed anymore
        stage.dispose();
    }
}
