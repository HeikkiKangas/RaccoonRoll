package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Screen for displaying options
 *
 * @author Laura Kanerva
 */

public class OptionsScreen extends ApplicationAdapter implements Screen {
    RaccoonRoll game;
    SpriteBatch batch;
    OrthographicCamera worldCamera;
    OrthographicCamera textCamera;
    Skin skin;
    TextButton back;
    TextButton english;
    TextButton finnish;
    TextButton save;
    Stage stage;
    float buttonHeight;
    float bgWidth;
    float bgHeight;
    Texture background;
    private Label titleLabel;
    private Label volumeMusicLabel;
    private Label volumeEffectsLabel;
    private Label languageLabel;
    I18NBundle optionsBundle;

    private Options options;
    private float musicVolume;
    private float effectsVolume;
    private String language;
    TextButton.TextButtonStyle selected;
    TextButton.TextButtonStyle notSelected;
    Container musicContainer;
    Container effectsContainer;

    boolean screenActive = true;


    /**
     * Sets up the options screen
     *
     * @param game  main game class
     */

    public OptionsScreen(RaccoonRoll game) {
        this.game = game;
        options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        musicVolume = options.getMusicVolume();
        effectsVolume = options.getEffectsVolume();
        language = options.getLanguage();
        background = new Texture("graphics/othermenus/Tausta75.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        optionsBundle = I18NBundle.createBundle(Gdx.files.internal("localization/OptionsBundle"), options.getLocale());

        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());
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

        Table options = new Table();
        Table backSave = new Table();

        final Slider volumeMusicSlider = new Slider(0f, 1f, 0.1f, false, skin);
        volumeMusicSlider.setValue(musicVolume);
        volumeMusicSlider.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                musicVolume = volumeMusicSlider.getValue();
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

        musicContainer = new Container(volumeMusicSlider);
        musicContainer.setTransform(true);
        musicContainer.scaleBy(game.scaleFromFHD(3f));
        effectsContainer = new Container(volumeEffectsSlider);
        effectsContainer.setTransform(true);
        effectsContainer.scaleBy(game.scaleFromFHD(3f));

        createLabels();

        float padding = game.scaleFromFHD(50);
        buttonHeight = game.scaleFromFHD(200f);

        table.add(options);

        options.add(titleLabel);
        options.row().pad(padding, 0, 0, 0);
        options.add(volumeMusicLabel);
        options.add(musicContainer);
        options.row().pad(padding * 2, 0, 0, 0);
        options.add(volumeEffectsLabel);
        options.add(effectsContainer);
        options.row().pad(padding, 0, 0, 0);
        options.add(languageLabel);
        options.add(english).width(Value.percentWidth(0.25f, table)).height(buttonHeight).padRight(padding);
        options.add(finnish).width(Value.percentWidth(0.25f, table)).height(buttonHeight);

        table.row().pad(padding * 2, 0, 0, 0);

        table.add(backSave);
        backSave.add(back).width(Value.percentWidth(0.20f, table)).height(buttonHeight).padRight(padding * 10);
        backSave.add(save).width(Value.percentWidth(0.20f, table)).height(buttonHeight).padLeft(padding * 10);

        addListeners();
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
        skin.add("smallfont", game.getTutorialSmallFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));

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

        /* SHOULD keep first language selected
        if(options.getLocale().equals("en")) {
            english.setStyle(selected);
        } else if (options.getLocale().equals("fi")){
            finnish.setStyle(selected);
        }
        */
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
     * Adds listeners to textbuttons
     * Defines the action upon clicking said button
     */

    private void addListeners() {
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Back", "Button clicked");
                game.setScreen(new MenuScreen(game));
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
                game.setScreen(new MenuScreen(game));
                screenActive = false;
            }
        });
    }

    /**
     * Draws the buttons
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
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void resize(int width, int height) {
        // change the stage's viewport when the screen size is changed
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "MazeScreen");
        }
        // dispose of assets when not needed anymore
        stage.dispose();
        background.dispose();
    }

    /**
     * Called when apply button (the one with checkmark) is pressed
     */

    private void saveOptions() {
        options.saveOptions(effectsVolume, musicVolume, language);
    }

}
