package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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

    RaccoonRoll game;
    SpriteBatch batch;
    OrthographicCamera worldCamera;
    OrthographicCamera textCamera;
    Skin skin;
    TextButton ok;
    Stage stage;
    int posNum;
    int points;
    float timeSpent;
    float buttonHeight;
    float bgWidth;
    float bgHeight;
    float raunoHeight;
    float raunoWidth;
    Texture background;
    Texture rauno;
    Label raunoTalk;
    Label pointsLabel;
    Label timeSpentLabel;
    I18NBundle positiveBundle;
    private Options options;

    public LevelCompletedScreen(RaccoonRoll game, int points, float timeSpent) {
        this.game = game;
        this.points = points;
        this.timeSpent = timeSpent;
        options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        rauno = new Texture("graphics/othermenus/pieniRauno.png");
        background = new Texture("graphics/othermenus/Tausta75.png");

        positiveBundle = I18NBundle.createBundle(Gdx.files.internal("localization/Positive"), options.getLocale());

        raunoWidth = game.scaleFromFHD(rauno.getWidth());
        raunoHeight = game.scaleFromFHD(rauno.getHeight());
        bgWidth = game.scaleFromFHD(background.getWidth());
        bgHeight = game.scaleFromFHD(background.getHeight());

        stage = new Stage(new ScreenViewport());
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
        speechBubble.background("bubble-lower-left");

        createLabels();

        raunoTalk.setAlignment(Align.center);
        speechBubble.add(raunoTalk);

        float padding = game.scaleFromFHD(600);
        table.add(speechBubble).padRight(padding);
        table.row();
        table.add(pointsLabel).padLeft(padding * 1.75f);
        table.row().pad(padding / 15, 0, 0, 0);
        table.add(timeSpentLabel).padLeft(padding * 1.75f);
        table.row().pad(padding / 2, 0, 0, 0);
        buttonHeight = game.scaleFromFHD(200f);

        table.add(ok).width(Value.percentWidth(0.25f, table)).height(buttonHeight).padLeft(padding * 2);
        ok.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Continue", "Button clicked");
                game.setScreen(new MapScreen(game));
            }
        });

        if (game.DEBUGGING()) {
            Gdx.app.log("Show ran", "LevelCompletedScreen");
        }
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
    }

    private void createLabels() {
        raunoTalk = new Label(positiveBundle.get("pos" + posNum), skin);
        pointsLabel = new Label((positiveBundle.get("points")) + points, skin);
        timeSpentLabel = new Label(positiveBundle.get("time") + formatTime(), skin);
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
    }

    /**
     * Draws a random number between 0 and 14 and returns the number
     */

    public int getRandomPositive() {
        int number = MathUtils.random(0, 14);
        return number;
    }

    /**
     * Formats time from float seconds
     *
     * @return time in m:ss
     */
    private String formatTime() {
        int time = (int) timeSpent;
        int minutes = time / 60;
        int seconds = time % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
