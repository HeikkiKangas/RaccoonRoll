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
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
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
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
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

    private float tiledMapHeight;
    private float tiledMapWidth;
    private float tileSize = 64f;
    private ArrayList<Rectangle> goodObjectRectangles;
    private ArrayList<Rectangle> badObjectRectangles;
    private Rectangle goalRectangle;
    private Body goalBlock;
    private Player player;
    private boolean goalReached;

    private I18NBundle mazeBundle;
    private Options options;

    private int goodObjectsRemaining;
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

        world = new World(new Vector2(0, 0), true);
        player = new Player(game);

        skin = assetManager.get("uiskin/comic-ui.json");

        debugRenderer = new Box2DDebugRenderer();

        mazeBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MazeBundle"), options.getLocale());

        loadTileMap(levelName);
        loadSounds();
        loadBackgroundMusic();

        player.createPlayerBody(world, getPlayerStartPos());
        goodObjectRectangles = getGoodRectangles();
        goodObjectsRemaining = goodObjectRectangles.size();
        badObjectRectangles = getBadRectangles();
        getGoalRectangle();

        createHud();
        createPauseMenu();
        createWalls();
        createGoalBlockBody();
        addContactListener();

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud);
        Gdx.input.setInputProcessor(multiplexer);
        Gdx.input.setCatchBackKey(true);
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
     * @param levelName
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

        hideGoal();
    }

    /**
     * Hides the goal on tilemap
     */
    private void hideGoal() {
        tiledMap.getLayers().get("goal").setVisible(false);
        tiledMap.getLayers().get("goal_ground").setVisible(false);
    }

    /**
     * Reveals the goal on tilemap
     */
    private void showGoal() {
        tiledMap.getLayers().get("goal").setVisible(true);
        tiledMap.getLayers().get("goal_ground").setVisible(true);
        world.destroyBody(goalBlock);
    }

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

        mainMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        });

        mapButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MapScreen(game));
                dispose();
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new OptionsScreen(game, mazeScreen));
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
    }

    private void createHud() {
        hud = new Stage(new ScreenViewport(), batch);

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

        table.top();
        table.add(timeSpentLabel).left().top().padLeft(game.scaleHorizontal(10)).width(Gdx.graphics.getWidth() / 6f);
        table.add(objectsLeftLabel).top().expandX();
        table.add(pauseButton).right().top().padRight(game.scaleHorizontal(10)).padTop(5);

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
     * Gets player starting position from tilemap
     *
     * @return vector of player start position
     */
    private Vector2 getPlayerStartPos() {
        MapLayer startPosLayer = tiledMap.getLayers().get("startpos");
        MapObject startPos = startPosLayer.getObjects().get(0);
        float startPosX = startPos.getProperties().get("x", Float.class) * game.getScale();
        float startPosY = startPos.getProperties().get("y", Float.class) * game.getScale();
        return new Vector2(startPosX, startPosY);
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
            world.step(1 / 60f, 6, 2);
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
        ArrayList<Rectangle> rectanglesToRemove = new ArrayList<Rectangle>();
        Vector2 playerPos = player.getPosition();
        Circle playerCircle = new Circle(playerPos.x, playerPos.y, player.getBodyRadius());

        for (Rectangle rectangle : goodObjectRectangles) {
            if (Intersector.overlaps(playerCircle, rectangle)) {
                rectanglesToRemove.add(rectangle);
                clearTile(
                        (TiledMapTileLayer) tiledMap.getLayers().get("good_tiles"),
                        getRectangleTileIndex(rectangle)
                );
                goodSound.play(options.getEffectsVolume());
                if (goodObjectsRemaining == 1) {
                    showGoal();
                }
            }
        }
        goodObjectRectangles.removeAll(rectanglesToRemove);
        goodObjectsRemaining = goodObjectRectangles.size();
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
        ArrayList<Rectangle> rectanglesToRemove = new ArrayList<Rectangle>();
        Vector2 playerPos = player.getPosition();
        Circle playerCircle = new Circle(playerPos.x, playerPos.y, player.getBodyRadius());

        for (Rectangle rectangle : badObjectRectangles) {
            if (Intersector.overlaps(playerCircle, rectangle)) {
                rectanglesToRemove.add(rectangle);
                clearTile(
                        (TiledMapTileLayer) tiledMap.getLayers().get("bad_tiles"),
                        getRectangleTileIndex(rectangle)
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
    }

    /**
     * Checks given rectangles x and y index in tilemap
     *
     * @param rectangle which tile index should be returned
     * @return vector of given rectangle's x and y indexes
     */
    private Vector2 getRectangleTileIndex(Rectangle rectangle) {
        float scale = game.getScale();
        return new Vector2(rectangle.x / scale / tileSize, rectangle.y / scale / tileSize);
    }

    /**
     * Clears given TileLayer's tile at given x and y index
     *
     * @param tileLayer on which layer the tile should be cleared
     * @param tileIndex coordinates of the tile to be cleared
     */
    private void clearTile(TiledMapTileLayer tileLayer, Vector2 tileIndex) {
        int x = (int) tileIndex.x;
        int y = (int) tileIndex.y;
        tileLayer.setCell(x, y, null);
    }

    /**
     * Creates world bodies from wall rectangles
     */
    private void createWalls() {
        ArrayList<Rectangle> wallRectangles = getWallRectangles();
        for (Rectangle wallRectangle : wallRectangles) {
            Body wallBody = world.createBody(getWallBodyDef(wallRectangle));
            wallBody.createFixture(getWallShape(wallRectangle), 0.0f);
        }
    }

    /**
     * Creates body for blocking the goal while there's good objects remaining in tilemap
     */
    private void createGoalBlockBody() {
        MapLayer goalBlockLayer = tiledMap.getLayers().get("goal_blocking_object");
        RectangleMapObject goalBlockObject = goalBlockLayer.getObjects().getByType(RectangleMapObject.class).get(0);
        Rectangle goalBlockRectangle = scaleRectangle(goalBlockObject.getRectangle(), game.getScale());
        goalBlock = world.createBody(getWallBodyDef(goalBlockRectangle));
        goalBlock.createFixture(getWallShape(goalBlockRectangle), 0.0f);
    }

    /**
     * Creates Rectangles scaled to world units from RectangleMapObjects on wall objects layer of the tilemap
     *
     * @return ArrayList of the Rectangles scaled to meters
     */
    private ArrayList<Rectangle> getWallRectangles() {
        ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
        MapLayer wallsLayer = tiledMap.getLayers().get("wall_objects");
        MapObjects mapObjects = wallsLayer.getObjects();
        Array<RectangleMapObject> rectangleObjects = mapObjects.getByType(RectangleMapObject.class);

        float scale = game.getScale();
        for (RectangleMapObject rectangleMapObject : rectangleObjects) {
            Rectangle tempRect = rectangleMapObject.getRectangle();
            Rectangle scaledRect = scaleRectangle(tempRect, scale);
            rectangles.add(scaledRect);
        }
        return rectangles;
    }

    /**
     * Creates Rectangles scaled to world units from RectangleMapObjects on good objects layer of the tilemap
     *
     * @return ArrayList of the Rectangles scaled to meters
     */
    private ArrayList<Rectangle> getGoodRectangles() {
        ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
        MapLayer goodLayer = tiledMap.getLayers().get("good_objects");
        MapObjects mapObjects = goodLayer.getObjects();
        Array<RectangleMapObject> rectangleObjects = mapObjects.getByType(RectangleMapObject.class);

        float scale = game.getScale();
        for (RectangleMapObject rectangleMapObject : rectangleObjects) {
            Rectangle tempRect = rectangleMapObject.getRectangle();
            Rectangle scaledRect = scaleRectangle(tempRect, scale);
            rectangles.add(scaledRect);
        }
        return rectangles;
    }

    /**
     * Creates Rectangles scaled to world units from RectangleMapObjects on bad objects layer of the tilemap
     *
     * @return ArrayList of the Rectangles scaled to meters
     */
    private ArrayList<Rectangle> getBadRectangles() {
        ArrayList<Rectangle> rectangles = new ArrayList<Rectangle>();
        MapLayer badLayer = tiledMap.getLayers().get("bad_objects");
        MapObjects mapObjects = badLayer.getObjects();
        Array<RectangleMapObject> rectangleObjects = mapObjects.getByType(RectangleMapObject.class);

        float scale = game.getScale();
        for (RectangleMapObject rectangleMapObject : rectangleObjects) {
            Rectangle tempRect = rectangleMapObject.getRectangle();
            Rectangle scaledRect = scaleRectangle(tempRect, scale);
            rectangles.add(scaledRect);
        }
        return rectangles;
    }

    /**
     * Creates Rectangle scaled to world units from RectangleMapObject on goal object layer of the tilemap
     */
    private void getGoalRectangle() {
        MapLayer goalLayer = tiledMap.getLayers().get("goal_object");
        MapObjects mapObjects = goalLayer.getObjects();
        RectangleMapObject rectangleObject = mapObjects.getByType(RectangleMapObject.class).get(0);

        float scale = game.getScale();

        Rectangle tempRect = rectangleObject.getRectangle();
        Rectangle scaledRect = scaleRectangle(tempRect, scale);

        goalRectangle = scaledRect;
    }

    /**
     * Creates BodyDef for wall bodies
     *
     * @param wallRect dimensions of the body to create
     * @return BodyDef of the wall
     */
    private BodyDef getWallBodyDef(Rectangle wallRect) {
        BodyDef wallBodyDef = new BodyDef();
        wallBodyDef.type = BodyDef.BodyType.StaticBody;
        wallBodyDef.position.set(
                wallRect.x + wallRect.width / 2,
                wallRect.y + wallRect.height / 2
        );
        return wallBodyDef;
    }

    /**
     * Creates PolygonShape of the scaled wall Rectangle
     *
     * @param wallRect dimensions of the shape to create
     * @return PolygonShape of the given wall rectangle
     */
    private PolygonShape getWallShape(Rectangle wallRect) {
        PolygonShape wallShape = new PolygonShape();
        wallShape.setAsBox(
                wallRect.width / 2,
                wallRect.height / 2
        );
        return wallShape;
    }

    /**
     * Scales given rectangle from pixels to meters
     *
     * @param r     Rectangle to scale
     * @param scale scaling to use
     * @return scaled Rectangle
     */
    private Rectangle scaleRectangle(Rectangle r, float scale) {
        Rectangle rr = new Rectangle();
        rr.x = r.x * scale;
        rr.y = r.y * scale;
        rr.width = r.width * scale;
        rr.height = r.height * scale;
        return rr;
    }

    @Override
    public void show() {
        if (paused) {
            updateLanguage();
            Gdx.input.setInputProcessor(multiplexer);
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
     * Disposes used assets
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

    private void updateTimeSpentLabel() {
        timeSpentLabel.setText(timeSpentText + game.formatTime(timeSpent));
    }

    private void updateObjectsLeftLabel() {
        objectsLeftLabel.setText(objectsLeftText + goodObjectsRemaining);
    }

    private void updateLanguage() {
        mazeBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MazeBundle"), options.getLocale());
        updateHudTexts();
        updatePauseMenuTexts();
    }

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

    private void updateHudTexts() {
        objectsLeftText = mazeBundle.get("goodObjectsRemaining");
        timeSpentText = mazeBundle.get("time");
        updateObjectsLeftLabel();
        updateTimeSpentLabel();
        pauseButton.setText(mazeBundle.get("pauseButton"));
    }
}
