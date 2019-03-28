package fi.tuni.tiko2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;

public class TutorialScreen implements Screen {
    private RaccoonRoll game;
    private SpriteBatch batch;
    private Player player;
    private OrthographicCamera worldCamera;
    private OrthographicCamera textCamera;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private TiledMapRenderer tiledMapRenderer;
    private TiledMap tiledMap;

    private Skin skin;
    private Stage stage;
    private TextButton tutorialMazeButton;

    private ArrayList<Sound> wallHitSounds;
    private Music backgroundMusic;

    private I18NBundle tutorialBundle;
    private Options options;
    private BitmapFont textFont;

    private float WORLD_WIDTH;
    private float WORLD_HEIGHT;

    private final float TIME_STEP = 1 / 61f;

    private final float tileSize = 64f;

    private boolean goToTutorialMaze;


    public TutorialScreen(RaccoonRoll game) {
        this.game = game;
        options = game.getOptions();
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();
        textFont = game.getTextFont();

        stage = new Stage(new ScreenViewport());

        Gdx.input.setInputProcessor(stage);
        Gdx.input.setCatchBackKey(true);

        tiledMap = new TmxMapLoader().load("tilemaps/tutorial/tutorial.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, game.getScale());
        float mapWidth = tiledMap.getProperties().get("width", Integer.class) * tileSize * game.getScale();
        float mapHeight = tiledMap.getProperties().get("height", Integer.class) * tileSize * game.getScale();

        WORLD_HEIGHT = game.getWORLD_HEIGHT();
        WORLD_WIDTH = game.getWORLD_WIDTH();
        worldCamera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
        worldCamera.position.set(mapWidth / 2, mapHeight / 2, 0);
        worldCamera.update();

        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();
        player = new Player(game);
        player.createPlayerBody(world, getPlayerStartPos());

        tutorialBundle = I18NBundle.createBundle(Gdx.files.internal("localization/TutorialBundle"), options.getLocale());
        loadSounds();
        loadBackgroundMusic("tutorial");

        createWalls();

        addContactListener();

        setupUi();
    }

    private void setupUi() {
        loadSkin();
        createTable();
    }

    private void createTable() {
        tutorialMazeButton = new TextButton(tutorialBundle.get("buttonTxt"), skin);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);
        Table itemsTable = new Table();

        if (game.DEBUGGING()) {
            table.setDebug(true);
            itemsTable.setDebug(true);
        }

        Label howToMoveLabel = new Label(tutorialBundle.get("howToMove"), skin);
        Label goodLabel = new Label(tutorialBundle.get("gatherGood"), skin);
        goodLabel.setWrap(true);
        goodLabel.setAlignment(Align.top, Align.center);
        Label badLabel = new Label(tutorialBundle.get("avoidBad"), skin);
        badLabel.setWrap(true);
        badLabel.setAlignment(Align.top, Align.center);
        goodLabel.setFontScale(0.8f);
        badLabel.setFontScale(0.8f);

        table.add(howToMoveLabel).padTop(game.scaleVertical(75)).padBottom(game.scaleVertical(100));
        table.row().expand().fill();
        table.add(itemsTable);
        itemsTable.add(goodLabel).top().left().padLeft(50).expand().fill();
        itemsTable.add(badLabel).top().right().padRight(50).expand().fill();
        table.row().right().padRight(game.scaleHorizontal(10)).bottom().padBottom(game.scaleVertical(10));
        table.add(tutorialMazeButton);

        tutorialMazeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.DEBUGGING()) {
                    Gdx.app.log("MazeButton", "Clicked");
                    goToTutorialMaze = true;
                }
            }
        });
    }

    private void loadSkin() {
        skin = new Skin();
        skin.addRegions(new TextureAtlas(Gdx.files.internal("uiskin/comic-ui.atlas")));
        skin.add("button", game.getButtonFont());
        skin.add("title", game.getTitleFont());
        skin.add("font", game.getTextFont());

        skin.load(Gdx.files.internal("uiskin/comic-ui.json"));
    }

    /**
     * Loads levels background music, sets looping and volume and starts playing the music.
     *
     * @param levelName name of the level which music we want to load
     */
    private void loadBackgroundMusic(String levelName) {
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/backgroundMusic/" + levelName + ".mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(options.getMusicVolume());
        backgroundMusic.play();
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

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        clearScreen();

        tiledMapRenderer.setView(worldCamera);
        tiledMapRenderer.render();

        player.movePlayer(delta);
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        player.draw(batch, delta);
        batch.setProjectionMatrix(textCamera.combined);
        batch.end();

        if (game.DEBUGGING()) {
            debugRenderer.render(world, worldCamera.combined);
        }

        stepWorld(delta);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)) {
            game.setScreen(new MenuScreen(game));
            dispose();
        }

        if (goToTutorialMaze) {
            game.setScreen(new MazeScreen(game, "tutorial"));
            dispose();
        }
    }

    private void stepWorld(float delta) {
        double accumulator;
        if (delta > 1 / 4f) {
            accumulator = 1 / 4f;
        } else if (delta < TIME_STEP) {
            accumulator = TIME_STEP;
        } else {
            accumulator = delta;
        }

        /*
        if (game.DEBUGGING()) {
            Gdx.app.log("FPS", "" + Gdx.graphics.getFramesPerSecond());
            Gdx.app.log("DeltaTime", "" + delta);
            Gdx.app.log("TimeStep ", "" + TIME_STEP);
            Gdx.app.log("Delta / Timestep", "" + df.format(delta / TIME_STEP));
        }
        */
        while (accumulator >= TIME_STEP) {
            if (game.DEBUGGING()) {
                //Gdx.app.log("WorldStep Accumulator", "" + accumulator);
            }
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }

        if (game.DEBUGGING()) {
            //Gdx.app.log("LeftOver", df.format(accumulator));
        }
    }

    /**
     * Clears the screen with black color
     */
    private void clearScreen() {
        Gdx.gl.glClearColor(74f / 255, 60f / 255, 27f / 255, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void resize(int width, int height) {

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

    @Override
    public void dispose() {
        stage.dispose();
        tiledMap.dispose();
        backgroundMusic.stop();
        backgroundMusic.dispose();
        player.dispose();
        world.dispose();
        if (game.DEBUGGING()) {
            Gdx.app.log("Disposed", "TutorialScreen");
        }
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
}
