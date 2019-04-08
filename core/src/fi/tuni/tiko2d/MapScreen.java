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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Screen for displaying a map with all levels
 *
 * @author
 */

public class MapScreen extends ApplicationAdapter implements Screen {

    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera textCamera;
    private Stage buttonStage;
    private Stage levelSelect;
    private Skin skin;
    private Texture map1;
    private Texture map2;
    private float bgHeight;
    private float bgWidth;
    private InputMultiplexer multiplexer;
    private GestureDetector mapScroller;
    private boolean showLevelSelect;

    private ArrayList<Country> levels;
    private I18NBundle mapBundle;

    private Group buttons;

    /*
    private Texture notStarted;
    private Texture started;
    private Texture done;
    */

    private float bgX;

    public MapScreen(RaccoonRoll game) {
        this.game = game;
        mapBundle = I18NBundle.createBundle(
                Gdx.files.internal("localization/MapBundle"),
                game.getOptions().getLocale());

        levels = new ArrayList<Country>();
        levels.add(new Country("uk", new String[]{"london", "manchester"}, 1300, 325));
        levels.add(new Country("fr", new String[]{"paris", "marseille"}, 1300, 200));
        levels.add(new Country("eg", new String[]{"kairo", "alexandria"}, 1375, 10));
        levels.add(new Country("us", new String[]{"newyork", "philadelphia"}, 400, 100));
        levels.add(new Country("ch", new String[]{"peking", "shanghai"}, 2600, 25));

        batch = game.getBatch();
        textCamera = game.getTextCamera();
        map1 = new Texture("graphics/worldmap/map1.png");
        map2 = new Texture("graphics/worldmap/map2.png");

        /*
        notStarted = new Texture("graphics/worldmap/Nappipun.png");
        started = new Texture("graphics/worldmap/Nappikelt.png");
        done = new Texture("graphics/worldmap/Nappivih.png");
        */

        buttonStage = new Stage(new ScreenViewport());

        multiplexer = new InputMultiplexer();
        mapScroller = new GestureDetector(new MapScroller());
        multiplexer.addProcessor(buttonStage);
        multiplexer.addProcessor(mapScroller);

        bgHeight = game.scaleVertical(map1.getHeight());
        bgWidth = game.scaleVertical(map1.getWidth());

        Gdx.input.setInputProcessor(multiplexer);
        Gdx.input.setCatchBackKey(true);

        createButtons();
        bgX = game.scaleVertical(-600);

        createSkin();
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

        //buttonStage.act(Gdx.graphics.getDeltaTime());
        buttonStage.draw();
        if (showLevelSelect) {
            levelSelect.draw();
        }

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

    private void generateLevelSelector(Country selectedCountry) {
        final Country country = selectedCountry;
        float padding = game.scaleVertical(50);
        boolean firstLevelCompleted = game.getCompletedLevels().getBoolean(country.levels[0], false);
        levelSelect = new Stage(new ScreenViewport());
        Table table = new Table(skin);
        Table levelButtonTable = new Table();

        if (game.DEBUGGING()) {
            table.setDebug(true);
        }

        Label countryName = new Label(country.countryName, skin);

        TextButton levelButton1 = new TextButton(country.levelNames[0], skin);
        levelButton1.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log(country.levelNames[0], "Clicked");
                game.setScreen(new MazeScreen(game, country.levels[0]));
                dispose();
            }
        });

        TextButton levelButton2 = new TextButton("", skin);
        if (firstLevelCompleted) {
            levelButton2.setText(country.levelNames[1]);
            levelButton2.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log(country.levelNames[1], "Clicked");
                    game.setScreen(new MazeScreen(game, country.levels[1]));
                    dispose();
                }
            });
        }

        TextButton closeButton = new TextButton(mapBundle.get("close"), skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Close", "Clicked");
                showLevelSelect = false;
                multiplexer.removeProcessor(levelSelect);
                multiplexer.addProcessor(buttonStage);
                multiplexer.addProcessor(mapScroller);
                levelSelect.dispose();
            }
        });

        levelButtonTable.add(levelButton1).padTop(padding).uniformX().fillX();
        levelButtonTable.row();
        // Placeholder time
        levelButtonTable.add(new Label("1:32", skin));
        if (firstLevelCompleted) {
            levelButtonTable.row();
            levelButtonTable.add(levelButton2).padTop(padding).fillX();
            levelButtonTable.row();
            // Placeholder time
            levelButtonTable.add(new Label("2:45", skin));
        }

        table.add(countryName);
        table.row();
        table.add(levelButtonTable);
        table.row();
        table.add(closeButton).padTop(padding);

        levelSelect.addActor(table);

        table.setBackground("text-field");
        //table.setFillParent(true);
        table.pack();
        table.setPosition(
                Gdx.graphics.getWidth() / 2 - table.getWidth() / 2,
                Gdx.graphics.getHeight() / 2 - table.getHeight() / 2);
    }

    private void createSkin() {
        skin = new Skin();

    }

    private void createButtons() {
        buttons = new Group();
        buttonStage.addActor(buttons);

        for (Country entry : levels) {
            final Country country = entry;
            Group countryButtons = new Group();
            Texture texture = new Texture(
                    String.format("graphics/worldmap/buttons/%s.png", country.countryCode)
            );
            /*
            boolean firstLevelCompleted = game.getCompletedLevels().getBoolean(entry.levels[0], false);
            boolean secondLevelCompleted = game.getCompletedLevels().getBoolean(entry.levels[1], false);
            */

            boolean addNextButton = true;
            /*
            if (firstLevelCompleted && secondLevelCompleted) {
                addNextButton = true;
            }
            */

            float x = game.scaleVertical(entry.buttonX);
            float y = game.scaleVertical(entry.buttonY);

            /*
            Texture texture;
            if (firstLevelCompleted && secondLevelCompleted) {
                addNextButton = true;
                texture = done;
            } else if (firstLevelCompleted && !secondLevelCompleted) {
                texture = started;
            } else {
                texture = notStarted;
            }
            */

            //selectedTexture = new Texture("graphics/worldmap/buttons/uk2.png");

            ImageButton btn1 = new ImageButton(new TextureRegionDrawable(texture));
            ImageButton btn2 = new ImageButton(new TextureRegionDrawable(texture));
            ImageButton btn3 = new ImageButton(new TextureRegionDrawable(texture));
            /*
            btn1.setRound(true);
            btn2.setRound(true);
            btn3.setRound(true);
            */
            btn1.setTransform(true);
            btn2.setTransform(true);
            btn3.setTransform(true);
            btn1.setScale(game.scaleVertical(1));
            btn2.setScale(game.scaleVertical(1));
            btn3.setScale(game.scaleVertical(1));
            btn1.setPosition(x, y);
            btn2.setPosition(x + bgWidth * 2, y);
            btn3.setPosition(x - bgWidth * 2, y);

            countryButtons.addActor(btn1);
            countryButtons.addActor(btn2);
            countryButtons.addActor(btn3);

            countryButtons.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Gdx.app.log(country.countryName, "Clicked");
                    multiplexer.removeProcessor(buttonStage);
                    multiplexer.removeProcessor(mapScroller);
                    generateLevelSelector(country);
                    multiplexer.addProcessor(levelSelect);
                    showLevelSelect = true;
                }
            });

            buttons.addActor(countryButtons);

            if (!addNextButton) {
                break;
            }
        }
    }

    @Override
    public void dispose() {
        /*
        started.dispose();
        notStarted.dispose();
        done.dispose();
        */
        map1.dispose();
        map2.dispose();
        buttonStage.dispose();
        levelSelect.dispose();
        if (game.DEBUGGING()) {
            Gdx.app.log("MapScreen", "Disposed");
        }
    }

    private class Country {
        public String countryCode;
        public String countryName;
        public String[] levels;
        public String[] levelNames;
        public float buttonX;
        public float buttonY;

        public Country(String country, String[] levels, float buttonX, float buttonY) {
            Locale locale = game.getOptions().getLocale();
            this.countryCode = country;
            countryName = mapBundle.get(countryCode);
            this.levels = levels;
            this.buttonX = buttonX;
            this.buttonY = buttonY;
            this.levelNames = new String[]{mapBundle.get(levels[0]), mapBundle.get(levels[1])};
        }
    }
}