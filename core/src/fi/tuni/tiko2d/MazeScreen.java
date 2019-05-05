package fi.tuni.tiko2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;

/**
 * Screen for displaying mazes
 *
 * @author Heikki Kangas
 */
public class MazeScreen implements Screen {
    private RaccoonRoll game;
    private SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;

    private Skin skin;
    // Hud
    private Stage hud;
    private Label timeSpentLabel;
    private String timeSpentText;
    private Label objectsLeftLabel;
    private String objectsLeftText;
    private TextButton pauseButton;
    // Pause menu
    private Stage pauseMenu;
    private Table pauseTable;
    private TextButton mainMenuButton;
    private TextButton optionsButton;
    private TextButton mapButton;
    private TextButton continueButton;
    private Label pausedLabel;

    private InputMultiplexer multiplexer;

    // Sounds
    private ArrayList<Sound> wallHitSounds;
    private Sound badSound;
    private Sound goodSound;
    private Sound victorySound;
    private Music backgroundMusic;

    // TiledMap
    private TiledMapUtil tiledMapUtil;
    private float tiledMapHeight;
    private float tiledMapWidth;
    private float tileSize = 64f;
    private int goodObjectsRemaining;
    private ArrayList<Rectangle> goodObjectRectangles;
    private ArrayList<Rectangle> badObjectRectangles;
    private Rectangle goalRectangle;
    private Body goalBlock;
    private Player player;
    private boolean goalReached;
    private ArrayList<Rectangle> rectanglesToRemove;

    private I18NBundle mazeBundle;
    private Options options;

    private float timeSpent;

    private long levelFinishedTime;
    private final long levelCompletedScreenDelay = 1000;

    private boolean paused;

    private String levelName;

    private MazeScreen mazeScreen;

    private AssetManager assetManager;

    /**
     * Sets up the selected maze
     *
     * @param game      main game class
     * @param levelName name of the level that will be loaded and shown
     */
    public MazeScreen(RaccoonRoll game, String levelName) {
        this.levelName = levelName;
        this.game = game;
        mazeScreen = this;
        assetManager = game.getAssetManager();
        options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        tiledMapUtil = game.getTiledMapUtil();

        world = new World(new Vector2(0, 0), true);
        player = new Player(game);

        skin = assetManager.get("uiskin/comic-ui.json");

        debugRenderer = new Box2DDebugRenderer();

        mazeBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MazeBundle"), options.getLocale());

        loadTileMap(levelName);
        loadSounds();
        loadBackgroundMusic();

        player.createPlayerBody(world, tiledMapUtil.getPlayerStartPos(tiledMap));
        goodObjectRectangles = tiledMapUtil.getGoodRectangles(tiledMap);
        goodObjectsRemaining = goodObjectRectangles.size();
        badObjectRectangles = tiledMapUtil.getBadRectangles(tiledMap);
        goalRectangle = tiledMapUtil.getGoalRectangle(tiledMap);

        createHud();
        createPauseMenu();
        tiledMapUtil.createWalls(tiledMapUtil.getWallRectangles(tiledMap), world);
        goalBlock = tiledMapUtil.createGoalBlockBody(tiledMap, world);
        addContactListener();

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud);
        Gdx.input.setInputProcessor(multiplexer);
        Gdx.input.setCatchBackKey(true);

        rectanglesToRemove = new ArrayList<Rectangle>();
    }

    /**
     * Add contact listener for playing sounds when player hits a wall
     */
    private void addContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                wallHitSounds.get(MathUtils.random(wallHitSounds.size() - 1)).play(options.getEffectsVolume());
            }

            @Override
            public void endContact(Contact contact) {

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
    }

    /**
     * Loads sound effects
     */
    private void loadSounds() {
        wallHitSounds = new ArrayList<Sound>();
        for (int i = 1; i < 6; i++) {
            wallHitSounds.add(assetManager.get(String.format("sounds/wallHit/WALL_HIT_0%d.mp3", i), Sound.class));
        }
        badSound = assetManager.get("sounds/badObject/BAD_01.mp3");
        goodSound = assetManager.get("sounds/goodObject/GOOD_01.mp3");
        victorySound = assetManager.get("sounds/victory/VICTORY_01.mp3");
    }

    /**
     * Loads levels background music, sets looping and volume and starts playing the music.
     */
    private void loadBackgroundMusic() {
        backgroundMusic = assetManager.get("sounds/backgroundMusic/maze.mp3");
        if (!backgroundMusic.isPlaying()) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(options.getMusicVolume());
            backgroundMusic.play();
        }
    }

    /**
     * Loads tilemap, sets up TiledMapRenderer and TiledMap dimensions in meters
     *
     * @param levelName Name of the level to be loaded
     */
    private void loadTileMap(String levelName) {
        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.textureMinFilter = Texture.TextureFilter.Nearest;
        parameters.textureMagFilter = Texture.TextureFilter.Nearest;
        tiledMap = new TmxMapLoader().load("tilemaps/" + levelName + "/maze.tmx", parameters);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, game.getScale());

        MapProperties mapProps = tiledMap.getProperties();

        // TiledMap dimensions in meters
        tiledMapWidth = mapProps.get("width", Integer.class) * tileSize * game.getScale();
        tiledMapHeight = mapProps.get("height", Integer.class) * tileSize * game.getScale();

        tiledMapUtil.hideGoal(tiledMap);
    }


    /**
     * Creates the pause menu with buttons to main menu, map, options and unpausing
     */
    private void createPauseMenu() {
        float padding = game.scaleHorizontal(25);
        float buttonHeight = game.scaleVertical(175f);
        pauseMenu = new Stage(new ScreenViewport(), batch);
        pauseTable = new Table(skin);
        pauseTable.setBackground("text-field");
        pauseMenu.addActor(pauseTable);

        pausedLabel = new Label(mazeBundle.get("paused"), skin);
        mainMenuButton = new TextButton(mazeBundle.get("mainMenu"), skin);
        optionsButton = new TextButton(mazeBundle.get("options"), skin);
        mapButton = new TextButton(mazeBundle.get("map"), skin);
        continueButton = new TextButton(mazeBundle.get("continue"), skin);

        pauseTable.add(pausedLabel);
        pauseTable.row();
        pauseTable.add(mainMenuButton).padTop(padding).height(buttonHeight);
        pauseTable.row();
        pauseTable.add(mapButton).padTop(padding).height(buttonHeight).uniformX().fillX();
        pauseTable.row();
        pauseTable.add(optionsButton).padTop(padding).height(buttonHeight).uniformX().fillX();
        pauseTable.row();
        pauseTable.add(continueButton).padTop(padding).height(buttonHeight).uniformX().fillX();

        pauseTable.pack();

        pauseTable.setPosition(
                Gdx.graphics.getWidth() / 2 - pauseTable.getWidth() / 2,
                Gdx.graphics.getHeight() / 2 - pauseTable.getHeight() / 2);
        addPauseMenuListeners();
    }

    /**
     * Adds listeners for pause menu buttons
     */
    private void addPauseMenuListeners() {
        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
                backgroundMusic.stop();
                dispose();
            }
        });

        mapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MapScreen(game));
                backgroundMusic.stop();
                dispose();
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new OptionsScreen(game, mazeScreen));
                backgroundMusic.pause();
            }
        });

        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                paused = false;
                multiplexer.removeProcessor(pauseMenu);
                player.setPaused(paused);
            }
        });
    }

    /**
     * Creates the hud that shows spent time, good objects left and pause button
     */
    private void createHud() {
        hud = new Stage(new ScreenViewport(), batch);
        float verticalPad = game.scaleVertical(10);
        float horizontalPad = game.scaleHorizontal(10);

        objectsLeftText = mazeBundle.get("goodObjectsRemaining");
        timeSpentText = mazeBundle.get("time");

        objectsLeftLabel = new Label(objectsLeftText + goodObjectsRemaining, skin, "small-white");
        timeSpentLabel = new Label(timeSpentText + game.formatTime(timeSpent), skin, "small-white");
        pauseButton = new TextButton(mazeBundle.get("pauseButton"), skin);

        Table table = new Table();
        table.setFillParent(true);
        hud.addActor(table);

        if (game.DEBUGGING()) {
            table.setDebug(true);
        }

        table.top().padTop(verticalPad);
        table.add(timeSpentLabel).left().top().padLeft(horizontalPad).width(Gdx.graphics.getWidth() / 6f);
        table.add(objectsLeftLabel).top().expandX();
        table.add(pauseButton).right().top().padRight(horizontalPad).height(game.scaleHorizontal(150));

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (paused) {
                    paused = false;
                    multiplexer.removeProcessor(pauseMenu);
                } else {
                    paused = true;
                    multiplexer.addProcessor(pauseMenu);
                }
                player.setPaused(paused);
            }
        });
    }

    /**
     * Clears the screen with black color
     */
    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }



    /**
     * Should be called when window resizes but doesn't
     *
     * @param width  not used
     * @param height not used
     */
    @Override
    public void resize(int width, int height) {
        if (game.DEBUGGING()) {
            Gdx.app.log("Resize", "happened");
        }
    }

    /**
     * Moves player, checks if player overlaps with objects, updates camera position and draws
     * tilemap and player
     *
     * @param delta time since last frame was drawn
     */
    @Override
    public void render(float delta) {
        if (!paused) {
            if (!goalReached) {
                timeSpent += delta;
            }

            player.movePlayer(delta);
            updateCameraPosition();
            checkGoodObjectOverlaps();
            checkBadObjectOverlaps();
            updateObjectsLeftLabel();
            updateTimeSpentLabel();

            if (goodObjectsRemaining == 0) {
                checkGoalOverlap();
            }
        }
        clearScreen();
        tiledMapRenderer.setView(worldCamera);
        tiledMapRenderer.render();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        player.draw(batch, delta);
        batch.end();

        if (paused) {
            pauseMenu.draw();
        }
        hud.draw();

        if (game.DEBUGGING()) {
            debugRenderer.render(world, worldCamera.combined);
            MemoryDebug.memoryUsed(delta);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            if (paused) {
                paused = false;
                multiplexer.removeProcessor(pauseMenu);
            } else {
                paused = true;
                multiplexer.addProcessor(pauseMenu);
            }
            player.setPaused(paused);
        }

        if (!paused) {
            tiledMapUtil.stepWorld(delta, world);
        }

        if (goalReached && System.currentTimeMillis() >= levelFinishedTime + levelCompletedScreenDelay) {
            game.getCompletedLevels().putBoolean(levelName, true);
            game.getCompletedLevels().flush();
            game.setScreen(new LevelCompletedScreen(game, timeSpent, levelName));
            backgroundMusic.stop();
            dispose();
        }
    }

    /**
     * Updates camera's position so there's no black background color shown outside tilemap
     */
    private void updateCameraPosition() {
        Vector2 playerPos = player.getPosition();
        float playerX = playerPos.x;
        float playerY = playerPos.y;
        float worldWidth = game.getWORLD_WIDTH();
        float worldHeight = game.getWORLD_HEIGHT();
        float minX = worldWidth / 2f;
        float maxX = tiledMapWidth - worldWidth / 2f;
        float minY = worldHeight / 2f;
        float maxY = tiledMapHeight - worldHeight / 2f;

        if (playerX <= minX) {
            worldCamera.position.x = minX;
        } else if (playerX >= maxX) {
            worldCamera.position.x = maxX;
        } else {
            worldCamera.position.x = playerX;
        }

        if (playerY <= minY) {
            worldCamera.position.y = minY;
        } else if (playerY >= maxY) {
            worldCamera.position.y = maxY;
        } else {
            worldCamera.position.y = playerY;
        }
        worldCamera.update();
    }

    /**
     * Checks if player overlaps with any good objects
     */
    private void checkGoodObjectOverlaps() {
        Vector2 playerPos = player.getPosition();
        Circle playerCircle = new Circle(playerPos.x, playerPos.y, player.getBodyRadius());

        for (Rectangle rectangle : goodObjectRectangles) {
            if (Intersector.overlaps(playerCircle, rectangle)) {
                rectanglesToRemove.add(rectangle);
                tiledMapUtil.clearTile(
                        (TiledMapTileLayer) tiledMap.getLayers().get("good_tiles"),
                        tiledMapUtil.getRectangleTileIndex(rectangle, tileSize)
                );
                goodSound.play(options.getEffectsVolume());
                if (goodObjectsRemaining == 1) {
                    tiledMapUtil.showGoal(tiledMap, world, goalBlock);
                }
            }
        }
        goodObjectRectangles.removeAll(rectanglesToRemove);
        goodObjectsRemaining = goodObjectRectangles.size();
        rectanglesToRemove.clear();
    }

    /**
     * Checks if player overlaps with goal
     */
    private void checkGoalOverlap() {
        Vector2 playerPos = player.getPosition();
        Circle playerCircle = new Circle(playerPos.x, playerPos.y, player.getBodyRadius());

        if (Intersector.overlaps(playerCircle, goalRectangle) && !goalReached) {
            player.setGoalReached();
            goalReached = true;
            victorySound.play(options.getEffectsVolume());
            levelFinishedTime = System.currentTimeMillis();
        }
    }

    /**
     * Checks if player overlaps with any bad objects
     */
    private void checkBadObjectOverlaps() {
        Vector2 playerPos = player.getPosition();
        Circle playerCircle = new Circle(playerPos.x, playerPos.y, player.getBodyRadius());

        for (Rectangle rectangle : badObjectRectangles) {
            if (Intersector.overlaps(playerCircle, rectangle)) {
                rectanglesToRemove.add(rectangle);
                tiledMapUtil.clearTile(
                        (TiledMapTileLayer) tiledMap.getLayers().get("bad_tiles"),
                        tiledMapUtil.getRectangleTileIndex(rectangle, tileSize)
                );
                player.applyDebuff();
                if (game.DEBUGGING()) {
                    Gdx.app.log("Debuff", "applied");
                }
                timeSpent += 10;
                badSound.play(options.getEffectsVolume());
            }
        }
        badObjectRectangles.removeAll(rectanglesToRemove);
        rectanglesToRemove.clear();
    }



    @Override
    public void show() {
        if (paused) {
            updateLanguage();
            Gdx.input.setInputProcessor(multiplexer);
        }
        if (!backgroundMusic.isPlaying()) {
            backgroundMusic.setVolume(options.getMusicVolume());
            backgroundMusic.play();
        }
    }

    @Override
    public void pause() {
        paused = true;
        player.setPaused(true);
        multiplexer.addProcessor(pauseMenu);
    }

    @Override
    public void resume() {}

    @Override
    public void hide() {
    }

    /**
     * Disposes no longer needed assets and stops the background music playing
     */
    @Override
    public void dispose() {
        if (game.DEBUGGING()) {
            Gdx.app.log("Starting dispose", "MazeScreen");
        }
        backgroundMusic.stop();
        hud.dispose();
        pauseMenu.dispose();

        tiledMap.dispose();
        world.dispose();
        debugRenderer.dispose();
        if (game.DEBUGGING()) {
            Gdx.app.log("Finished dispose", "MazeScreen");
        }
    }

    /**
     * Updates the hud label showing time spent
     */
    private void updateTimeSpentLabel() {
        timeSpentLabel.setText(timeSpentText + game.formatTime(timeSpent));
    }

    /**
     * Updates the hud label showing good objects left
     */
    private void updateObjectsLeftLabel() {
        objectsLeftLabel.setText(objectsLeftText + goodObjectsRemaining);
    }

    /**
     * Updates the used language when player returns from options to game
     */
    private void updateLanguage() {
        mazeBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MazeBundle"), options.getLocale());
        updateHudTexts();
        updatePauseMenuTexts();
    }

    /**
     * Updates pause menu texts to use currently selected language
     */
    private void updatePauseMenuTexts() {
        pausedLabel.setText(mazeBundle.get("paused"));
        mainMenuButton.setText(mazeBundle.get("mainMenu"));
        optionsButton.setText(mazeBundle.get("options"));
        mapButton.setText(mazeBundle.get("map"));
        continueButton.setText(mazeBundle.get("continue"));

        pauseTable.pack();

        pauseTable.setPosition(
                Gdx.graphics.getWidth() / 2 - pauseTable.getWidth() / 2,
                Gdx.graphics.getHeight() / 2 - pauseTable.getHeight() / 2);
    }

    /**
     * Updates hud texts to use currently selected language
     */
    private void updateHudTexts() {
        objectsLeftText = mazeBundle.get("goodObjectsRemaining");
        timeSpentText = mazeBundle.get("time");
        updateObjectsLeftLabel();
        updateTimeSpentLabel();
        pauseButton.setText(mazeBundle.get("pauseButton"));
    }
}
