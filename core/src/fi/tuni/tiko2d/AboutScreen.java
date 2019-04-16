package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Screen for displaying information
 *
 * @author Laura Kanerva
 */

// whatever changes we made (also, should link to license)

public class AboutScreen extends ApplicationAdapter implements Screen {

    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera textCamera;
    private Skin skin;
    private TextButton back;
    private Stage stage;
    private float buttonHeight;
    private float bgWidth;
    private float bgHeight;
    private Texture background;
    private Label programmerTitle;
    private Label programmer1;
    private Label programmer2;
    private Label scrumTitle;
    private Label scrum;
    private Label graphicsTitle;
    private Label graphics;
    private Label musicTitle;
    private Label music;
    private Label fontCredit;
    private Label uiCredit;
    private I18NBundle aboutBundle;
    private Boolean screenActive = true;
    private Options options;
    private AssetManager assetManager;

    /**
     * Sets up the information screen
     *
     * @param game  main game class
     */

    public AboutScreen(RaccoonRoll game) {
        this.game = game;
        assetManager = game.getAssetManager();
        options = game.getOptions();
        batch = game.getBatch();
        textCamera = game.getTextCamera();
        background = assetManager.get("graphics/othermenus/Tausta75.png");

        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        aboutBundle = I18NBundle.createBundle(Gdx.files.internal("localization/AboutBundle"), options.getLocale());

        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());
    }

    @Override
    public void show() {
        Table table = new Table();
        table.setFillParent(true);
        Table buttonTable = new Table();
        stage.addActor(table);
        if (game.DEBUGGING()) {
            table.setDebug(true);
            buttonTable.setDebug(true);
        }

        createSkin();
        back = new TextButton(aboutBundle.get("backButton"), skin);
        createLabels();

        float padding = game.scaleFromFHD(50);
        table.row().padTop(padding / 2);
        table.add(programmerTitle);
        table.row();
        table.add(programmer1);
        table.row();
        table.add(programmer2);
        table.row().pad(padding, 0, 0, 0);
        table.add(graphicsTitle);
        table.row();
        table.add(graphics);
        table.row().pad(padding, 0, 0, 0);
        table.add(scrumTitle);
        table.row();
        table.add(scrum);
        table.row().pad(padding, 0, 0, 0);
        table.add(musicTitle);
        table.row();
        table.add(music);
        table.row();
        table.row().pad(padding, 0, 0, 0);
        table.add(uiCredit);
        table.row();
        table.add(fontCredit);
        table.row();

        //buttonTable.padRight(padding * 26);
        buttonHeight = game.scaleFromFHD(200f);
        stage.addActor(buttonTable);
        buttonTable.add(back).width(Value.percentWidth(0.20f, table)).height(buttonHeight);
        buttonTable.padBottom(padding * 5);
        buttonTable.padLeft(padding * 9);

        back.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Back", "Button clicked");
                game.setScreen(new MenuScreen(game));
                screenActive = false;
            }
        });
    }

    /**
     * Creates skin and assigns fonts to different styles
     */

    private void createSkin() {
        skin = assetManager.get("uiskin/comic-ui.json");
        /*
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());
        skin.add("smallfont", game.getTutorialSmallFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));
        */
    }

    /**
     * Creates labels
     */

    private void createLabels() {
        programmerTitle = new Label(aboutBundle.get("programmerTitle"), skin, "title");
        programmer1 = new Label(aboutBundle.get("programmer1"), skin);
        programmer2 = new Label(aboutBundle.get("programmer2"), skin);
        scrumTitle = new Label(aboutBundle.get("scrumTitle"), skin, "title");
        scrum = new Label(aboutBundle.get("scrum"), skin);
        graphicsTitle = new Label(aboutBundle.get("graphicsTitle"), skin, "title");
        graphics = new Label(aboutBundle.get("graphics"), skin);
        musicTitle = new Label(aboutBundle.get("musicTitle"), skin, "title");
        music = new Label(aboutBundle.get("music"), skin);
        fontCredit = new Label(aboutBundle.get("fontCredit"), skin, "credits");
        fontCredit.setAlignment(Align.top, Align.center);
        uiCredit = new Label(aboutBundle.get("uiCredit"), skin, "credits");
        uiCredit.setAlignment(Align.top, Align.center);
    }

    /**
     * Draws the information and a button
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

    }

    @Override
    public void resize(int width, int height) {
        // change the stage's viewport when the screen size is changed
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        // dispose of assets when not needed anymore
        stage.dispose();
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "AboutScreen");
        }
    }
}
