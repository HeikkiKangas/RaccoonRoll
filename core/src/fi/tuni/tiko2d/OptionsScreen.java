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
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Screen for displaying options
 *
 * @author Laura Kanerva
 */

public class OptionsScreen extends ApplicationAdapter implements Screen {
    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera textCamera;
    private Skin skin;
    private TextButton back;
    private TextButton english;
    private TextButton finnish;
    private TextButton save;
    private Stage stage;
    private float buttonHeight;
    private float bgWidth;
    private float bgHeight;
    private Texture background;
    private Label titleLabel;
    private Label volumeMusicLabel;
    private Label volumeEffectsLabel;
    private Label languageLabel;
    private I18NBundle optionsBundle;
    private Options options;
    private float musicVolume;
    private float effectsVolume;
    private String language;
    private TextButton.TextButtonStyle selected;
    private TextButton.TextButtonStyle notSelected;
    private Container musicContainer;
    private Container effectsContainer;
    private boolean screenActive = true;
    private MazeScreen mazeScreen;
    private AssetManager assetManager;
    private Music backgroundMusic;


    /**
     * Sets up the options screen
     *
     * @param game  main game class
     */

    public OptionsScreen(RaccoonRoll game) {
        this.game = game;
        assetManager = game.getAssetManager();
        options = game.getOptions();
        batch = game.getBatch();
        textCamera = game.getTextCamera();
        musicVolume = options.getMusicVolume();
        effectsVolume = options.getEffectsVolume();
        language = options.getLanguage();
        background = assetManager.get("graphics/othermenus/Tausta75.png");
        backgroundMusic = assetManager.get("sounds/backgroundMusic/main_menu_loop.mp3");

        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        optionsBundle = I18NBundle.createBundle(Gdx.files.internal("localization/OptionsBundle"), options.getLocale());

        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());
    }

    /**
     * Sets up the options screen with MazeScreen to go back to
     *
     * @param game       main game class
     * @param mazeScreen the MazeScreen to go back to
     */
    public OptionsScreen(RaccoonRoll game, MazeScreen mazeScreen) {
        this.game = game;
        this.mazeScreen = mazeScreen;
        assetManager = game.getAssetManager();
        options = game.getOptions();
        batch = game.getBatch();
        textCamera = game.getTextCamera();
        musicVolume = options.getMusicVolume();
        effectsVolume = options.getEffectsVolume();
        language = options.getLanguage();
        background = assetManager.get("graphics/othermenus/Tausta75.png");
        backgroundMusic = assetManager.get("sounds/backgroundMusic/main_menu_loop.mp3");

        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        optionsBundle = I18NBundle.createBundle(Gdx.files.internal("localization/OptionsBundle"), options.getLocale());

        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());
    }

    @Override
    public void show() {
        Table table = new Table();
        table.setFillParent(true);
        Table backSave = new Table();
        stage.addActor(table);
        if (game.DEBUGGING()) {
            table.setDebug(true);
            backSave.setDebug(true);
        }

        createSkin();
        createButtons();

        final Slider volumeMusicSlider = new Slider(0f, 1f, 0.1f, false, skin);
        volumeMusicSlider.setValue(musicVolume);
        volumeMusicSlider.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                musicVolume = volumeMusicSlider.getValue();
                backgroundMusic.setVolume(musicVolume);
                return false;
            }
        });

        final Slider volumeEffectsSlider = new Slider(0f, 1f, 0.1f, false, skin);
        volumeEffectsSlider.setValue(effectsVolume);
        volumeEffectsSlider.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                effectsVolume = volumeEffectsSlider.getValue();
                return false;
            }
        });

        createContainers(volumeMusicSlider, volumeEffectsSlider);
        createLabels();

        float padding = game.scaleFromFHD(50);
        buttonHeight = game.scaleFromFHD(200f);

        table.add(titleLabel);
        table.row().pad(padding, 0, 0, 0);
        table.add(volumeMusicLabel);
        table.add(musicContainer);
        table.row().pad(padding * 2, 0, 0, 0);
        table.add(volumeEffectsLabel);
        table.add(effectsContainer);
        table.row().pad(padding, 0, 0, 0);
        table.add(languageLabel);
        table.add(english).width(Value.percentWidth(0.25f, table)).height(buttonHeight).padRight(padding);
        table.add(finnish).width(Value.percentWidth(0.25f, table)).height(buttonHeight);

        stage.addActor(backSave);
        backSave.add(back).width(Value.percentWidth(0.20f, table)).height(buttonHeight).padRight(padding * 10);
        backSave.add(save).width(Value.percentWidth(0.20f, table)).height(buttonHeight).padLeft(padding * 10);
        backSave.padBottom(padding * 5);
        backSave.padLeft(padding * 38);

        addListeners();
    }

    /**
     * Creates skin and assigns fonts to different styles
     */

    private void createSkin() {
        skin = assetManager.get("uiskin/comic-ui.json");

        selected = skin.get("selected", TextButton.TextButtonStyle.class);
        notSelected = skin.get("default", TextButton.TextButtonStyle.class);
    }

    /**
     * Creates textbuttons
     */

    private void createButtons() {
        back = new TextButton(optionsBundle.get("backButton"), skin);
        english = new TextButton(optionsBundle.get("englishButton"), skin);
        finnish = new TextButton(optionsBundle.get("finnishButton"), skin);
        save = new TextButton(optionsBundle.get("saveButton"), skin);
        if (language.equals("fi")) {
            finnish.setStyle(selected);
            english.setStyle(notSelected);
        } else {
            english.setStyle(selected);
            finnish.setStyle(notSelected);
        }
    }

    /**
     * Creates labels
     */

    private void createLabels() {
        titleLabel = new Label(optionsBundle.get("title"), skin, "title" );
        volumeMusicLabel = new Label(optionsBundle.get("musicSlider"), skin );
        volumeEffectsLabel = new Label(optionsBundle.get("effectsSlider"), skin );
        languageLabel = new Label(optionsBundle.get("languageTitle"), skin, "title");
    }

    /**
     * Creates containers for sliders
     */

    private void createContainers(Slider music, Slider effects) {
        musicContainer = new Container(music);
        musicContainer.setTransform(true);
        musicContainer.scaleBy(game.scaleFromFHD(3f));
        effectsContainer = new Container(effects);
        effectsContainer.setTransform(true);
        effectsContainer.scaleBy(game.scaleFromFHD(3f));
    }

    /**
     * Adds listeners to textbuttons
     * Defines the action upon clicking said button
     */

    private void addListeners() {
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Back", "Button clicked");
                if (mazeScreen != null) {
                    game.setScreen(mazeScreen);
                } else {
                    game.setScreen(new MenuScreen(game));
                }
                screenActive = false;
            }
        });

        english.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("English", "is chosen language");
                language = "en";
                english.setStyle(selected);
                finnish.setStyle(notSelected);
            }
        });

        finnish.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Finnish", "is chosen language");
                language = "fi";
                finnish.setStyle(selected);
                english.setStyle(notSelected);
            }
        });

        save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Save", "Button clicked");
                saveOptions();
                if (mazeScreen != null) {
                    game.setScreen(mazeScreen);
                } else {
                    game.setScreen(new MenuScreen(game));
                }
                screenActive = false;
            }
        });
    }

    /**
     * Draws background, buttons and sliders
     * Calls dispose() when screen is not active
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
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if(!screenActive) {
            dispose();
        }
        if (game.DEBUGGING()) {
            MemoryDebug.memoryUsed(delta);
        }
    }

    @Override
    public void hide() {
        dispose();
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
        stage.getViewport().update(width, height, true);
    }

    /**
     * Disposes used assets when they are not needed anymore
     */

    @Override
    public void dispose() {
        stage.dispose();
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "OptionsScreen");
        }
    }

    /**
     * Called when Save button is pressed
     */

    private void saveOptions() {
        options.saveOptions(effectsVolume, musicVolume, language);
    }

}
