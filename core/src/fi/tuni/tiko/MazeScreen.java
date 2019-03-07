package fi.tuni.tiko;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

public class MazeScreen implements Screen {
    RaccoonRoll game;
    SpriteBatch batch;
    OrthographicCamera worldCamera;
    OrthographicCamera textCamera;
    World world;
    Body playerBody;
    Box2DDebugRenderer debugRenderer;
    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;

    float tiledMapHeight;
    float tiledMapWidth;
    float tileSize = 16f;
    ArrayList<Rectangle> goodObjectsRectangles;

    // Refactor attributes below this line to separate player class
    float playerRotation;
    float playerRadius;
    Animation<TextureRegion> playerAnimation;
    float statetime;


    public MazeScreen(RaccoonRoll game, String levelName) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();

        world = new World(new Vector2(0, 0), true);

        debugRenderer = new Box2DDebugRenderer();

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("graphics/player/roll_animation/racc_roll.txt"));
        playerAnimation = new Animation<TextureRegion>(1 / 30f, atlas.findRegions("racc_roll"));

        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.textureMinFilter = Texture.TextureFilter.Nearest;
        parameters.textureMagFilter = Texture.TextureFilter.Nearest;
        tiledMap = new TmxMapLoader().load("tilemaps/" + levelName + ".tmx", parameters);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, game.getScale());

        MapProperties mapProps = tiledMap.getProperties();
        tiledMapWidth = mapProps.get("width", Integer.class) * tileSize * game.getScale();
        tiledMapHeight = mapProps.get("height", Integer.class) * tileSize * game.getScale();

        MapLayer startPosLayer = tiledMap.getLayers().get("startpos");
        MapObject startPos = startPosLayer.getObjects().get(0);
        float startPosX = startPos.getProperties().get("x", Float.class);
        float startPosY = startPos.getProperties().get("y", Float.class);

        playerBody = world.createBody(getPlayerBodyDef(startPosX, startPosY));
        playerBody.createFixture(getPlayerFixtureDef());

        goodObjectsRectangles = getGoodRectangles();

        createWalls();
        playerRotation = 90;
        playerRadius = 12 * game.getScale();
    }

    public void clearScreen() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Resize", "happened");
    }

    @Override
    public void render(float delta) {
        clearScreen();
        movePlayer(delta);
        updateCameraPosition();
        checkGoodObjectOverlaps();
        Vector2 playerVelocity = playerBody.getLinearVelocity();
        if (playerVelocity.x != 0 || playerVelocity.y != 0) {
            playerRotation = playerBody.getLinearVelocity().angle();
            statetime += delta * Math.max(Math.abs(playerVelocity.x), Math.abs(playerVelocity.y));
        }
        tiledMapRenderer.setView(worldCamera);
        tiledMapRenderer.render();
        //debugRenderer.render(world, worldCamera.combined);
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        batch.draw(
                playerAnimation.getKeyFrame(statetime, true),
                playerBody.getPosition().x - playerRadius,
                playerBody.getPosition().y - playerRadius,
                playerRadius,
                playerRadius,
                playerRadius * 2,
                playerRadius * 2,
                1.0f,
                1.0f,
                playerRotation - 90);
        batch.end();
        world.step(1 / 60f, 6, 2);
    }

    private void checkGoodObjectOverlaps() {
        ArrayList<Rectangle> rectanglesToRemove = new ArrayList<Rectangle>();
        float playerX = playerBody.getPosition().x;
        float playerY = playerBody.getPosition().y;
        // Vector2 playerPos = playerBody.getWorldCenter();
        Circle playerCircle = new Circle(playerX, playerY, playerRadius);

        for (Rectangle rectangle : goodObjectsRectangles) {
            if (Intersector.overlaps(playerCircle, rectangle)) {
                rectanglesToRemove.add(rectangle);
                clearTile(
                        (TiledMapTileLayer) tiledMap.getLayers().get("good_tiles"),
                        getRectangleTileIndex(rectangle)
                );
            }
        }
        goodObjectsRectangles.removeAll(rectanglesToRemove);
    }

    private Vector2 getRectangleTileIndex(Rectangle rectangle) {
        float scale = game.getScale();
        return new Vector2(rectangle.x / scale / tileSize, rectangle.y / scale / tileSize);
    }

    private void clearTile(TiledMapTileLayer tileLayer, Vector2 tileIndex) {
        int x = (int) tileIndex.x;
        int y = (int) tileIndex.y;
        tileLayer.setCell(x, y, null);
    }

    private void updateCameraPosition() {
        Vector2 playerPos = playerBody.getPosition();
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

    private void movePlayer(float deltatime) {
        float x = 0;
        float y = 0;

        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
                x = 150f * deltatime;
            } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
                x = -150f * deltatime;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
                y = 150f * deltatime;
            } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
                y = -150f * deltatime;
            }
        } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
            x = Gdx.input.getAccelerometerY() * 25 * deltatime;
            y = Gdx.input.getAccelerometerX() * 25 * deltatime;
            if (x < 0.2 && x > -0.2) {
                x = 0;
            }
            if (y < 0.2 && y > -0.2) {
                y = 0;
            }
            if (y > 0) {
                y = -y;
            } else {
                y = Math.abs(y);
            }
        }
        /*
        playerBody.applyForceToCenter(
                new Vector2(x, y),
                true);
        */
        playerBody.setLinearVelocity(x, y);
    }

    private BodyDef getPlayerBodyDef(float x, float y) {
        float scale = game.getScale();
        BodyDef playerBodyDef = new BodyDef();
        playerBodyDef.type = BodyDef.BodyType.DynamicBody;
        playerBodyDef.position.set(x * scale, y * scale);
        return playerBodyDef;
    }

    private FixtureDef getPlayerFixtureDef() {
        FixtureDef playerFixtureDef = new FixtureDef();
        playerFixtureDef.density = 0.1f;
        playerFixtureDef.restitution = 0.1f;
        playerFixtureDef.friction = 0.75f;

        CircleShape playerCircle = new CircleShape();
        playerCircle.setRadius(12 * game.getScale());

        playerFixtureDef.shape = playerCircle;

        return playerFixtureDef;
    }

    private void createWalls() {
        ArrayList<Rectangle> wallRectangles = getWallRectangles();
        for (Rectangle wallRectangle : wallRectangles) {
            Body wallBody = world.createBody(getWallBodyDef(wallRectangle));
            wallBody.createFixture(getWallShape(wallRectangle), 0.0f);
        }
    }

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

    private BodyDef getWallBodyDef(Rectangle wallRect) {
        BodyDef wallBodyDef = new BodyDef();
        wallBodyDef.type = BodyDef.BodyType.StaticBody;
        wallBodyDef.position.set(
                wallRect.x + wallRect.width / 2,
                wallRect.y + wallRect.height / 2
        );
        return wallBodyDef;
    }

    private PolygonShape getWallShape(Rectangle wallRect) {
        PolygonShape wallShape = new PolygonShape();
        wallShape.setAsBox(
                wallRect.width / 2,
                wallRect.height / 2
        );
        return wallShape;
    }

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

    @Override
    public void dispose() {

    }
}
