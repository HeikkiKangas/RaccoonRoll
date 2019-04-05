package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;

/**
 * Screen for displaying a map with all levels
 *
 * @author
 */

public class MapScreen extends ApplicationAdapter implements Screen {

    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private OrthographicCamera textCamera;
    private Stage stage;
    private Texture map1;
    private Texture map2;
    private float bgHeight;
    private float bgWidth;
    private InputMultiplexer multiplexer;

    private ArrayList<Country> levels;

    private Group buttons;

    private Texture notStarted;
    private Texture started;
    private Texture done;

    private float bgX;

    public MapScreen(RaccoonRoll game) {
        levels = new ArrayList<Country>();
        levels.add(new Country("United Kingdom", new String[]{"London", "Manchester"}, 1300, 325, "uk.png"));
        levels.add(new Country("France", new String[]{"Paris", "Marseille"}, 1300, 200, "france.png"));
        levels.add(new Country("Egypt", new String[]{"Kairo", "Alexandria"}, 1375, 10, "egypt.png"));
        levels.add(new Country("United States", new String[]{"New York", "New Jersey"}, 400, 100, "us.png"));
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        map1 = new Texture("graphics/map1.png");
        map2 = new Texture("graphics/map2.png");


        notStarted = new Texture("graphics/worldmap/Nappipun.png");
        started = new Texture("graphics/worldmap/Nappikelt.png");
        done = new Texture("graphics/worldmap/Nappivih.png");

        stage = new Stage(new ScreenViewport(), batch);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(new GestureDetector(new MapScroller()));

        bgHeight = game.scaleVertical(map1.getHeight());
        bgWidth = game.scaleVertical(map1.getWidth());

        Gdx.input.setInputProcessor(multiplexer);
        Gdx.input.setCatchBackKey(true);

        createButtons();
        bgX = game.scaleVertical(-600);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (game.DEBUGGING()) {
            MemoryDebug.memoryUsed(delta);
        }
        Gdx.gl.glClearColor(49f / 255, 36f / 255, 209f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(textCamera.combined);
        batch.begin();
        batch.draw(map1, bgX, 0, bgWidth, bgHeight);
        batch.draw(map2, bgX + bgWidth, 0, bgWidth, bgHeight);


        if (bgX > 0) {
            batch.draw(map1, bgX - bgWidth * 2, 0, bgWidth, bgHeight);
            batch.draw(map2, bgX - bgWidth, 0, bgWidth, bgHeight);
        } else if (bgX < -(bgWidth * 2 - Gdx.graphics.getWidth())) {
            batch.draw(map1, bgX + bgWidth * 2, 0, bgWidth, bgHeight);
            batch.draw(map2, bgX + bgWidth * 3, 0, bgWidth, bgHeight);
        }

        batch.end();

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.setScreen(new MenuScreen(game));
            dispose();
        }
    }

    @Override
    public void hide() {

    }

    class MapScroller extends GestureDetector.GestureAdapter {
        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            if (game.DEBUGGING()) {
                Gdx.app.log("Panning",
                        "\nX: " + x + "\nDeltaX: " + deltaX + "\nbgX: " + bgX);
            }
            bgX += deltaX;
            buttons.moveBy(deltaX, 0);
            Gdx.app.log("buttons X", "" + buttons.getX());
            if (bgX > bgWidth * 2) {
                bgX -= bgWidth * 2;
                buttons.moveBy(-(bgWidth * 2), 0);
                Gdx.app.log("buttons X -", "" + buttons.getX());
            } else if (bgX < -bgWidth * 2) {
                bgX += bgWidth * 2;
                buttons.moveBy(bgWidth * 2, 0);
                Gdx.app.log("buttons X +", "" + buttons.getX());
            }

            return super.pan(x, y, deltaX, deltaY);
        }
    }

    private void createButtons() {
        buttons = new Group();
        stage.addActor(buttons);

        /*
        createUkButtons();

        if (game.getCompletedLevels().getBoolean("manchester", true)) {
            createFranceButtons();
        } else {
            return;
        }

        if (game.getCompletedLevels().getBoolean("marseille", true)) {
            createEgyptButtons();
        } else {
            return;
        }

        if (game.getCompletedLevels().getBoolean("alexandria", false)) {
            // createUsButtons();
        } else {
            return;
        }
        */
        for (Country entry : levels) {
            final String country = entry.country;
            Group countryButtons = new Group();
            //Texture texture = new Texture("graphics/worldmap/buttons/" + entry.flagPath);
            Texture texture;
            boolean firstLevelCompleted = game.getCompletedLevels().getBoolean(entry.levels[0].toLowerCase(), false);
            boolean secondLevelCompleted = game.getCompletedLevels().getBoolean(entry.levels[1].toLowerCase(), false);
            boolean addNextButton = true;

            float x = game.scaleVertical(entry.buttonX);
            float y = game.scaleVertical(entry.buttonY);


            if (firstLevelCompleted && secondLevelCompleted) {
                addNextButton = true;
                texture = done;
            } else if (firstLevelCompleted && !secondLevelCompleted) {
                texture = started;
            } else {
                texture = notStarted;
            }


            //selectedTexture = new Texture("graphics/worldmap/buttons/uk2.png");

            ImageButton btn1 = new ImageButton(new TextureRegionDrawable(texture));
            ImageButton btn2 = new ImageButton(new TextureRegionDrawable(texture));
            btn1.setRound(true);
            btn2.setRound(true);
            btn1.setTransform(true);
            btn2.setTransform(true);
            btn1.setScale(game.scaleVertical(1));
            btn2.setScale(game.scaleVertical(1));
            btn1.setPosition(x, y);
            btn2.setPosition(x - bgWidth * 2, y);

            countryButtons.addActor(btn1);
            countryButtons.addActor(btn2);

            countryButtons.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log(country, "Clicked");
                }
            });

            buttons.addActor(countryButtons);

            if (!addNextButton) {
                break;
            }
        }
    }

    private void createUkButtons() {
        Group ukButtons = new Group();
        Texture selectedTexture;
        boolean londonCompleted = game.getCompletedLevels().getBoolean("london", false);
        boolean manchesterCompleted = game.getCompletedLevels().getBoolean("manchester", false);

        float x = game.scaleVertical(1300);
        float y = game.scaleVertical(325);

        if (londonCompleted && !manchesterCompleted) {
            selectedTexture = started;
        } else if (londonCompleted && manchesterCompleted) {
            selectedTexture = done;
        } else {
            selectedTexture = notStarted;
        }

        ImageButton ukButton1 = new ImageButton(new TextureRegionDrawable(selectedTexture));
        ukButton1.setPosition(x, y);
        ukButton1.setRound(true);
        ukButton1.setTransform(true);
        ukButton1.setScale(game.scaleVertical(1));

        ImageButton ukButton2 = new ImageButton(new TextureRegionDrawable(selectedTexture));
        ukButton2.setPosition(x - bgWidth * 2, y);
        ukButton2.setRound(true);
        ukButton2.setTransform(true);
        ukButton2.setScale(game.scaleVertical(1));

        ukButtons.addActor(ukButton1);
        ukButtons.addActor(ukButton2);

        ukButtons.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dispose();
                game.setScreen(new MazeScreen(game, "london"));
                Gdx.app.log("UK", "Clicked");
            }
        });

        buttons.addActor(ukButtons);
    }

    public void createEgyptButtons() {
        Group egyptButtons = new Group();
        Texture selectedTexture;
        boolean kairoCompleted = game.getCompletedLevels().getBoolean("kairo", false);
        boolean alexandriaCompleted = game.getCompletedLevels().getBoolean("alexandria", false);

        float x = game.scaleVertical(1375);
        float y = game.scaleVertical(10);

        if (kairoCompleted && !alexandriaCompleted) {
            selectedTexture = started;
        } else if (kairoCompleted && alexandriaCompleted) {
            selectedTexture = done;
        } else {
            selectedTexture = notStarted;
        }

        ImageButton egyptButton1 = new ImageButton(new TextureRegionDrawable(selectedTexture));
        egyptButton1.setPosition(x, y);
        egyptButton1.setRound(true);
        egyptButton1.setTransform(true);
        egyptButton1.setScale(game.scaleVertical(1));

        ImageButton egyptButton2 = new ImageButton(new TextureRegionDrawable(selectedTexture));
        egyptButton2.setPosition(x - bgWidth * 2, y);
        egyptButton2.setRound(true);
        egyptButton2.setTransform(true);
        egyptButton2.setScale(game.scaleVertical(1));

        egyptButtons.addActor(egyptButton1);
        egyptButtons.addActor(egyptButton2);

        egyptButtons.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //dispose();
                //game.setScreen(new MazeScreen(game, "kairo"));
                Gdx.app.log("Egypt", "Clicked");
            }
        });

        buttons.addActor(egyptButtons);
    }

    public void createFranceButtons() {
        Group franceButtons = new Group();
        Texture selectedTexture;
        boolean parisCompleted = game.getCompletedLevels().getBoolean("paris", false);
        boolean marseilleCompleted = game.getCompletedLevels().getBoolean("marseille", false);

        float x = game.scaleVertical(1300);
        float y = game.scaleVertical(200);

        if (parisCompleted && !marseilleCompleted) {
            selectedTexture = started;
        } else if (parisCompleted && marseilleCompleted) {
            selectedTexture = done;
        } else {
            selectedTexture = notStarted;
        }

        ImageButton franceButton1 = new ImageButton(new TextureRegionDrawable(selectedTexture));
        franceButton1.setPosition(x, y);
        franceButton1.setRound(true);
        franceButton1.setTransform(true);
        franceButton1.setScale(game.scaleVertical(1));

        ImageButton franceButton2 = new ImageButton(new TextureRegionDrawable(selectedTexture));
        franceButton2.setPosition(x - bgWidth * 2, y);
        franceButton2.setRound(true);
        franceButton2.setTransform(true);
        franceButton2.setScale(game.scaleVertical(1));

        franceButtons.addActor(franceButton1);
        franceButtons.addActor(franceButton2);

        franceButtons.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //dispose();
                //game.setScreen(new MazeScreen(game, "kairo"));
                Gdx.app.log("France", "Clicked");
            }
        });

        buttons.addActor(franceButtons);
    }

    @Override
    public void dispose() {
        started.dispose();
        notStarted.dispose();
        done.dispose();
        map1.dispose();
        map2.dispose();
        stage.dispose();
    }

    private class Country {
        public String country;
        public String[] levels;
        public float buttonX;
        public float buttonY;
        public String flagPath;

        public Country(String country, String[] levels, float buttonX, float buttonY, String flagPath) {
            this.country = country;
            this.levels = levels;
            this.buttonX = buttonX;
            this.buttonY = buttonY;
            this.flagPath = flagPath;
        }
    }
}
