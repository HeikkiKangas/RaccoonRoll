package fi.tuni.tiko2d;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.ArrayList;

public class TiledMapUtil {
    private RaccoonRoll game;

    public TiledMapUtil(RaccoonRoll game) {
        this.game = game;
    }

    /**
     * Steps the world with 1/61s steps while delta is bigger than 1/61
     *
     * @param delta time since last frame
     */
    public void stepWorld(float delta, World world) {
        float time_step = 1f / 61;
        double accumulator;
        if (delta > 1 / 4f) {
            accumulator = 1 / 4f;
        } else if (delta < time_step) {
            accumulator = time_step;
        } else {
            accumulator = delta;
        }

        while (accumulator >= time_step) {
            world.step(time_step, 6, 2);
            accumulator -= time_step;
        }
    }

    /**
     * Hides the goal on tilemap
     */
    public void hideGoal(TiledMap tiledMap) {
        tiledMap.getLayers().get("goal").setVisible(false);
        tiledMap.getLayers().get("goal_ground").setVisible(false);
        if (tiledMap.getLayers().getIndex("goal_ground2") != -1) {
            tiledMap.getLayers().get("goal_ground2").setVisible(false);
        }
    }

    /**
     * Reveals the goal on tilemap
     */
    public void showGoal(TiledMap tiledMap, World world, Body goalBlock) {
        tiledMap.getLayers().get("goal").setVisible(true);
        tiledMap.getLayers().get("goal_ground").setVisible(true);
        if (tiledMap.getLayers().getIndex("goal_ground2") != -1) {
            tiledMap.getLayers().get("goal_ground2").setVisible(true);
        }
        world.destroyBody(goalBlock);
    }

    /**
     * Gets player starting position from tilemap
     *
     * @return vector of player start position
     */
    public Vector2 getPlayerStartPos(TiledMap tiledMap) {
        MapLayer startPosLayer = tiledMap.getLayers().get("startpos");
        MapObject startPos = startPosLayer.getObjects().get(0);
        float startPosX = startPos.getProperties().get("x", Float.class) * game.getScale();
        float startPosY = startPos.getProperties().get("y", Float.class) * game.getScale();
        return new Vector2(startPosX, startPosY);
    }

    /**
     * Checks given rectangles x and y index in tilemap
     *
     * @param rectangle which tile index should be returned
     * @return vector of given rectangle's x and y indexes
     */
    public Vector2 getRectangleTileIndex(Rectangle rectangle, float tileSize) {
        float scale = game.getScale();
        return new Vector2(rectangle.x / scale / tileSize, rectangle.y / scale / tileSize);
    }

    /**
     * Clears given TileLayer's tile at given x and y index
     *
     * @param tileLayer on which layer the tile should be cleared
     * @param tileIndex coordinates of the tile to be cleared
     */
    public void clearTile(TiledMapTileLayer tileLayer, Vector2 tileIndex) {
        int x = (int) tileIndex.x;
        int y = (int) tileIndex.y;
        tileLayer.setCell(x, y, null);
    }

    /**
     * Creates body for blocking the goal while there's good objects remaining in tilemap
     */
    public Body createGoalBlockBody(TiledMap tiledMap, World world) {
        MapLayer goalBlockLayer = tiledMap.getLayers().get("goal_blocking_object");
        RectangleMapObject goalBlockObject = goalBlockLayer.getObjects().getByType(RectangleMapObject.class).get(0);
        Rectangle goalBlockRectangle = scaleRectangle(goalBlockObject.getRectangle(), game.getScale());
        Body goalBlock = world.createBody(getWallBodyDef(goalBlockRectangle));
        goalBlock.createFixture(getWallShape(goalBlockRectangle), 0.0f);
        return goalBlock;
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

    /**
     * Creates Rectangles scaled to world units from RectangleMapObjects on wall objects layer of the tilemap
     *
     * @return ArrayList of the Rectangles scaled to meters
     */
    public ArrayList<Rectangle> getWallRectangles(TiledMap tiledMap) {
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
     * Creates world bodies from wall rectangles
     */
    public void createWalls(ArrayList<Rectangle> wallRectangles, World world) {
        for (Rectangle wallRectangle : wallRectangles) {
            Body wallBody = world.createBody(getWallBodyDef(wallRectangle));
            wallBody.createFixture(getWallShape(wallRectangle), 0.0f);
        }
    }

    /**
     * Creates Rectangle scaled to world units from RectangleMapObject on goal object layer of the tilemap
     */
    public Rectangle getGoalRectangle(TiledMap tiledMap) {
        MapLayer goalLayer = tiledMap.getLayers().get("goal_object");
        MapObjects mapObjects = goalLayer.getObjects();
        RectangleMapObject rectangleObject = mapObjects.getByType(RectangleMapObject.class).get(0);

        float scale = game.getScale();

        Rectangle tempRect = rectangleObject.getRectangle();
        return scaleRectangle(tempRect, scale);
    }

    /**
     * Creates Rectangles scaled to world units from RectangleMapObjects on good objects layer of the tilemap
     *
     * @return ArrayList of the Rectangles scaled to meters
     */
    public ArrayList<Rectangle> getGoodRectangles(TiledMap tiledMap) {
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
    public ArrayList<Rectangle> getBadRectangles(TiledMap tiledMap) {
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
}
