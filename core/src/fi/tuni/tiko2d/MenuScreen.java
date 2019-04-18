package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera textCamera;
    private Skin skin;
    private TextButton play;
    private TextButton optionsButton;
    private TextButton about;
    private Stage stage;
    private I18NBundle menuBundle;
    private float titleWidth;
    private float titleHeight;
    private float bgWidth;
    private float bgHeight;
    private float raunoWidth;
    private float raunoHeight;
    private Texture title;
    private Texture background;
    private Texture rauno;
    private Music backgroundMusic;
    private Options options;
    private boolean tutorialCompleted;
    private AssetManager assetManager;

    /**
     * Sets up the main menu
     *
     * @param game  main game class
     */

    public MenuScreen(RaccoonRoll game) {
        this.game = game;
        assetManager = game.getAssetManager();
        options = game.getOptions();
        batch = game.getBatch();
        textCamera = game.getTextCamera();
        tutorialCompleted = game.getCompletedLevels().getBoolean("tutorial", false);
        createTextures();

        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        menuBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MenuBundle"), options.getLocale());

        scaleObjects();
        setUpAudio();

        createTable();

        Gdx.input.setCatchBackKey(false);
    }

    @Override
    public void show() {

    }

    private void createTable() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        if (game.DEBUGGING()) {
            table.setDebug(true);
        }

        skin = assetManager.get("uiskin/comic-ui.json");
        float padding = game.scaleFromFHD(300);
        float scaledButtonPadding = game.scaleFromFHD(25f);
        float buttonHeight = game.scaleFromFHD(200f);

        createButtons();

        table.right();
        table.bottom().padBottom(padding / 2);
        table.padRight(padding);

        table.add(play).width(Value.percentWidth(0.25f, table)).height(buttonHeight);
        table.row().padTop(scaledButtonPadding);
        table.add(optionsButton).uniformX().fillX().height(buttonHeight);
        table.row().padTop(scaledButtonPadding);
        table.add(about).uniformX().fillX().height(buttonHeight);

        addListeners();
    }

    /**
     * Creates textures used in main menu
     */

    private void createTextures() {
        title = assetManager.get("graphics/mainmenu/Logoiso2.png");
        background = assetManager.get("graphics/mainmenu/Valikontausta.png");
        rauno = assetManager.get("graphics/mainmenu/Valikkorauno.png");
    }

    /**
     * Sets background music to main menu
     */

    private void setUpAudio() {
        backgroundMusic = assetManager.get("sounds/backgroundMusic/main_menu_loop.mp3");
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(options.getMusicVolume());
        backgroundMusic.play();
    }

    /**
     * Scales the title and background according to the screen
     */

    private void scaleObjects() {
        titleWidth = game.scaleFromFHD(title.getWidth());
        titleHeight = game.scaleFromFHD(title.getHeight());

        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());

        raunoWidth = game.scaleFromFHD(rauno.getWidth());
        raunoHeight = game.scaleFromFHD(rauno.getHeight());
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
                if (tutorialCompleted) {
                    game.setScreen(new MapScreen(game));
                } else {
                    game.setScreen(new TutorialScreen(game));
                    backgroundMusic.stop();
                }
                dispose();
            }
        });

        optionsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("optionsButton", "Button clicked");
                game.setScreen(new OptionsScreen(game));
                dispose();
            }
        });

        about.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("About", "Button clicked");
                game.setScreen(new AboutScreen(game));
                dispose();
                //for testing purposes
                //game.setScreen(new LevelCompletedScreen(game, 200f, "london"));
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
        batch.draw(rauno, 0, 0, raunoWidth, raunoHeight);
        batch.draw(title, Gdx.graphics.getWidth() / 2 - titleWidth / 2, Gdx.graphics.getHeight() - titleHeight, titleWidth, titleHeight);
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (game.DEBUGGING()) {
            MemoryDebug.memoryUsed(delta);
        }
    }

    @Override
    public void hide() {

    }

    /**
     * Should be called when window resizes but doesn't
     * Should change the stage's viewport when the screen size is changed
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
     * Disposes used assets when they are not needed anymore
     */

    @Override
    public void dispose() {
        stage.dispose();
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "MenuScreen");
        }
    }
}
