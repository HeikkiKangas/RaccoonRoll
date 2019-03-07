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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.FitViewport;
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
    Label title;
    float buttonHeight;

    /*
    old json:
    com.badlogic.gdx.graphics.g2d.BitmapFont: {
        button: {
            file: font-button-export.fnt
        }
        font: {
            file: font-export.fnt
        }
        title: {
            file: font-title-export.fnt
        }
    }
     */

    public MenuScreen(RaccoonRoll game) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        //for testing purposes
        // Locale could be moved to RaccoonRoll class?
        //locale = new Locale("fi", "FI");
        locale = Locale.getDefault();
        menuBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MenuBundle"), locale);
    }

    @Override
    public void show() {
        Table table = new Table();
        table.setFillParent(true);
        // abother table for buttons
        Table buttonTable = new Table();
        //gives me the grid
        //table.setDebug(true);
        //buttonTable.setDebug(true);
        stage.addActor(table);

        //skin = new Skin (Gdx.files.internal("uiskin/comic-ui.json"));

        skin = new Skin();

        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        /*
        korvataan jsonista poistetut fontit RaccoonRollissa generoiduilla
        fonttien koko vaihdettavissa RaccoonRoll generateFonts() metodissa
        */
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        // ladataan erikseen json koska konstruktorilla ladatessa valittaisi puuttuvista fonteista
        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));

        /*
            Label käyttää oletuksena pientä perusfonttia joka on tekstiä varten
            stylename titlen kanssa isompaa title fonttia
         */
        title = new Label(menuBundle.get("title"), skin, "title");
        play = new TextButton(menuBundle.get("playButton"), skin);
        options = new TextButton(menuBundle.get("optionsButton"), skin);
        about = new TextButton(menuBundle.get("aboutButton"), skin);

        //fill ja uniform laittaa muotoonsa
        table.add(title);
        table.row().pad(75, 0, 0, 0);

        // lisätään buttonTable tableen jotta napit eivät olisi titlen levyiset
        table.add(buttonTable);

        // nappien koon asetus, muuta lukua 200 tarpeen mukaan
        buttonHeight = Gdx.graphics.getHeight() / 1080f * 200;

        /*
        .width(Value.percentWidth(0.5f, table) = puolet parent tablen leveydestä
        lisäillään napit ja paddingit buttoonTableen ihan samallailla kun ennen tableen
        */
        buttonTable.add(play).width(Value.percentWidth(0.5f, table)).height(buttonHeight);
        buttonTable.row().padTop(25);
        buttonTable.add(options).uniformX().fillX().height(buttonHeight);
        buttonTable.row().padTop(25);
        buttonTable.add(about).uniformX().fillX().height(buttonHeight);

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
                Gdx.app.log("About", "Should be here");
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
