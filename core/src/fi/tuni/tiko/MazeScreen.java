package fi.tuni.tiko;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
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
    Box2DDebugRenderer debugRenderer;
    TiledMap tiledMap;
    TiledMapRenderer tiledMapRenderer;

    float tiledMapHeight;
    float tiledMapWidth;
    float tileSize = 64f;
    ArrayList<Rectangle> goodObjectsRectangles;
    Player player;

    int goodObjectsRemaining;

    public MazeScreen(RaccoonRoll game, String levelName) {
        this.game = game;
        batch = game.getBatch();
        worldCamera = game.getWorldCamera();
        textCamera = game.getTextCamera();

        world = new World(new Vector2(0, 0), true);
        player = new Player(game);

        debugRenderer = new Box2DDebugRenderer();



        TmxMapLoader.Parameters parameters = new TmxMapLoader.Parameters();
        parameters.textureMinFilter = Texture.TextureFilter.Nearest;
        parameters.textureMagFilter = Texture.TextureFilter.Nearest;
        tiledMap = new TmxMapLoader().load("tilemaps/tutorial_64/" + levelName + ".tmx", parameters);
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, game.getScale());

        MapProperties mapProps = tiledMap.getProperties();
        tiledMapWidth = mapProps.get("width", Integer.class) * tileSize * game.getScale();
        tiledMapHeight = mapProps.get("height", Integer.class) * tileSize * game.getScale();

        player.createPlayerBody(world, getPlayerStartPos());
        goodObjectsRectangles = getGoodRectangles();
        goodObjectsRemaining = goodObjectsRectangles.size();

        createWalls();
    }

    public void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    public Vector2 getPlayerStartPos() {
        MapLayer startPosLayer = tiledMap.getLayers().get("startpos");
        MapObject startPos = startPosLayer.getObjects().get(0);
        float startPosX = startPos.getProperties().get("x", Float.class);
        float startPosY = startPos.getProperties().get("y", Float.class);
        return new Vector2(startPosX, startPosY);
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("Resize", "happened");
    }

    @Override
    public void render(float delta) {
        clearScreen();
        player.movePlayer(delta);
        updateCameraPosition();
        checkGoodObjectOverlaps();

        tiledMapRenderer.setView(worldCamera);
        tiledMapRenderer.render();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        player.draw(batch, delta);
        //drawGoodObjectsRemaining();
        batch.end();
        //debugRenderer.render(world, worldCamera.combined);
        world.step(1 / 60f, 6, 2);
    }

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

    private void checkGoodObjectOverlaps() {
        ArrayList<Rectangle> rectanglesToRemove = new ArrayList<Rectangle>();
        Vector2 playerPos = player.getPosition();
        Circle playerCircle = new Circle(playerPos.x, playerPos.y, player.getBodyRadius());

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
        goodObjectsRemaining = goodObjectsRectangles.size();
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
