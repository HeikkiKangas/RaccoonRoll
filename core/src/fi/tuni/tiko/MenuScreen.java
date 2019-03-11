package fi.tuni.tiko;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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

import java.util.Locale;

public class MenuScreen extends ApplicationAdapter implements Screen {
    RaccoonRoll game;
    SpriteBatch batch;
    OrthographicCamera worldCamera;
    OrthographicCamera textCamera;
    Skin skin;
    TextButton play;
    TextButton options;
    TextButton about;
    Stage stage;
    Locale locale;
    I18NBundle menuBundle;
    //Label title;
    float buttonHeight;
    float titleWidth;
    float titleHeight;
    Texture title;

    public MenuScreen(RaccoonRoll game) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        title = new Texture("graphics/mainmenu/Logo1.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        //for testing purposes
        // Locale could be moved to RaccoonRoll class to save a bit of memory
        //locale = new Locale("fi", "FI");
        locale = Locale.getDefault();
        menuBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MenuBundle"), locale);
        titleWidth = game.scaleFromFHD(title.getWidth());
        titleHeight = game.scaleFromFHD(title.getHeight());
    }

    @Override
    public void show() {
        Table table = new Table();
        table.setFillParent(true);
        //gives me the grid
        //table.setDebug(true);
        stage.addActor(table);

        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));

        //title = new Label(menuBundle.get("title"), skin, "title");
        play = new TextButton(menuBundle.get("playButton"), skin);
        options = new TextButton(menuBundle.get("optionsButton"), skin);
        about = new TextButton(menuBundle.get("aboutButton"), skin);

        //table.add(title);

        // table's top and right padding size scaled
        float padding = game.scaleFromFHD(300);
        table.row().pad(padding, 0, 0, 0);
        table.right();
        table.padRight(padding);
        //buttonHeight = Gdx.graphics.getHeight() / 1080f * 200;
        buttonHeight = game.scaleFromFHD(200f);
        //fill ja uniform laittaa muotoonsa
        float scaledButtonPadding = game.scaleFromFHD(25f);
        table.add(play).width(Value.percentWidth(0.25f, table)).height(buttonHeight);
        table.row().padTop(scaledButtonPadding);
        table.add(options).uniformX().fillX().height(buttonHeight);
        table.row().padTop(scaledButtonPadding);
        table.add(about).uniformX().fillX().height(buttonHeight);


        play.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Play", "Button clicked");
                game.setScreen(new MazeScreen(game, "tutorial"));
            }
        });

        options.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Options", "Should be here");
            }
        });

        about.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("About", "Button clicked");
                game.setScreen(new AboutScreen(game));
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        batch.setProjectionMatrix(textCamera.combined);
        batch.begin();
        batch.draw(title, Gdx.graphics.getWidth() / 2 - titleWidth / 2, Gdx.graphics.getHeight() - titleHeight, titleWidth, titleHeight);
        batch.end();
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
