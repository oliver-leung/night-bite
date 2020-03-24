package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef;
import edu.cornell.gdiac.nightbite.entity.*;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.PooledList;

public class WorldModel {
    /** Density of objects */
    private static final float BASIC_DENSITY = 0.0f;
    /** Friction of objects */
    private static final float BASIC_FRICTION = 1f;
    /** Collision restitution for all objects */
    private static final float BASIC_RESTITUTION = 0f;

    /** Objects that don't move during updates */
    private PooledList<Obstacle> staticObjects;
    /** Objects that move during updates */
    private PooledList<Obstacle> dynmaicObjects;

    private int NUM_PLAYERS = 2;
    private PlayerModel[] player_list;
    // TODO: Maybe use a better data structure
    private ItemModel[] items;

    private Rectangle bounds;

    public WorldModel() {
        // TODO: Make this data driven
        bounds = new Rectangle(0, 0, 32f, 16f);
    }

    public void populate() {
        // TODO: PLEASE FIX HOW OBJECTS ARE INSTANTIATED PROPERLY
        // TODO: WE NEED TO MAKE IT SO THAT

        /* Add holes */
        PolygonObstacle obj;
        obj = new HoleModel(LevelController.WALL1, 16, 5);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(holeTile);
        addObject(obj);

        obj = new HoleModel(LevelController.WALL2, 2, 4);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(holeTile);
        addObject(obj);

        obj = new HoleModel(LevelController.WALL2, 30, 4);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(holeTile);
        addObject(obj);

        /* Add walls */
        obj = new PolygonObstacle(LevelController.WALL2, 9.5f, 8);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(wallTile);
        obj.setName("wall1");
        addObject(obj);

        obj = new PolygonObstacle(LevelController.WALL2, 22.5f, 8);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(wallTile);
        obj.setName("wall2");
        addObject(obj);

        BoxObstacle wall;
        float ddwidth = wallTile.getRegionWidth() / scale.x;
        float ddheight = wallTile.getRegionHeight() / scale.y;
        wall = new BoxObstacle(16, 3.5f, ddwidth, ddheight);
        wall.setDensity(BASIC_DENSITY);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setDrawScale(scale);
        wall.setTexture(standTile);
        wall.setName("wall3");
        addObject(wall);

        /* Add screen edges */

        // left screen edge
        obj = new PolygonObstacle(LevelController.VERT_WALL, 32.5f, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        // right screen edge
        obj = new PolygonObstacle(LevelController.VERT_WALL, -0.5f, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        // top screen edge
        obj = new PolygonObstacle(LevelController.HORI_WALL, 0.0f, -0.5f);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        // bottom screen edge
        obj = new PolygonObstacle(LevelController.HORI_WALL, 0.0f, 18.5f);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(BASIC_DENSITY);
        obj.setFriction(BASIC_FRICTION);
        obj.setRestitution(BASIC_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addObject(obj);

        /* Add players */
        // Team A
        float pWidth = PlayerModel.player1Texture.getRegionWidth() / scale.x;
        float pHeight = PlayerModel.player1Texture.getRegionHeight() / scale.y;
        p1 = new PlayerModel(LevelController.p1_position.x, LevelController.p1_position.y, pWidth, pHeight, "a", 0);
        p1.setDrawScale(scale);
        p1.setTexture(PlayerModel.player1Texture);
        p2.setMovable(true);

        /* Add home stalls */
        // Team A
        HomeModel home = new HomeModel(p1.getHomeLoc().x, p1.getHomeLoc().y, 2f, 2f, "a");
        home.setBodyType(BodyDef.BodyType.StaticBody);
        home.setDrawScale(scale);
        home.setTexture(standTile);
        home.setName("homeA");
        addObject(home);

        /* Add players */
        // Team B
        p2 = new PlayerModel(LevelController.p2_position.x, LevelController.p2_position.y, pWidth, pHeight, "b",1);
        p2.setDrawScale(scale);
        p2.setTexture(PlayerModel.player2FilmStrip);
        p2.setMovable(true);

        /* Add home stalls */
        // Team B
        home = new HomeModel(p2.getHomeLoc().x, p2.getHomeLoc().y, 2f, 2f, "b");
        home.setBodyType(BodyDef.BodyType.StaticBody);
        home.setDrawScale(scale);
        home.setTexture(standTile);
        home.setName("homeB");
        addObject(home);
        addObject(p1);
        addObject(p2);

        // player list
        player_list = new PlayerModel[] { p1, p2 };

        /* Add items */
        float itemWidth = ItemModel.itemTexture.getRegionWidth() / scale.x;
        float itemHeight = ItemModel.itemTexture.getRegionHeight() / scale.y;
        item = new ItemModel(item_position.x, item_position.y, itemWidth, itemHeight);
        item.setDrawScale(scale);
        item.setTexture(ItemModel.itemTexture);
        item.setSensor(true);
        item.setMovable(true);
        addObject(item);
    }

    public void setObjetctScale(float sx, float sy) {

    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    public void reset() {
        staticObjects.clear();
        dynmaicObjects.clear();
    }
}
