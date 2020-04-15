package edu.cornell.gdiac.nightbite;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
     * Width of the game world in Box2d units.
     */
    public static final float DEFAULT_WIDTH = 20f;
    /**
     * Height of the game world in Box2d units.
     */
    public static final float DEFAULT_HEIGHT = 10f;

    // TODO: Should this be here? Maybe this should be defined in Canvas instead
    /**
     * Width of the game world in pixel units.
     */
    public static final float DEFAULT_PIXEL_WIDTH = 1280f;
    /**
     * Height of the game world in units.
     */
    public static final float DEFAULT_PIXEL_HEIGHT = 640f;

    /**
     * The winner of the level.
     */
    public String winner;

    public ArrayList<ItemModel> getItems() {
        return items;
    }

    // TODO: Maybe use a better data structure
    private ArrayList<ItemModel> items;

    /**
     * How many frames after winning/losing do we continue?
     */
    public static final int EXIT_COUNT = 120;

    /**
     * World
     */
    protected World world;

    /** World scale */
    protected Vector2 scale;
    // TODO: should this be a float or v2?
    protected Vector2 actualScale;

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
     * Objects that don't move during updates
     */
    private PooledList<Obstacle> staticObjects;

    public Rectangle getBounds() {
        return bounds;
    }

    private Rectangle bounds;
    /**
     * Objects that move during updates
     */
    private PooledList<Obstacle> dynamicObjects;
    private ArrayList<PlayerModel> player_list;

    public WorldModel() {
        // TODO: We need a contact listener for WorldModel, which means we need to have a CollisionManager
        // Actually technically not true since we can set this stuff in WorldController, but still
        world = new World(Vector2.Zero, false);
        // TODO: CollisionController
        // TODO: Make this data driven
        bounds = new Rectangle(0, 0, 20f, 10f);
        scale = new Vector2(1f, 1f);
        actualScale = new Vector2(1f, 1f);
        dynamicObjects = new PooledList<>();
        staticObjects = new PooledList<>();
        complete = false;
        countdown = -1;
        player_list = new ArrayList<>();
        items = new ArrayList<>();
    }

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
    public ItemModel getItem(int index) {
        return items.get(index);
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
        // System.out.println(scale);
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

    public Vector2 getScale() {
        return scale;
    }

    public Vector2 getActualScale() {
        return actualScale;
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

    /**
     * Transform an object from tile coordinates to canonical world coordinates.
     * <p>
     * Tile coordinates dictate that the bottom left tile is (0, 0), but in order to place an object in world
     * coordinates, it must be centered on (0.5, 0.5).
     *
     * @param obj The obstacle to be transformed
     */
    private void transformTileToWorld(Obstacle obj) {
        Vector2 pos = obj.getPosition();
        pos.x -= 0.5;
        pos.y -= 0.5;
        obj.setPosition(pos);
    }

    public void addStaticObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        transformTileToWorld(obj);
        staticObjects.add(obj);
        obj.activatePhysics(world);
    }

    public void addDynamicObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        transformTileToWorld(obj);
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
    public void initLighting(GameCanvas canvas) {
        // TODO; make all this work with non diagonal scaling
        raycamera = new OrthographicCamera(canvas.getWidth() / scale.x, canvas.getHeight() / scale.y);
        raycamera.position.set(canvas.getWidth() / scale.x / 2, canvas.getHeight() / scale.y / 2, 0);
        raycamera.update();

        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(true);
        rayhandler = new RayHandler(world, canvas.getWidth(), canvas.getWidth());
        rayhandler.setCombinedMatrix(raycamera);

        // See https://www.informit.com/articles/article.aspx?p=1616796&seqNum=5
        rayhandler.diffuseBlendFunc.set(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // All hard coded for now, to be changed with data-driven levels
        float[] color = new float[]{ 0.0f, 0.0f, 0.0f, 1.0f };
        rayhandler.setAmbientLight(color[0], color[1], color[2], color[3]);
        int blur = 2;
        rayhandler.setBlur(blur > 0);
        rayhandler.setBlurNum(blur);
    }

    /**
     * Creates one point light, which goes in all directions.
     *
     * @param color The rgba value of the light color.
     * @param dist The radius of the light.
     */
    public void createPointLight(float[] color, float dist) {
        // ALL HARDCODED!
        float[] c = color;
        float[] pos = new float[]{0.0f, 0.0f};
        float d = dist;
        int rays = 512;

        PointSource point = new PointSource(rayhandler, rays, Color.WHITE, d, pos[0], pos[1]);
        point.setColor(c[0], c[1], c[2], c[3]);
        point.setSoft(true);

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

    // TODO: This is experimental scaling code

    // This padding might be approximate lol tbh
    private static final float PADDING = 0f;

    public void setPixelBounds(GameCanvas canvas) {
        // TODO: Optimizations; only perform this calculation if the canvas size has changed or something

        // The whole point is that if the canvas is DEFAULT_PIXEL_WIDTH x DEFAULT_PIXEL_HEIGHT and
        // the world is DEFAULT_WIDTH x DEFAULT_HEIGHT, everything is unscaled.
        // These are called the canonical pixel space and canonical world space respectively.

        // scaleWorld translates the levelspace to canonical world space
        // (32 x 18, or otherwise indicated in WorldController)
        float scaleWorldX = DEFAULT_WIDTH / bounds.width;
        float scaleWorldY = DEFAULT_HEIGHT / bounds.height;

        // World2Pixel translates from canonical world space to canonical pixel space
        // Assumes the ratio from DEFAULT_HEIGHT and DEFAULT_PIXEL_HEIGHT is the same as the ratio from
        // DEFAULT_WIDTH and DEFAULT_PIXEL_WIDTH
        float world2Pixel =  DEFAULT_PIXEL_HEIGHT / DEFAULT_HEIGHT;
        System.out.println(world2Pixel);

        // scalePixel translate canonical pixel space to pixel space
        // (1920 x 1080, or otherwise indicated in WorldController)
        float scalePixelX = canvas.getWidth() / (DEFAULT_PIXEL_WIDTH - 2 * PADDING);
        float scalePixelY = canvas.getHeight() / (DEFAULT_PIXEL_HEIGHT - 2 * PADDING);

        // Take the smaller scale so that we only scale diagonally or something
        // This is for asset scaling
        float finalAssetScale = Math.min(scaleWorldX * scalePixelX, scaleWorldY * scalePixelY);
        // This is for converting things to pixel space?
        float finalPosScale = finalAssetScale * world2Pixel;

        scale.set(finalPosScale, finalPosScale);
        actualScale.set(finalAssetScale, finalAssetScale);

        System.out.println(scale);
        System.out.println(actualScale);

        // pixTransform = new Affine2();
        // pixTransform.scale(finalPosScale, finalPosScale);

        // Vector2 pixelBoundsSize = new Vector2(bounds.width, bounds.height);
        // pixTransform.applyTo(pixelBoundsSize);

        // System.out.println(pixelBoundsSize);
        // System.out.println(canvas.getWidth());
        // System.out.println((canvas.getWidth() - pixelBoundsSize.x) / 2);

        // pixTransform.translate((canvas.getWidth() - pixelBoundsSize.x) / 2, (canvas.getHeight() - pixelBoundsSize.y) / 2);

    }

}
