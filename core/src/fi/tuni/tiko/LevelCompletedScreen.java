package fi.tuni.tiko;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LevelCompletedScreen extends ApplicationAdapter implements Screen {

    RaccoonRoll game;
    SpriteBatch batch;
    OrthographicCamera worldCamera;
    OrthographicCamera textCamera;
    Skin skin;
    TextButton ok;
    Stage stage;
    float buttonHeight;

    public LevelCompletedScreen(RaccoonRoll game) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {
        Table table = new Table();
        if (game.DEBUGGING()) {
            table.setDebug(true);
        }
        table.setFillParent(true);
        stage.addActor(table);

        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));
        ok = new TextButton("Continue", skin);

        float padding = game.scaleFromFHD(50);
        table.row().pad(padding * 10, 0, 0, 0);
        table.padLeft(padding * 26);
        buttonHeight = game.scaleFromFHD(200f);
        table.add(ok).width(Value.percentWidth(0.25f, table)).height(buttonHeight);

        ok.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Continue", "Button clicked");
                game.setScreen(new MenuScreen(game));
            }
        });
    }

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
        // dispose of assets when not needed anymore
        stage.dispose();
    }
}
