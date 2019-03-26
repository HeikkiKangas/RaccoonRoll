package fi.tuni.tiko2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

public class TiledMapUtils {
    /**
     * Loads tilemap, sets up TiledMapRenderer and TiledMap dimensions in meters
     *
     * @param levelName
     */
    public void loadTileMap(String levelName) {
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
     * Gets player starting position from tilemap
     *
     * @return vector of player start position
     */
    public Vector2 getPlayerStartPos() {
        MapLayer startPosLayer = tiledMap.getLayers().get("startpos");
        MapObject startPos = startPosLayer.getObjects().get(0);
        float startPosX = startPos.getProperties().get("x", Float.class) * game.getScale();
        float startPosY = startPos.getProperties().get("y", Float.class) * game.getScale();
        return new Vector2(startPosX, startPosY);
    }

    /**
     * Creates world bodies from wall rectangles
     */
    public void createWalls() {
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
    public ArrayList<Rectangle> getWallRectangles() {
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
    public BodyDef getWallBodyDef(Rectangle wallRect) {
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
    public PolygonShape getWallShape(Rectangle wallRect) {
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
    public Rectangle scaleRectangle(Rectangle r, float scale) {
        Rectangle rr = new Rectangle();
        rr.x = r.x * scale;
        rr.y = r.y * scale;
        rr.width = r.width * scale;
        rr.height = r.height * scale;
        return rr;
    }
}
