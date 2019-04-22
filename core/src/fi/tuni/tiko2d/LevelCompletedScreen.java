package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
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
 * Screen displayed after level has been completed
 *
 * @author Laura Kanerva
 */

public class LevelCompletedScreen extends ApplicationAdapter implements Screen {

    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera textCamera;
    private Skin skin;
    private Stage stage;
    private int posNum;
    private float timeSpent;
    private float bgWidth;
    private float bgHeight;
    private float raunoHeight;
    private float raunoWidth;
    private Texture background;
    private Texture rauno;
    private Label raunoTalk;
    private Label timeSpentLabel;
    private Label title;
    private Label unlocked;
    private Label highscore;
    private I18NBundle positiveBundle;
    private Options options;
    private AssetManager assetManager;
    private Preferences highScores;
    private boolean newHighscore;
    private boolean levelUnlocked;
    private Music backgroundMusic;

    /**
     * Sets up the screen showed after completing a level
     *
     * @param game      main game class
     * @param timeSpent amount of time spent on the level
     * @param levelName name of the completed level
     */

    public LevelCompletedScreen(RaccoonRoll game, float timeSpent, String levelName) {
        this.game = game;
        this.timeSpent = timeSpent;
        highScores = game.getHighScores();
        assetManager = game.getAssetManager();
        options = game.getOptions();
        batch = game.getBatch();
        textCamera = game.getTextCamera();
        rauno = assetManager.get("graphics/othermenus/pieniRauno.png");
        background = assetManager.get("graphics/othermenus/Tausta75.png");
        backgroundMusic = assetManager.get("sounds/backgroundMusic/main_menu_loop.mp3");
        backgroundMusic.setVolume(game.getOptions().getMusicVolume());
        backgroundMusic.play();

        checkHighscore(levelName);

        positiveBundle = I18NBundle.createBundle(Gdx.files.internal("localization/Positive"), options.getLocale());

        scaleObjects();

        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        createTables();
        if (game.DEBUGGING()) {
            Gdx.app.log("Constructor ran", "LevelCompletedScreen");
        }
    }

    @Override
    public void show() {

    }

    /**
     * Checks the previous highscore and compares it to the last score
     *
     * @param levelName name of the completed level
     */

    private void checkHighscore(String levelName) {
        float highScore = highScores.getFloat(levelName, 0);
        if(highScore == 0) {
            levelUnlocked = true;
        }
        if (highScore > timeSpent || highScore == 0) {
            highScores.putFloat(levelName, timeSpent);
            highScores.flush();
            newHighscore = true;
            if (game.DEBUGGING()) {
                Gdx.app.log("HighScore", "New HighScore!");
            }
        }
    }

    /**
     * Creates tables and places all elements in right places
     */

    private void createTables() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        if (game.DEBUGGING()) {
            table.setDebug(true);
        }

        skin = assetManager.get("uiskin/comic-ui.json");
        TextButton ok = new TextButton(positiveBundle.get("ok"), skin);
        posNum = getRandomPositive();
        raunoTalk = new Label(positiveBundle.get("pos" + posNum), skin);
        float padding = game.scaleFromFHD(600);
        float buttonHeight = game.scaleFromFHD(200f);

        Table speechBubble = new Table(skin);
        speechBubble.background("bubble-lower-right");
        raunoTalk.setAlignment(Align.center);
        speechBubble.add(raunoTalk);

        createLabels();


        table.add(title).top();
        table.row().pad(padding / 10, 0, 0, 0);
        table.add(speechBubble).left();

        if(newHighscore) {
            table.row().padTop(padding / 14);
            table.add(highscore).padLeft(padding * 1.85f);
        }

        if(!newHighscore && !levelUnlocked) {
            table.row().padTop(padding / 5);
        } else {
            table.row().padTop(padding / 14);
        }

        table.add(timeSpentLabel).padLeft(padding * 1.85f);

        if(levelUnlocked) {
            table.row().padTop(padding / 14);
            table.add(unlocked).padLeft(padding * 1.85f);
        }

        table.row().pad(padding / 5, 0, 0, 0);

        //table.add(ok).width(Value.percentWidth(0.25f, table)).height(buttonHeight).padLeft(padding * 2);
        table.add(ok).width(Value.percentWidth(0.25f, table)).height(buttonHeight).right().bottom();
        ok.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Continue", "Button clicked");
                game.setScreen(new MapScreen(game));
                dispose();
            }
        });
    }

    /**
     * Scales the image and background according to the screen
     */

    private void scaleObjects() {
        raunoWidth = game.scaleFromFHD(rauno.getWidth());
        raunoHeight = game.scaleFromFHD(rauno.getHeight());
        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());
    }

    /**
     * Creates labels
     */

     private void createLabels() {
        //for testing purposes
        //raunoTalk = new Label(positiveBundle.get("pos14"), skin);
        raunoTalk = new Label(positiveBundle.get("pos" + posNum), skin);
        timeSpentLabel = new Label(positiveBundle.get("time") + game.formatTime(timeSpent), skin);
        title = new Label(positiveBundle.get("title"), skin, "title");
        unlocked = new Label(positiveBundle.get("unlocked"), skin);
        highscore = new Label(positiveBundle.get("highscore"), skin);
    }

    /**
     * Renders the background, text and button
     *
     * @param delta time since last frame was drawn
     */

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(textCamera.combined);
        batch.begin();
        batch.draw(background, 0, 0, bgWidth, bgHeight);
        batch.draw(rauno, 0, 0, raunoWidth, raunoHeight);
        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void hide() {

    }

    /**
     * Should be called when window resizes
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
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "LevelCompletedScreen");
        }
        stage.dispose();
    }

    /**
     * Draws a random number between 0 and 14 and returns the number
     */

    public int getRandomPositive() {
        return MathUtils.random(0, 14);
    }
}
