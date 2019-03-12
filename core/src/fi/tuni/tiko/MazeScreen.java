package fi.tuni.tiko;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;

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
    private OrthographicCamera textCamera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;

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
    private String goodObjectsRemainingLabel;
    private String pointsLabel;
    private String timeSpentLabel;
    private int goodObjectsRemaining;
    private int points;
    private float timeSpent;

    private BitmapFont textFont;
    private BitmapFont hudFont;

    /**
     * Sets up the selected maze
     *
     * @param game      main game class
     * @param levelName name of the level that will be loaded and shown
     */
    public MazeScreen(RaccoonRoll game, String levelName) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        textFont = game.getTextFont();
        hudFont = game.getHudFont();

        world = new World(new Vector2(0, 0), true);
        player = new Player(game);

        debugRenderer = new Box2DDebugRenderer();

        mazeBundle = I18NBundle.createBundle(Gdx.files.internal("localization/MazeBundle"), game.getLocale());
        createLabels();

        loadTileMap(levelName);
        loadSounds();
        loadBackgroundMusic(levelName);

        player.createPlayerBody(world, getPlayerStartPos());
        goodObjectRectangles = getGoodRectangles();
        goodObjectsRemaining = goodObjectRectangles.size();
        badObjectRectangles = getBadRectangles();
        getGoalRectangle();

        createWalls();
        createGoalBlockBody();
        addContactListener();
    }

    /**
     * Add contact listener for playing sounds when player hits a wall
     */
    private void addContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                wallHitSounds.get(MathUtils.random(wallHitSounds.size() - 1)).play(game.getEffectsVolume());
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
            String filePath = String.format("sounds/wallHit/WALL_HIT_0%d.mp3", i);
            wallHitSounds.add(Gdx.audio.newSound(Gdx.files.internal(filePath)));
        }
        badSound = Gdx.audio.newSound(Gdx.files.internal("sounds/badObject/BAD_01.mp3"));
        goodSound = Gdx.audio.newSound(Gdx.files.internal("sounds/goodObject/GOOD_01.mp3"));
        victorySound = Gdx.audio.newSound(Gdx.files.internal("sounds/victory/VICTORY_01.mp3"));
    }

    /**
     * Loads levels background music, sets looping and volume and starts playing the music.
     *
     * @param levelName name of the level which music we want to load
     */
    private void loadBackgroundMusic(String levelName) {
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/backgroundMusic/" + levelName + ".mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(game.getMusicVolume());
        //backgroundMusic.play();
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

    /**
     * Gets localized labels for HUD from bundle
     */
    private void createLabels() {
        goodObjectsRemainingLabel = mazeBundle.get("goodObjectsRemaining");
        timeSpentLabel = mazeBundle.get("time");
        pointsLabel = mazeBundle.get("points");
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
        float startPosX = startPos.getProperties().get("x", Float.class);
        float startPosY = startPos.getProperties().get("y", Float.class);
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
        if (!goalReached) {
            timeSpent += delta;
        }

        clearScreen();
        player.movePlayer(delta);
        updateCameraPosition();
        checkGoodObjectOverlaps();
        checkBadObjectOverlaps();

        if (goodObjectsRemaining == 0) {
            checkGoalOverlap();
        }

        tiledMapRenderer.setView(worldCamera);
        tiledMapRenderer.render();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        player.draw(batch, delta);
        batch.setProjectionMatrix(textCamera.combined);
        drawHud();
        batch.end();
        if (game.DEBUGGING()) {
            debugRenderer.render(world, worldCamera.combined);
        }
        world.step(1 / 60f, 6, 2);
    }

    /**
     * Draws time, objects left and points to upper edge of the screen
     */
    private void drawHud() {
        int padding = game.scaleTextFromFHD(20);
        int textY = Gdx.graphics.getHeight() - padding;

        String timeSpentText = String.format("%s %s", timeSpentLabel, formatTime());
        hudFont.draw(
                batch,
                timeSpentText,
                padding,
                textY
        );

        String pointsText = String.format("%s %d", pointsLabel, points);
        hudFont.draw(
                batch,
                pointsText,
                Gdx.graphics.getWidth() - game.getTextDimensions(hudFont, pointsText).x - padding,
                textY
        );

        String goodObjectsRemainingText = String.format("%s %d", goodObjectsRemainingLabel, goodObjectsRemaining);
        Vector2 goodObjectsRemainingTextDimensions = game.getTextDimensions(hudFont, goodObjectsRemainingText);
        hudFont.draw(
                batch,
                goodObjectsRemainingText,
                Gdx.graphics.getWidth() / 2 - goodObjectsRemainingTextDimensions.x / 2,
                textY
        );
    }

    /**
     * Formats time from float seconds
     *
     * @return time in m:ss
     */
    private String formatTime() {
        int time = (int) timeSpent;
        int minutes = time / 60;
        int seconds = time % 60;
        return String.format("%d:%02d", minutes, seconds);
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
                points++;
                goodSound.play(game.getEffectsVolume());
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
            victorySound.play(game.getEffectsVolume());
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
                points--;
                badSound.play(game.getEffectsVolume());
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

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    /**
     * Disposes used assets
     */
    @Override
    public void dispose() {
        player.dispose();
        for (Sound wallHitSound : wallHitSounds) {
            wallHitSound.dispose();
        }
        backgroundMusic.stop();
        backgroundMusic.dispose();
        victorySound.dispose();
        goodSound.dispose();
        badSound.dispose();

        tiledMap.dispose();
        world.dispose();
        debugRenderer.dispose();
    }
}
