package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HighscoreScreen extends ApplicationAdapter implements Screen {

    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private OrthographicCamera textCamera;
    private Skin skin;
    private TextButton back;
    private Stage stage;
    private float buttonHeight;
    private float bgWidth;
    private float bgHeight;
    private Texture background;
    private I18NBundle aboutBundle;
    private Options options;

    public HighscoreScreen(RaccoonRoll game) {
        this.game = game;
        options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        background = new Texture("graphics/othermenus/Tausta75.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        aboutBundle = I18NBundle.createBundle(Gdx.files.internal("localization/AboutBundle"), options.getLocale());

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
        back = new TextButton("Back", skin);
        //make a bundle and change

        float padding = game.scaleFromFHD(50);
        table.row().padTop(padding);
        table.add(back).width(Value.percentWidth(0.20f, table)).height(buttonHeight);
        table.row();
    }

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
    }

    @Override
    public void resize(int width, int height) {
        // change the stage's viewport when the screen size is changed
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {

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

    @Override
    public void dispose() {
        // dispose of assets when not needed anymore
        stage.dispose();
        background.dispose();
        skin.dispose();
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "AboutScreen");
        }
    }
}
