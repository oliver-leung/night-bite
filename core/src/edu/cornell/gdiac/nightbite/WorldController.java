/*
 * WorldController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination 
 * of the CollisionController and GameplayController from the previous lab.  There is not 
 * much to do for collisions; Box2d takes care of all of that for us.  This controller 
 * invokes Box2d and then performs any after the fact modifications to the data 
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.nightbite.entity.*;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.ScreenListener;
import org.w3c.dom.Text;

import java.util.Iterator;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller.  Thus this is 
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class WorldController implements Screen, ContactListener {

	public static final int ITEMS_TO_WIN = 3;
	/**
	 * Exit code for quitting the game
	 */
	public static final int EXIT_QUIT = 0;
	/**
	 * Exit code for advancing to next level
	 */
	public static final int EXIT_NEXT = 1;
	/**
	 * Exit code for jumping back to previous level
	 */
	public static final int EXIT_PREV = 2;
	/**
	 * How many frames after winning/losing do we continue?
	 */
	public static final int EXIT_COUNT = 120;
	/**
	 * The amount of time for a physics engine step.
	 */
	public static final float WORLD_STEP = 1 / 60.0f;
	/**
	 * Number of velocity iterations for the constrain solvers
	 */
	public static final int WORLD_VELOC = 6;
	/**
	 * Number of position iterations for the constrain solvers
	 */
	public static final int WORLD_POSIT = 2;
	/**
	 * Width of the game world in Box2d units
	 */
	protected static final float DEFAULT_WIDTH = 32.0f;
	/**
	 * Height of the game world in Box2d units
	 */
	protected static final float DEFAULT_HEIGHT = 18.0f;
	/**
	 * The default value of gravity (going down)
	 */
	protected static final float DEFAULT_GRAVITY = -4.9f;
	protected static final float PUSH_IMPULSE = 200f;
	/**
	 * Density of objects
	 */
	private static final float BASIC_DENSITY = 0.0f;
	/**
	 * Friction of objects
	 */
	private static final float BASIC_FRICTION = 1f;

	// Pathnames to shared assets
	/**
	 * Collision restitution for all objects
	 */
	private static final float BASIC_RESTITUTION = 0f;
	public TextureRegion backgroundTile;
	public TextureRegion standTile;
	public TextureRegion holeTile;
	protected String winner;
	/**
	 * The texture for the exit condition
	 */
	protected TextureRegion goalTile;
	/**
	 * The font for giving messages to the player
	 */
	protected BitmapFont displayFont;
	/**
	 * The texture for walls and platforms
	 */
	protected TextureRegion wallTile;
	/**
	 * Player 1
	 */
	protected PlayerModel p1;
	/**
	 * Player 2
	 */
	protected PlayerModel p2;
	/**
	 * Item
	 */
	protected ItemModel item;

	protected int playerWalkCounter;
	/**
	 * All the objects in the world.
	 */
	protected PooledList<Obstacle> objects = new PooledList<>();
	/**
	 * Track asset loading from all instances and subclasses
	 */
	protected AssetState worldAssetState = AssetState.EMPTY;
	/**
	 * Track all loaded assets (for unloading purposes)
	 */
	protected Array<String> assets;
	/**
	 * Reference to the game canvas
	 */
	protected GameCanvas canvas;
	/**
	 * Queue for adding objects
	 */
	protected PooledList<Obstacle> addQueue = new PooledList<>();
	/**
	 * The Box2D world
	 */
	protected World world;
	/**
	 * The boundary of the world
	 */
	protected Rectangle bounds;
	/**
	 * The world scale
	 */
	protected Vector2 scale;
	/**
	 * Listener that will update the player mode when we are done
	 */
	private ScreenListener listener;
	/**
	 * Whether or not this is an active controller
	 */
	private boolean active;
	/**
	 * Whether we have completed this level
	 */
	private boolean complete;
	/**
	 * Whether we have failed at this world (and need a reset)
	 */
	private boolean failed;
	/**
	 * Whether or not debug mode is active
	 */
	private boolean debug;
	/**
	 * Countdown active for winning or losing
	 */
	private int countdown;

	// TODO for refactoring update
	private int NUM_PLAYERS = 2;
	private PlayerModel[] player_list;
	private PooledList<Vector2> object_list = new PooledList<>();
	private float[] prev_hori_dir = new float[]{-1, -1};

	protected Vector2 item_position = new Vector2(16, 12);

	private static final float MOVABLE_OBJECT_DENSITY = 1.0f;
	private static final float MOVABLE_OBJECT_FRICTION = 0.1f;
	private static final float MOVABLE_OBJECT_RESTITUTION = 0.4f;

	/**
	 * Creates a new game world
	 * <p>
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds  The game bounds in Box2d coordinates
	 * @param gravity The gravitational force on this Box2d world
	 */
	protected WorldController(Rectangle bounds, Vector2 gravity) {
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world = new World(gravity, false);
		world.setContactListener(this);
		world.setGravity(new Vector2(0, 0));
		assets = new Array<>();
		this.bounds = new Rectangle(bounds);
		this.scale = new Vector2(1, 1);
		complete = false;
		failed = false;
		debug = false;
		active = false;
		countdown = -1;
	}

	/**
	 * Creates a new game world with the default values.
	 * <p>
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 */
	protected WorldController() {
		this(new Rectangle(0, 0, DEFAULT_WIDTH, DEFAULT_HEIGHT),
				new Vector2(0, DEFAULT_GRAVITY));
	}

	/**
	 * Creates a new game world
	 * <p>
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param width   The width in Box2d coordinates
	 * @param height  The height in Box2d coordinates
	 * @param gravity The downward gravity
	 */
	protected WorldController(float width, float height, float gravity) {
		this(new Rectangle(0, 0, width, height), new Vector2(0, gravity));
	}

	/**
	 * Returns true if debug mode is active.
	 *
	 * If true, all objects will display their physics bodies.
	 *
	 * @return true if debug mode is active.
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Sets whether debug mode is active.
	 * <p>
	 * If true, all objects will display their physics bodies.
	 *
	 * @param value whether debug mode is active.
	 */
	public void setDebug(boolean value) {
		debug = value;
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

	/**
	 * Sets whether the level is completed.
	 *
	 * If true, the level will advance after a countdown
	 *
	 * @param value whether the level is completed.
	 */
	public void setComplete(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		complete = value;
	}
	
	/**
	 * Returns true if the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @return true if the level is failed.
	 */
	public boolean isFailure( ) {
		return failed;
	}

	/**
	 * Sets whether the level is failed.
	 *
	 * If true, the level will reset after a countdown
	 *
	 * @param value whether the level is failed.
	 */
	public void setFailure(boolean value) {
		if (value) {
			countdown = EXIT_COUNT;
		}
		failed = value;
	}
	
	/**
	 * Returns true if this is the active screen
	 *
	 * @return true if this is the active screen
	 */
	public boolean isActive( ) {
		return active;
	}
	
	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 */
	public GameCanvas getCanvas() {
		return canvas;
	}

	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param canvas the canvas associated with this controller
	 */
	public void setCanvas(GameCanvas canvas) {
		this.canvas = canvas;
		this.scale.x = canvas.getWidth()/bounds.getWidth();
		this.scale.y = canvas.getHeight()/bounds.getHeight();
	}

	/**
	 * Draw the physics objects to the canvas
	 * <p>
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overriden if the world needs fancy backgrounds or the like.
	 * <p>
	 * The method draws all objects in the order that they were added.
	 *
	 * @param delta time from last frame
	 */
	public void draw(float delta) {
		canvas.clear();

		canvas.begin();

		// Draw background
		backgroundTile.setRegionHeight(canvas.getHeight());
		backgroundTile.setRegionWidth(canvas.getWidth());
		canvas.draw(backgroundTile, 0, 0);

		StringBuilder message1 = new StringBuilder("Player A score: ");
		StringBuilder message2 = new StringBuilder("Player B score: ");

		for (Obstacle obj : objects) {
			if (obj.draw) {
				if (obj instanceof HomeModel && obj.getName().equals("homeB")) {
					message1.append(((HomeModel) obj).getScore());
				} else if (obj instanceof HomeModel && obj.getName().equals("homeA")) {
					message2.append(((HomeModel) obj).getScore());
				}
				obj.draw(canvas);
			}
		}

		canvas.drawText(message1.toString(), displayFont, 50.0f, canvas.getHeight() - 6 * 5.0f);
		canvas.drawText(message2.toString(), displayFont, canvas.getWidth() - 200f, canvas.getHeight() - 6 * 5.0f);

		if (complete && !failed) {
			displayFont.setColor(Color.YELLOW);
			canvas.drawTextCentered(winner + "VICTORY!", displayFont, 0.0f);
		} else if (failed) {
			displayFont.setColor(Color.RED);
			canvas.drawTextCentered("FAILURE!", displayFont, 0.0f);
		}
		canvas.end();

		if (debug) {
			canvas.beginDebug();
			for (Obstacle obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
    }
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		objects = null;
		addQueue = null;
		bounds = null;
		scale  = null;
		world  = null;
		canvas = null;
	}

	/**
	 *
	 * Adds a physics object in to the insertion queue.
	 *
	 * Objects on the queue are added just before collision processing.  We do this to 
	 * control object creation.
	 *
	 * param obj The object to add
	 */
	public void addQueuedObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		addQueue.add(obj);
	}

	/**
	 * Immediately adds the object to the physics world
	 *
	 * param obj The object to add
	 */
	protected void addObject(Obstacle obj) {
		assert inBounds(obj) : "Object is not in bounds";
		objects.add(obj);
		obj.activatePhysics(world);
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
		Vector2 gravity = new Vector2( world.getGravity() );

		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();

		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		MechanicManager input = MechanicManager.getInstance(object_list);
		input.update(); // TODO: do we need bounds and scale?

		// TODO: use listener properly? maybe?
		if (listener == null) {
			return true;
		}

		// Toggle debug
		if (input.didDebug()) {
			debug = !debug;
		}
		
		// Handle resets
		if (input.didReset()) {
			reset();
		}
		
		// Now it is time to maybe switch screens.

		// TODO: what is exit?
        // TODO: Actually what is this entire if statement
		if (input.didExit()) {
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		} else if (countdown > 0) {
			countdown--;
		} else if (countdown == 0) {
			if (failed) {
				reset();
			} else if (complete) {
				listener.exitScreen(this, EXIT_NEXT);
				return false;
			}
		}
		return true;
	}

	private void populateLevel() {
		/* populate asset textures */
		holeTile = Assets.HOLE;
		wallTile = Assets.WALL;
		standTile = Assets.STAND;
		backgroundTile = Assets.GAME_BACKGROUND;
		displayFont = Assets.RETRO_FONT;
		PlayerModel.playerTexture = Assets.PLAYER_FILMSTRIPS;
		PlayerModel.player1FilmStrip = Assets.PLAYER_FILMSTRIPS[0];
		PlayerModel.player2FilmStrip = Assets.PLAYER_FILMSTRIPS[1];
		ItemModel.itemTexture = Assets.FISH_ITEM;

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
		addToObjectList(14, 18, 14.5f, 15.5f);

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
		float pWidth = PlayerModel.player1FilmStrip.getRegionWidth() / scale.x;
		float pHeight = PlayerModel.player1FilmStrip.getRegionHeight() / scale.y;
		p1 = new PlayerModel(LevelController.p1_position.x, LevelController.p1_position.y, pWidth, pHeight, "a", 0);
		p1.setDrawScale(scale);
		p1.setTexture(PlayerModel.player1FilmStrip);

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
		item.setDensity(MOVABLE_OBJECT_DENSITY);
		item.setFriction(MOVABLE_OBJECT_FRICTION);
		item.setRestitution(MOVABLE_OBJECT_RESTITUTION);
		item.setDrawScale(scale);
		item.setTexture(ItemModel.itemTexture);
		item.setSensor(false);
		addObject(item);
	}

	public void addToObjectList(float xbottom, float xtop, float ybottom, float ytop) {
		int x_bottom = Math.round(xbottom);
		int x_top = Math.round(xtop);
		int y_bottom = Math.round(ybottom);
		int y_top = Math.round(ytop);

		Vector2 v = new Vector2();
		for (int i = x_bottom; i <= x_top; i++) {
			for (int j = y_bottom; j <= y_top; j++) {
				v.x = i;
				v.y = j;
				object_list.add(v);
			}
		}
	}

	public void update(float dt) {
		// TODO: Refactor all player movement

		MechanicManager manager = MechanicManager.getInstance(object_list);

		// TODO peer review below

		item.update();

		PlayerModel p;
		float playerHorizontal;
		float playerVertical;
		boolean playerDidBoost;
		boolean playerDidThrow;

		for (int i = 0; i < NUM_PLAYERS; i++) {

			playerHorizontal = manager.getVelX(i);
			playerVertical = manager.getVelY(i);
			playerDidBoost = manager.isDashing(i);
			playerDidThrow = manager.isThrowing(i);
			p = player_list[i];

			// handle player facing left-right
			if (playerHorizontal != 0 && playerHorizontal != prev_hori_dir[i]) {
				PlayerModel.playerTexture[i].flip(true, false);
			}

			// update player state // TODO film strip: needs player 1 film strip first
//			if (playerVertical != 0 || playerHorizontal != 0) {
//				p.setWalk();
//				if (playerWalkCounter % 20 == 0) {
//					PlayerModel.player2FilmStrip.setFrame(1);
//				} else if (playerWalkCounter % 20 == 10) {
//					PlayerModel.player2FilmStrip.setFrame(0);
//				}
//				playerWalkCounter++;
//			} else {
//				p.setStatic();
//				playerWalkCounter = 0;
//				PlayerModel.player2FilmStrip.setFrame(0);
//			}
//			if (playerHorizontal != 0 || playerVertical != 0) {
//				p.setWalk();
//			} else {
//				p.setStatic();
//			}

			// Set player movement impulse
			p.setIX(playerHorizontal);
			p.setIY(playerVertical);
			// If player dashed whiled moving, set boost impulse
			if (playerDidBoost && (playerHorizontal != 0 || playerVertical != 0)) {
				p.setBoostImpulse(playerHorizontal, playerVertical);
			}
			p.applyImpulse();

			/* Play state */
			if (!p.isAlive()) { p.respawn(); }
			p.setActive(p.isAlive());

			/* Items */

			/* IF FISH IN PLAYER HANDS */
			if (p.item) {
				item.setPosition(p.getX(), p.getY() + 1f);
			}

			/* IF PLAYER GRABS ITEM */
			// TODO how to make fair (if grab at same time, player 1 advantage)
			if (!item.isHeld() && p.getOverlapItem() && playerDidThrow && item.cooldownOver()) {
				item.setHeld(p);
				item.startCooldown();
			}

			/* IF PLAYER THROWS ITEM */
			if (playerDidThrow && (playerHorizontal != 0 || playerVertical != 0) && p.item && item.cooldownOver()) {

				item.setUnheld();
				item.startCooldown();

				item.throwItem(p.getImpulse());
			}

			// player cooldown (for respawn)
			p.cooldown();

			// update horizontal direction
			if (playerHorizontal != 0) {
				prev_hori_dir[i] = playerHorizontal;
			}
		}
	}
	
	/**
	 * Processes physics
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * physics.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param dt Number of seconds since last animation frame
	 */
	public void postUpdate(float dt) {
		// Add any objects created by actions
		while (!addQueue.isEmpty()) {
			addObject(addQueue.poll());
		}
		
		// Turn the physics engine crank.
		world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
		while (iterator.hasNext()) {
			PooledList<Obstacle>.Entry entry = iterator.next();
			Obstacle obj = entry.getValue();
			if (obj.isRemoved()) {
				obj.deactivatePhysics(world);
				entry.remove();
			} else {
				// Note that update is called last!
				obj.update(dt);
			}
		}
	}

	public void handlePlayerToObjectContact(PlayerModel player, Object object) {

		if (object instanceof HoleModel) {

			// Player-Hole collision
			player.setAlive(false);
			player.draw = false;

			if (player.item) {
				item.setUnheld();
				item.startRespawn();
			}

		} else if (object instanceof BoxObstacle && ((BoxObstacle) object).getName().equals("item")) {

			// Player-Item
			player.setOverlapItem(true);

		} else if (object instanceof HomeModel) {

			// Player-Home
			HomeModel homeObject = (HomeModel) object;
			// If players went to their own home, drop off item and increment score
			// TODO no consequence if try to drop item at opponent's home?
			if (player.getTeam().equals(homeObject.getTeam()) && player.item) {

				homeObject.incrementScore();

				player.resetTexture();

				item.setUnheld();
				item.startRespawn();

				// win condition
				checkWinCondition(homeObject);
			}
		}
	}

	public void checkWinCondition(HomeModel homeObject) {
		if (homeObject.getScore() >= ITEMS_TO_WIN) {
			setComplete(true);
			if (homeObject.getTeam().equals("a")) {
				winner = "PLAYER B ";
			} else if (homeObject.getTeam().equals("b")) {
				winner = "PLAYER A ";
			}
		}
	}

	public void handleItemToObjectContact(ItemModel item, Object object) {
		if (object instanceof HoleModel) {
			PlayerModel p = item.holdingPlayer;
			if (p != null) {
				p.item = false;
			}

			item.setUnheld();
			item.startRespawn();

		} else if ((object instanceof HomeModel) && (item.holdingPlayer == null)) {
			item.setUnheld();
			item.startRespawn();

			// add score
			HomeModel homeObject = (HomeModel) object;
			homeObject.incrementScore();

			// check win condition
			checkWinCondition(homeObject);
		}
	}

	public void beginContact(Contact contact) {
		Object a = contact.getFixtureA().getBody().getUserData();
		Object b = contact.getFixtureB().getBody().getUserData();

		// Player-Object Contact
		if (a instanceof PlayerModel) {
			handlePlayerToObjectContact((PlayerModel) a, b);
		} else if (b instanceof PlayerModel) {
			handlePlayerToObjectContact((PlayerModel) b, a);
		}

		if (a instanceof ItemModel) {
			handleItemToObjectContact((ItemModel) a, b);
		} else if (b instanceof ItemModel) {
			handleItemToObjectContact((ItemModel) b, a);
		}

//		if (a instanceof PlayerModel && b instanceof ItemModel) {
//			contact.setEnabled(true);
//		}
//		if (a instanceof ItemModel && b instanceof PlayerModel) {
//			contact.setEnabled(true);
//		}
	}

	public void endContact(Contact contact) {
		Object a = contact.getFixtureA().getBody().getUserData();
		Object b = contact.getFixtureB().getBody().getUserData();
		if (a instanceof PlayerModel && b instanceof BoxObstacle && ((BoxObstacle) b).getName().equals("item")) {
			((PlayerModel) a).setOverlapItem(false);
		} else if (b instanceof PlayerModel && a instanceof BoxObstacle && ((BoxObstacle) a).getName().equals("item")) {
			((PlayerModel) b).setOverlapItem(false);
		}
	}

	public void postSolve(Contact contact, ContactImpulse impulse) {
		Object a = contact.getFixtureA().getBody().getUserData();
		Object b = contact.getFixtureB().getBody().getUserData();

		// Player-Player Contact
		if (a instanceof PlayerModel && b instanceof PlayerModel) {
			PlayerModel playerA = (PlayerModel) a;
			PlayerModel playerB = (PlayerModel) b;

			Vector2 flyDirection;
			if (playerA.state == PlayerModel.MoveState.RUN &&
					(playerB.state == PlayerModel.MoveState.WALK || playerB.state == PlayerModel.MoveState.STATIC)) {
				flyDirection = playerB.getLinearVelocity().nor();
				playerA.resetBoosting();
				playerB.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerB.getPosition(), true);
			} else if (playerB.state == PlayerModel.MoveState.RUN &&
					(playerA.state == PlayerModel.MoveState.WALK || playerA.state == PlayerModel.MoveState.STATIC)) {
				flyDirection = playerA.getLinearVelocity().nor();
				playerA.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerA.getPosition(), true);
				playerB.resetBoosting();
			} else if (playerB.state == PlayerModel.MoveState.RUN &&
					(playerA.state == PlayerModel.MoveState.RUN)) {
				flyDirection = playerA.getLinearVelocity().nor();
				playerA.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerA.getPosition(), true);
				flyDirection = playerB.getLinearVelocity().nor();
				playerA.resetBoosting();
				playerB.resetBoosting();
				playerB.getBody().applyLinearImpulse(flyDirection.scl(PUSH_IMPULSE), playerB.getPosition(), true);
			}
		}
	}

	public void preSolve(Contact contact, Manifold oldManifold) {
		Object a = contact.getFixtureA().getBody().getUserData();
		Object b = contact.getFixtureB().getBody().getUserData();

		if ((a instanceof PlayerModel && b instanceof ItemModel) || (b instanceof PlayerModel && a instanceof ItemModel)) {
			contact.setEnabled(false);
		}
	}

	/**
	 * Called when the Screen is resized.
	 *
	 * This can happen at any point during a non-paused state but will never happen
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}
	
	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {
		if (active) {
			if (preUpdate(delta)) {
				update(delta); // This is the one that must be defined.
				postUpdate(delta);
			}
			draw(delta);
		}
	}

	/**
	 * Called when the Screen is paused.
	 *
	 * This is usually when it's not active or visible on screen. An Application is
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
		active = true;
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
		active = false;
	}

	/**
	 * Sets the ScreenListener for this mode
	 * <p>
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	/**
	 * Tracks the asset state.  Otherwise subclasses will try to load assets
	 */
	protected enum AssetState {
		/** No assets loaded */
		EMPTY,
		/** Still loading assets */
		LOADING,
		/** Assets are complete */
		COMPLETE
	}

}