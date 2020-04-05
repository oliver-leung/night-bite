package edu.cornell.gdiac.nightbite;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.nightbite.entity.ItemModel;
import edu.cornell.gdiac.nightbite.entity.PlayerModel;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.LightSource;
import edu.cornell.gdiac.util.PointSource;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class WorldModel {
    /**
     * Immovable object parameters
     */
    private static final float IMMOVABLE_OBJ_DENSITY = 0f;
    private static final float IMMOVABLE_OBJ_FRICTION = 1f;
    private static final float IMMOVABLE_OBJ_RESTITUTION = 0f;

    /**
     * Player textures
     */
    public static FilmStrip player1FilmStrip;

    /**
     * How many frames after winning/losing do we continue?
     */
    public static final int EXIT_COUNT = 120;
    public static FilmStrip player2FilmStrip;
    /**
     * Item parameters
     */
    protected static Vector2 ITEM_START_POSITION = new Vector2(16, 12);
    public String winner;

    // TODO: This should be data driven
    /**
     * World scale
     */
    public Vector2 scale;

    // TODO: PLEASE remove this eventually
    public Vector2 getITEMSTART() {
        return ITEM_START_POSITION;
    }

    /**
     * World
     */
    protected World world;

    /**
     * Whether we have completed this level
     */
    private boolean complete;
    /**
     * Countdown active for winning or losing
     */
    private int countdown;
    /**
     * The camera defining the RayHandler view; scale is in physics coordinates
     */
    protected OrthographicCamera raycamera;

    /**
     * The rayhandler for storing lights, and drawing them
     */
    protected RayHandler rayhandler;

    /**
     * All of the lights that we loaded from the JSON file
     */
    private Array<LightSource> lights = new Array<LightSource>();

    /**
     * FOR AI
     */
    TextureRegion itemTexture;
    // TODO: Maybe use a better data structure
    private ArrayList<ItemModel> items;
    /**
     * Objects that don't move during updates
     */
    private PooledList<Obstacle> staticObjects;

    private Rectangle bounds;
    /**
     * Objects that move during updates
     */
    private PooledList<Obstacle> dynamicObjects;
    private ArrayList<PlayerModel> player_list;


    // TODO: REMOVE ALL THESE DUMB TEXTURES
    TextureRegion wallTile;
    TextureRegion standTile;
    TextureRegion backgroundTile;
    TextureRegion goalTile;
    TextureRegion holeTile;
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
        player_list = new ArrayList<>();
        items = new ArrayList<>();
    }

    public void setTextures(TextureRegion[] textures, FilmStrip[] filmStrips) {
        wallTile = textures[0];
        standTile = textures[1];
        backgroundTile = textures[2];
        goalTile = textures[3];
        holeTile = textures[4];
        itemTexture = textures[5];
        player1FilmStrip = filmStrips[0];
        player2FilmStrip = filmStrips[1];
    }

    // TODO: END REMOVE ALL THESE DUMB TEXTURES

    // TODO: DO we need addQueue?

    /**
     * Returns a string equivalent to the COMPLEMENT of bits in s
     * <p>
     * This function assumes that s is a string of 0s and 1s of length < 16.
     * This function allows the JSON file to specify exclusion bit arrays (for masking)
     * in a readable format.
     *
     * @param s the string representation of the bit array
     * @return a string equivalent to the COMPLEMENT of bits in s
     */
    public static short bitStringToComplement(String s) {
        short value = 0;
        short pos = 1;
        for (int ii = s.length() - 1; ii >= 0; ii--) {
            if (s.charAt(ii) == '0') {
                value += pos;
            }
            pos *= 2;
        }
        return value;
    }

    /**
     * Returns true if the level is completed.
     * <p>
     * If true, the level will advance after a countdown
     *
     * @return true if the level is completed.
     */
    public boolean isComplete() {
        return complete;
    }

    // TODO: PLEASE fix this
    public ItemModel getItem() {
        return items.get(0);
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

    public boolean isDone() {
        countdown--;
        return countdown <= 0 && complete;
    }


    /**
     * Returns the world's rayhandler.
     */
    public RayHandler getRayhandler() {
        return rayhandler;
    }

    /**
     * Returns the world's lights.
     */
    public Array<LightSource> getLights() {
        return lights;
    }

    // TODO: player model refactor
    public ArrayList<PlayerModel> getPlayers() {
        return player_list;
    }

    public void worldStep(float step, int vel, int posit) {
        world.step(step, vel, posit);
    }

    /**
     * Returns true if the object is in bounds.
     * <p>
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x + bounds.width);
        boolean vert = (bounds.y <= obj.getY() && obj.getY() <= bounds.y + bounds.height);
        return horiz && vert;
    }

    public void addStaticObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        staticObjects.add(obj);
        obj.activatePhysics(world);
    }

    public void addDynamicObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        dynamicObjects.add(obj);
        obj.activatePhysics(world);
    }

    public void addPlayer(PlayerModel player) {
        player_list.add(player);
        addDynamicObject(player);
    }

    // ******************** LIGHTING METHODS ********************

    public void addItem(ItemModel item) {
        items.add(item);
        addDynamicObject(item);
    }

    /**
     * TODO allow passing in of different lighting parameters
     */
    public void initLighting() {
        raycamera = new OrthographicCamera(bounds.width, bounds.height);
        raycamera.position.set(bounds.width / 2.0f, bounds.height / 2.0f, 0);
        raycamera.update();

        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
        rayhandler = new RayHandler(world, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
        rayhandler.setCombinedMatrix(raycamera);

        // All hard coded for now, to be changed with data-driven levels
        float[] color = new float[]{ 0.5f, 0.5f, 0.5f, 1.0f };
        rayhandler.setAmbientLight(color[0], color[0], color[0], color[0]);
        int blur = 2;
        // rayhandler.setBlur(blur > 0);
        rayhandler.setBlur(true);
        rayhandler.setBlurNum(blur);
    }

    /**
     * Creates one point light, which goes in all directions.
     *
     * TODO allow parameters to be passed
     */
    public void createPointLight() {
        // ALL HARDCODED!
        float[] color = new float[]{1.0f, 0.2f, 0.0f, 1.0f};
        float[] pos = new float[]{0.0f, 0.0f};
        float dist = 7.0f;
        int rays = 512;

        PointSource point = new PointSource(rayhandler, rays, Color.WHITE, dist, pos[0], pos[1]);
        point.setColor(color[0], color[1], color[2], color[3]);
        point.setSoft(false);

        // Create a filter to exclude see through items
        Filter f = new Filter();
        f.maskBits = bitStringToComplement("1111"); // controls collision/cast shadows
        point.setContactFilter(f);
        point.setActive(false); // TURN ON LATER
        lights.add(point);
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

        for(LightSource light : lights) {
            light.remove();
        }
        lights.clear();

        if (rayhandler != null) {
            rayhandler.dispose();
            rayhandler = null;
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