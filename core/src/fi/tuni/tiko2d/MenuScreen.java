package fi.tuni.tiko2d;

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
    TextButton optionsButton;
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
    private Options options;
    Boolean screenChanged = false;

    /**
     * Sets up the main menu
     *
     * @param game  main game class
     */

    public MenuScreen(RaccoonRoll game) {
        this.game = game;
        options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        title = new Texture("graphics/mainmenu/Logoiso2.png");
        background = new Texture("graphics/mainmenu/Valikontausta.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        menuBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MenuBundle"), options.getLocale());

        scaleObjects();

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/backgroundMusic/main_menu_loop.mp3"));
        backgroundMusic.setVolume(options.getMusicVolume());
        backgroundMusic.play();
    }

    @Override
    public void show() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        if (game.DEBUGGING()) {
            table.setDebug(true);
        }

        createSkin();
        createButtons();

        float padding = game.scaleFromFHD(300);
        table.right();
        table.bottom().padBottom(padding / 2);
        table.padRight(padding);
        buttonHeight = game.scaleFromFHD(200f);
        float scaledButtonPadding = game.scaleFromFHD(25f);
        table.add(play).width(Value.percentWidth(0.25f, table)).height(buttonHeight);
        table.row().padTop(scaledButtonPadding);
        table.add(optionsButton).uniformX().fillX().height(buttonHeight);
        table.row().padTop(scaledButtonPadding);
        table.add(about).uniformX().fillX().height(buttonHeight);

        addListeners();
    }

    /**
     * Scales the title and background according to the screen
     */

    private void scaleObjects() {
        titleWidth = game.scaleFromFHD(title.getWidth());
        titleHeight = game.scaleFromFHD(title.getHeight());

        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());
    }

    /**
     * Creates skin and assigns fonts to different styles
     */

    private void createSkin() {
        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));
    }

    /**
     * Creates textbuttons
     */

    private void createButtons() {
        play = new TextButton(menuBundle.get("playButton"), skin);
        optionsButton = new TextButton(menuBundle.get("optionsButton"), skin);
        about = new TextButton(menuBundle.get("aboutButton"), skin);
    }

    /**
     * Adds listeners to textbuttons
     * Defines the action upon clicking said button
     */

    private void addListeners() {
        play.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Play", "Button clicked");
                screenChanged = true;
                game.setScreen(new MazeScreen(game, "london"));
                backgroundMusic.stop();
            }
        });

        optionsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("optionsButton", "Button clicked");
                game.setScreen(new OptionsScreen(game));
                backgroundMusic.stop();
            }
        });

        about.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("About", "Button clicked");
                //game.setScreen(new AboutScreen(game));
                //for testing purposes
                game.setScreen(new LevelCompletedScreen(game));
                //game.setScreen(new MapScreen(game));
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

        if(screenChanged) {
            dispose();
        }
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
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "MenuScreen");
        }
        // dispose of assets when not needed anymore
        //play.clearListeners();
        stage.dispose();
    }
}
