package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import edu.cornell.gdiac.nightbite.entity.*;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;
import org.w3c.dom.Text;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class WorldModel {
    /** Immovable object parameters */
    private static final float IMMOVABLE_OBJ_DENSITY = 0f;
    private static final float IMMOVABLE_OBJ_FRICTION = 1f;
    private static final float IMMOVABLE_OBJ_RESTITUTION = 0f;

    /** Movable object parameters */
    private static final float MOVABLE_OBJ_DENSITY = 1.0f;
    private static final float MOVABLE_OBJ_FRICTION = 0.1f;
    private static final float MOVABLE_OBJ_RESTITUTION = 0.4f;

    public String winner;

    /**
     * How many frames after winning/losing do we continue?
     */
    public static final int EXIT_COUNT = 120;

    /** World */
    protected World world;
     /** World scale */
    protected Vector2 scale;

    // TODO: Maybe use a better data structure
    private ItemModel[] items;

    // TODO: This should be data driven
    /** Item parameters */
    protected static Vector2 ITEM_START_POSITION = new Vector2(16, 12);

    // TODO: PLEASE remove this eventually
    public Vector2 getITEMSTART() {
        return ITEM_START_POSITION;
    }

    // TODO: PLEASE fix this
    public ItemModel getItem() {
        return items[0];
    }

    /**
     * Whether we have completed this level
     */
    private boolean complete;
    /**
     * Countdown active for winning or losing
     */
    private int countdown;

    /** FOR AI */

    /** Objects that don't move during updates */
    private PooledList<Obstacle> staticObjects;
    /** Objects that move during updates */
    private PooledList<Obstacle> dynamicObjects;

    private PlayerModel[] player_list;

    private Rectangle bounds;

    /** Player textures */
    public static TextureRegion player1Texture;
    public static FilmStrip player2FilmStrip;


    // TODO: REMOVE ALL THESE DUMB TEXTURES
    TextureRegion wallTile;
    TextureRegion standTile;
    TextureRegion backgroundTile;
    TextureRegion goalTile;
    TextureRegion holeTile;

    public void setTextures(TextureRegion[] textures) {
        wallTile = textures[0];
        standTile = textures[1];
        backgroundTile = textures[2];
        goalTile = textures[3];
        holeTile = textures[4];
    }

    // TODO: END REMOVE ALL THESE DUMB TEXTURES

    // TODO: DO we need addQueue?

    public WorldModel() {
        // TODO: We need a contact listener for WorldModel, which means we need to have a CollisionManager
        // Actually technically not true since we can set this stuff in WorldController, but still
        world = new World(Vector2.Zero, false);
        // TODO: CollisionController
        // TODO: Make this data driven
        bounds = new Rectangle(0, 0, 32f, 18f);
        scale = new Vector2(1f, 1f);
        dynamicObjects = new PooledList<>();
        staticObjects = new PooledList<>();
        complete = false;
        countdown = -1;
    }

    /**
     * Returns true if the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @return true if the level is completed.
     */
    public boolean isComplete() {
        return complete;
    }

    public boolean isDone() {
        countdown --;
        return countdown <= 0 && complete;
    }

    /**
     * Complete the level
     */
    public void completeLevel() {
        countdown = EXIT_COUNT;
        complete = true;
    }

    public void setContactListener(ContactListener c) {
        world.setContactListener(c);
    }

    public Iterable<Obstacle> getObjects() {
        // Overkill, but I'm bored. Also this will probably help like a lot.
        class objectIterable implements Iterator<Obstacle> {
            // private PooledList<Obstacle>[] lists = new PooledList[] {staticObjects, dynamicObjects};
            private Iterator<Obstacle> list0 = staticObjects.iterator();
            private Iterator<Obstacle> list1 = dynamicObjects.iterator();
            @Override
            public boolean hasNext() {
                return list0.hasNext() || list1.hasNext();
            }

            @Override
            public Obstacle next() {
                if (list0.hasNext()) {
                    return list0.next();
                }
                if (list1.hasNext()) {
                    return list1.next();
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        class objectIterator implements Iterable<Obstacle> {
            @Override
            public Iterator<Obstacle> iterator() {
                return new objectIterable();
            }
        }

        return new objectIterator();
    }

    public Iterator<PooledList<Obstacle>.Entry> objectEntryIter() {
        // Overkill, but I'm bored. Also this will probably help like a lot.
        class objectIterable implements Iterator<PooledList<Obstacle>.Entry> {
            Iterator<PooledList<Obstacle>.Entry> statIter = staticObjects.entryIterator();
            Iterator<PooledList<Obstacle>.Entry> dynIter = dynamicObjects.entryIterator();
            boolean finishedStat = false;
            @Override
            public boolean hasNext() {
                return !statIter.hasNext() && ! dynIter.hasNext();
            }

            @Override
            public PooledList<Obstacle>.Entry next() {
                if (statIter.hasNext()) {
                    return statIter.next();
                }

                if (dynIter.hasNext()) {
                    return dynIter.next();
                }

                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        return new objectIterable();

    }

    public void populate() {
        // TODO: PLEASE FIX HOW OBJECTS ARE INSTANTIATED PROPERLY
        // TODO: WE NEED TO MAKE IT SO THAT THIS IS LIKE DATA DRIVEN OR SOME CRAP

        // TODO: What are we going to do with assets

        /* Add holes */
        PolygonObstacle obj;
        obj = new HoleModel(LevelController.WALL1, 16, 5);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(holeTile);
        addStaticObject(obj);

        obj = new HoleModel(LevelController.WALL2, 2, 4);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(holeTile);
        addStaticObject(obj);

        obj = new HoleModel(LevelController.WALL2, 30, 4);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(holeTile);
        addStaticObject(obj);

        /* Add walls */
        obj = new WallModel(LevelController.WALL2, 9.5f, 8);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(wallTile);
        obj.setName("wall1");
        addStaticObject(obj);

        obj = new WallModel(LevelController.WALL2, 22.5f, 8);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(wallTile);
        obj.setName("wall2");
        addStaticObject(obj);

        BoxObstacle wall;
        float ddwidth = wallTile.getRegionWidth() / scale.x;
        float ddheight = wallTile.getRegionHeight() / scale.y;
        wall = new BoxObstacle(16, 3.5f, ddwidth, ddheight);
        wall.setDensity(IMMOVABLE_OBJ_DENSITY);
        wall.setFriction(IMMOVABLE_OBJ_FRICTION);
        wall.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        wall.setBodyType(BodyDef.BodyType.StaticBody);
        wall.setDrawScale(scale);
        wall.setTexture(standTile);
        wall.setName("wall3");
        addStaticObject(wall);

        /* Add screen edges */

        // left screen edge
        obj = new WallModel(LevelController.VERT_WALL, 32.5f, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addStaticObject(obj);

        // right screen edge
        obj = new WallModel(LevelController.VERT_WALL, -0.5f, 0);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addStaticObject(obj);

        // top screen edge
        obj = new WallModel(LevelController.HORI_WALL, 0.0f, -0.5f);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addStaticObject(obj);

        // bottom screen edge
        obj = new WallModel(LevelController.HORI_WALL, 0.0f, 18.5f);
        obj.setBodyType(BodyDef.BodyType.StaticBody);
        obj.setDensity(IMMOVABLE_OBJ_DENSITY);
        obj.setFriction(IMMOVABLE_OBJ_FRICTION);
        obj.setRestitution(IMMOVABLE_OBJ_RESTITUTION);
        obj.setDrawScale(scale);
        obj.setTexture(standTile);
        obj.setName("wall1");
        addStaticObject(obj);

        /* Add players */
        // Team A
        float pWidth = player1Texture.getRegionWidth() / scale.x;
        float pHeight = player1Texture.getRegionHeight() / scale.y;
        PlayerModel p1 = new PlayerModel(LevelController.p1_position.x, LevelController.p1_position.y, pWidth, pHeight, player1Texture, "a");
        p1.setDensity(MOVABLE_OBJ_DENSITY);
        p1.setFriction(MOVABLE_OBJ_FRICTION);
        p1.setRestitution(MOVABLE_OBJ_RESTITUTION);
        p1.setDrawScale(scale);
//        p1.setMovable(true);

        /* Add home stalls */
        // Team A
        HomeModel home = new HomeModel(p1.getHomeLoc().x, p1.getHomeLoc().y, 2f, 2f, "a");
        home.setBodyType(BodyDef.BodyType.StaticBody);
        home.setDrawScale(scale);
        home.setTexture(standTile);
        home.setName("homeA");
        addStaticObject(home);

        /* Add players */
        // Team B
        PlayerModel p2 = new PlayerModel(LevelController.p2_position.x, LevelController.p2_position.y, pWidth, pHeight, player2FilmStrip, "b");
        p2.setDensity(MOVABLE_OBJ_DENSITY);
        p2.setFriction(MOVABLE_OBJ_FRICTION);
        p2.setRestitution(MOVABLE_OBJ_RESTITUTION);
        p2.setDrawScale(scale);
//        p2.setMovable(true);

        /* Add home stalls */
        // Team B
        home = new HomeModel(p2.getHomeLoc().x, p2.getHomeLoc().y, 2f, 2f, "b");
        home.setBodyType(BodyDef.BodyType.StaticBody);
        home.setDrawScale(scale);
        home.setTexture(standTile);
        home.setName("homeB");
        addStaticObject(home);
        addDynamicObject(p1);
        addDynamicObject(p2);

        // player list
        player_list = new PlayerModel[] { p1, p2 };

        /* Add items */
        float itemWidth = ItemModel.itemTexture.getRegionWidth() / scale.x;
        float itemHeight = ItemModel.itemTexture.getRegionHeight() / scale.y;
        ItemModel item = new ItemModel(ITEM_START_POSITION.x, ITEM_START_POSITION.y, itemWidth, itemHeight);
        item.setDensity(MOVABLE_OBJ_DENSITY);
        item.setFriction(MOVABLE_OBJ_FRICTION);
        item.setRestitution(MOVABLE_OBJ_RESTITUTION);
        item.setDrawScale(scale);
//        item.setTexture(ItemModel.itemTexture);
//        item.setMovable(true);
        items = new ItemModel[] {item};
        addDynamicObject(item);
    }

    public void setScale(Vector2 scale) {
        setScale(scale.x, scale.y);
    }

    public void setScale(float sx, float sy) {
        scale.set(sx, sy);
        System.out.println(scale);
    }

    public float getHeight() {
        return bounds.height;
    }

    public float getWidth() {
        return bounds.width;
    }

    // TODO: player model refactor
    public PlayerModel[] getPlayers() {
        return player_list;
    }

    public void worldStep(float step, int vel, int posit) {
        world.step(step, vel, posit);
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

    protected void addStaticObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        staticObjects.add(obj);
        obj.activatePhysics(world);
    }

    protected void addDynamicObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        dynamicObjects.add(obj);
        obj.activatePhysics(world);
    }

    public void reset() {
        // TODO: Theoretically reset is just throwing away this WorldModel and remaking it right? This
        // TODO: isn't really necessary
        staticObjects.clear();
        dynamicObjects.clear();
    }

    public void dispose() {
        for (Obstacle obj : getObjects()) {
            obj.deactivatePhysics(world);
        }
        staticObjects.clear();
        dynamicObjects.clear();
        // Honestly this is kind of dumb.
        // We can literally just dereference the WorldModel and all this should go away.
        staticObjects = null;
        dynamicObjects = null;
        world = null;
        scale = null;
    }

}
