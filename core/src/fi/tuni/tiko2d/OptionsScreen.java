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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.Locale;

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
    Stage stage;
    float buttonHeight;
    private Label titleLabel;
    private Label volumeMusicLabel;
    private Label volumeEffectsLabel;
    private Label languageLabel;
    I18NBundle optionsBundle;

    /*
    private Options options;
    private float musicVolume;
    private float effectsVolume;
    private String language;
    */

    /**
     * Sets up the options screen
     *
     * @param game  main game class
     */

    public OptionsScreen(RaccoonRoll game) {
        this.game = game;
        //options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        optionsBundle = I18NBundle.createBundle(Gdx.files.internal("localization/OptionsBundle"), game.getLocale());
        //optionsBundle = I18NBundle.createBundle(Gdx.files.internal("localization/OptionsBundle"), options.getLocale());
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

        //volumeMusicSlider.setHeight(game.scaleVertical(500));
        //volumeMusicSlider.setWidth(game.scaleHorizontal(500));

        volumeMusicSlider.setValue(game.getMusicVolume());
        //volumeMusicSlider.setValue(options.getMusicVolume());

        volumeMusicSlider.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                game.setMusicVolume(volumeMusicSlider.getValue());
                //musicVolume = volumeMusicSlider.getValue();
                return false;
            }
        });

        final Slider volumeEffectsSlider = new Slider(0f, 1f, 0.1f, false, skin);

        volumeEffectsSlider.setValue(game.getEffectsVolume());
        //volumeEffectsSlider.setValue(options.getEffectsVolume());

        volumeEffectsSlider.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                game.setEffectsVolume(volumeEffectsSlider.getValue());
                //effectsVolume = volumeEffectsSlider.getValue();
                return false;
            }
        });

        createLabels();

        float padding = game.scaleFromFHD(50);
        buttonHeight = game.scaleFromFHD(200f);

        table.add(titleLabel);
        table.row().pad(padding * 2, 0, 0, 0);
        table.add(volumeMusicLabel);
        table.add(volumeMusicSlider);
        table.row().pad(padding, 0, 0, 0);
        table.add(volumeEffectsLabel);
        table.add(volumeEffectsSlider);

        table.row().pad(padding * 2, 0, 0, 0);

        table.add(languageLabel);
        table.row().pad(padding * 2, 0, 0, 0);
        table.add(english);
        table.add(finnish);

        table.row().pad(padding * 2, 0, 0, 0);
        table.add(back).padRight(padding * 10); //.width(Value.percentWidth(0.25f, table)).height(buttonHeight);

        addListeners();
    }

    private void createSkin() {
        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));
    }

    private void createButtons() {
        back = new TextButton(optionsBundle.get("backButton"), skin);
        english = new TextButton(optionsBundle.get("englishButton"), skin);
        finnish = new TextButton(optionsBundle.get("finnishButton"), skin);
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
                game.setLocale(new Locale("en", "GB"));
                //language = "en";
            }
        });

        finnish.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Finnish", "is chosen language");
                game.setLocale(new Locale("fi", "FI"));
                //language = "fi";
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
    /*
    private void saveOptions() {
        options.saveOptions(effectsVolume, musicVolume, language);
    }
    */
}
