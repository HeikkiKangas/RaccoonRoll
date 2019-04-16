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
 * Screen displayed after each level
 *
 * @author Laura Kanerva
 */

public class LevelCompletedScreen extends ApplicationAdapter implements Screen {

    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera textCamera;
    private Skin skin;
    private TextButton ok;
    private Stage stage;
    private int posNum;
    private float timeSpent;
    private float buttonHeight;
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
    private I18NBundle positiveBundle;
    private Options options;
    private AssetManager assetManager;
    private Preferences highScores;
    private boolean newHighscore;
    private Music backgroundMusic;

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

        float highScore = highScores.getFloat(levelName, 0);
        if (highScore > timeSpent || highScore == 0) {
            highScores.putFloat(levelName, timeSpent);
            highScores.flush();
            newHighscore = true;
            Gdx.app.log("HighScore", "New HighScore!");
        }

        positiveBundle = I18NBundle.createBundle(Gdx.files.internal("localization/Positive"), options.getLocale());

        scaleObjects();

        stage = new Stage(new ScreenViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        if (game.DEBUGGING()) {
            Gdx.app.log("Constructor ran", "LevelCompletedScreen");
        }
    }

    @Override
    public void show() {
        Table table = new Table();
        if (game.DEBUGGING()) {
            table.setDebug(true);
        }
        table.setFillParent(true);
        stage.addActor(table);

        createSkin();
        ok = new TextButton(positiveBundle.get("ok"), skin);

        posNum = getRandomPositive();

        Table speechBubble = new Table(skin);
        speechBubble.background("bubble-lower-right");

        createLabels();

        raunoTalk.setAlignment(Align.center);
        speechBubble.add(raunoTalk); //.left()

        float padding = game.scaleFromFHD(600);
        table.add(title);
        table.row().pad(padding / 10, 0, 0, 0);;
        table.add(speechBubble).padRight(padding); //padin sijaan .left
        table.row().pad(padding / 13);
        table.add(timeSpentLabel).padLeft(padding * 1.85f);
        table.row();
        table.add(unlocked);
        //table.row().pad(padding / 3, 0, 0, 0); yksirivisille
        table.row().pad(padding / 4, 0, 0, 0);
        buttonHeight = game.scaleFromFHD(200f);

        table.add(ok).width(Value.percentWidth(0.25f, table)).height(buttonHeight).padLeft(padding * 2);
        ok.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Continue", "Button clicked");
                game.setScreen(new MapScreen(game));
                dispose();
            }
        });

        if (game.DEBUGGING()) {
            Gdx.app.log("Show ran", "LevelCompletedScreen");
        }
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

    private void createLabels() {
        //for testing purposes
        //raunoTalk = new Label(positiveBundle.get("pos13"), skin);
        raunoTalk = new Label(positiveBundle.get("pos" + posNum), skin);
        //pointsLabel = new Label((positiveBundle.get("points")) + points, skin);
        timeSpentLabel = new Label(positiveBundle.get("time") + game.formatTime(timeSpent), skin);
        title = new Label(positiveBundle.get("title"), skin, "title");
        unlocked = new Label(positiveBundle.get("unlocked"), skin);
    }

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

    private float rgbToFloat(float num) {
        return num / 255;
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
            Gdx.app.log("Disposed", "LevelCompletedScreen");
        }
        // dispose of assets when not needed anymore
        stage.dispose();
        /*
        background.dispose();
        rauno.dispose();
        */
    }

    /**
     * Draws a random number between 0 and 14 and returns the number
     */

    public int getRandomPositive() {
        int number = MathUtils.random(0, 14);
        return number;
    }
}
