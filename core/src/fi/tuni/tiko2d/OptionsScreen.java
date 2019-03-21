package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        optionsBundle = I18NBundle.createBundle(Gdx.files.internal("localization/OptionsBundle"), options.getLocale());
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
        musicContainer.scaleBy(3f);
        effectsContainer = new Container(volumeEffectsSlider);
        effectsContainer.setTransform(true);
        effectsContainer.scaleBy(3f);

        createLabels();

        float padding = game.scaleFromFHD(50);
        buttonHeight = game.scaleFromFHD(200f);

        table.add(titleLabel);
        table.row().pad(padding, 0, 0, 0);
        table.add(volumeMusicLabel);
        table.add(musicContainer);
        //table.add(volumeMusicSlider).width(Value.percentWidth(0.25f, table));
        table.row().pad(padding * 2, 0, 0, 0);
        table.add(volumeEffectsLabel);
        table.add(effectsContainer);
        //table.add(volumeEffectsSlider).width(Value.percentWidth(0.25f, table));
        table.row().pad(padding, 0, 0, 0);
        table.add(languageLabel);
        table.add(english).width(Value.percentWidth(0.25f, table)).height(buttonHeight).padRight(padding);
        table.add(finnish).width(Value.percentWidth(0.25f, table)).height(buttonHeight);
        table.row().pad(padding * 2, 0, 0, 0);
        table.add(back).width(Value.percentWidth(0.20f, table)).height(buttonHeight).padRight(padding * 2);
        table.add(save).width(Value.percentWidth(0.20f, table)).height(buttonHeight);

        addListeners();
    }

    private void createSkin() {
        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));

        selected = skin.get("selected", TextButton.TextButtonStyle.class);
        notSelected = skin.get("default", TextButton.TextButtonStyle.class);
    }

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

    private void createLabels() {
        titleLabel = new Label(optionsBundle.get("title"), skin, "title" );
        volumeMusicLabel = new Label(optionsBundle.get("musicSlider"), skin );
        volumeEffectsLabel = new Label(optionsBundle.get("effectsSlider"), skin );
        languageLabel = new Label(optionsBundle.get("languageTitle"), skin, "title");
    }

    private void addListeners() {
        back.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Back", "Button clicked");
                game.setScreen(new MenuScreen(game));
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

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void hide() {

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
    }

    /**
     * Called when apply button (the one with checkmark) is pressed
     */

    private void saveOptions() {
        options.saveOptions(effectsVolume, musicVolume, language);
    }

}
