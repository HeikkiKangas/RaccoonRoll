package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Screen for displaying information
 *
 * @author Laura Kanerva
 */

public class AboutScreen extends ApplicationAdapter implements Screen {

    RaccoonRoll game;
    SpriteBatch batch;
    OrthographicCamera worldCamera;
    OrthographicCamera textCamera;
    Skin skin;
    TextButton back;
    Stage stage;
    float buttonHeight;
    Label programmerTitle;
    Label programmer1;
    Label programmer2;
    Label scrumTitle;
    Label scrum;
    Label graphicsTitle;
    Label graphics;
    Label musicTitle;
    Label music;
    I18NBundle aboutBundle;

    private Options options;

    /**
     * Sets up the information screen
     *
     * @param game  main game class
     */

    public AboutScreen(RaccoonRoll game) {
        this.game = game;
        options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        aboutBundle = I18NBundle.createBundle(Gdx.files.internal("localization/AboutBundle"), options.getLocale());
    }

    @Override
    public void show() {
        Table table = new Table();
        table.setFillParent(true);
        Table buttonTable = new Table();
        stage.addActor(table);
        table.setDebug(true);
        if (game.DEBUGGING()) {
            buttonTable.setDebug(true);
        }

        createSkin();
        back = new TextButton(aboutBundle.get("backButton"), skin);
        createLabels();

        float padding = game.scaleFromFHD(50);
        table.row().padTop(padding);
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

        buttonTable.row();
        buttonTable.padRight(padding * 26);
        buttonHeight = game.scaleFromFHD(200f);
        table.add(buttonTable);
        buttonTable.add(back).width(Value.percentWidth(0.20f, table)).height(buttonHeight);

        back.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Back", "Button clicked");
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    private void createSkin() {
        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));
    }

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
            Gdx.app.log("Disposed", "AboutScreen");
        }
        // dispose of assets when not needed anymore
        stage.dispose();
    }
}
