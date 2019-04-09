package fi.tuni.tiko2d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
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
    private Stage tutorialStage;
    private Skin skin;
    private Texture map1;
    private Texture map2;
    private float bgHeight;
    private float bgWidth;
    private InputMultiplexer multiplexer;
    private GestureDetector mapScroller;
    private boolean showLevelSelect;
    private Preferences highScores;

    private ArrayList<Country> levels;
    private I18NBundle mapBundle;

    private Group buttons;

    private AssetManager assetManager;
    private Music backgroundMusic;

    /*
    private Texture notStarted;
    private Texture started;
    private Texture done;
    */

    private float bgX;

    public MapScreen(RaccoonRoll game) {
        this.game = game;
        highScores = game.getHighScores();
        assetManager = game.getAssetManager();
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
        map1 = assetManager.get("graphics/worldmap/map1.png");
        map2 = assetManager.get("graphics/worldmap/map2.png");

        backgroundMusic = assetManager.get("sounds/backgroundMusic/main_menu_loop.mp3");

        /*
        notStarted = new Texture("graphics/worldmap/Nappipun.png");
        started = new Texture("graphics/worldmap/Nappikelt.png");
        done = new Texture("graphics/worldmap/Nappivih.png");
        */

        buttonStage = new Stage(new ScreenViewport(), batch);
        tutorialStage = new Stage(new ScreenViewport(), batch);

        multiplexer = new InputMultiplexer();
        mapScroller = new GestureDetector(new MapScroller());
        multiplexer.addProcessor(buttonStage);
        multiplexer.addProcessor(tutorialStage);
        multiplexer.addProcessor(mapScroller);

        bgHeight = game.scaleVertical(map1.getHeight());
        bgWidth = game.scaleVertical(map1.getWidth());

        Gdx.input.setInputProcessor(multiplexer);
        Gdx.input.setCatchBackKey(true);

        bgX = game.scaleVertical(-600);

        createSkin();
        createButtons();
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
        tutorialStage.draw();
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
        levelSelect = new Stage(new ScreenViewport(), batch);
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
                backgroundMusic.stop();
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
                    backgroundMusic.stop();
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

        Label levelTimeLabel1 = new Label("", skin);
        float levelTime1 = highScores.getFloat(country.levels[0], 0);
        if (levelTime1 != 0f) {
            levelTimeLabel1.setText(mapBundle.get("bestTime") + game.formatTime(levelTime1));
        } else {
            levelTimeLabel1.setText(mapBundle.get("notPlayedYet"));
        }
        levelButtonTable.add(levelTimeLabel1);

        if (firstLevelCompleted) {
            levelButtonTable.row();
            levelButtonTable.add(levelButton2).padTop(padding).fillX();
            levelButtonTable.row();

            Label levelTimeLabel2 = new Label("", skin);
            float levelTime2 = highScores.getFloat(country.levels[1], 0);
            if (levelTime2 != 0f) {
                levelTimeLabel2.setText(mapBundle.get("bestTime") + game.formatTime(levelTime2));
            } else {
                levelTimeLabel2.setText(mapBundle.get("notPlayedYet"));
            }
            levelButtonTable.add(levelTimeLabel2);
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
        skin = assetManager.get("uiskin/comic-ui.json");

    }

    private void createButtons() {
        buttons = new Group();
        buttonStage.addActor(buttons);
        TextButton tutorialButton = new TextButton(mapBundle.get("tutorialButton"), skin);
        Table tutorialTable = new Table();
        tutorialTable.setFillParent(true);
        tutorialTable.top().right();
        tutorialTable.add(tutorialButton).pad(
                game.scaleVertical(10),
                0,0, game.scaleHorizontal(10)
        );
        tutorialStage.addActor(tutorialTable);
        tutorialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("TutorialButton", "Clicked");
                game.setScreen(new TutorialScreen(game));
                backgroundMusic.stop();
                dispose();
            }
        });

        for (Country entry : levels) {
            final Country country = entry;
            Group countryButtons = new Group();
            Texture texture = assetManager.get(
                    String.format("graphics/worldmap/buttons/%s.png", country.countryCode)
            );

            //boolean addNextButton = game.getCompletedLevels().getBoolean(country.levels[1], false);

            boolean addNextButton = true;

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
        /*
        map1.dispose();
        map2.dispose();
        */
        buttonStage.dispose();
        tutorialStage.dispose();
        if (levelSelect != null) {
            levelSelect.dispose();
        }
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