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
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.ScreenListener;

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
public class WorldController implements Screen {


	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;

	/**
	 * Exit code for advancing to next level
	 */
	public static final int EXIT_NEXT = 1;

	/** PHYSICS ENGINE STEP */

	/** The amount of time for a physics engine step. */
	public static final float WORLD_STEP = 1 / 60.0f;
	/** Number of velocity iterations for the constrain solvers. */
	public static final int WORLD_VELOC = 6;
	/** Number of position iterations for the constrain solvers. */
	public static final int WORLD_POSIT = 2;

	/** GAME PARAMS */

	/** Width of the game world in Box2d units. */
	protected static final float DEFAULT_WIDTH = 32.0f;
	/** Height of the game world in Box2d units. */
	protected static final float DEFAULT_HEIGHT = 18.0f;


	protected static final float DEFAULT_GRAVITY = -4.9f;


	public TextureRegion backgroundTile;
	/**
	 * The font for giving messages to the player
	 */
	protected BitmapFont displayFont;

	/**
	 * Item
	 */
	// protected ItemModel item;
	// protected boolean prevRespawning = false;

	/**
	 * Reference to the game canvas
	 */
	protected GameCanvas canvas;
	/**
	 * Queue for adding objects
	 */
	protected PooledList<Obstacle> addQueue = new PooledList<>();
	/**
	 * Listener that will update the player mode when we are done
	 */
	private ScreenListener listener;
	/**
	 * Whether or not this is an active controller
	 */
	private boolean active;

	/**
	 * Whether or not debug mode is active
	 */
	private boolean debug;

	// TODO for refactoring update
	private int NUM_PLAYERS = 2;
	private PooledList<Vector2> object_list = new PooledList<>();
	private float[] prev_hori_dir = new float[]{-1, -1};

	private WorldModel worldModel;

	// TODO: Fix after item refactor
	private boolean prevRespawning;

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
	// TODO: Remove bounds and gravity parameter
	protected WorldController(Rectangle bounds, Vector2 gravity) {
		setDebug(false);
		worldModel = new WorldModel();
		// TODO: Refactor out collisions to another class?
		debug = false;
		active = false;
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
	// TODO: Remove Parameters
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
		worldModel.setScale(canvas.getWidth()/worldModel.getWidth(), canvas.getHeight()/worldModel.getHeight());
	}

	public void populateLevel() {
		/** Populate asset textures */
		backgroundTile = Assets.GAME_BACKGROUND;
		displayFont = Assets.RETRO_FONT;

		worldModel.setTextures(new TextureRegion[] {Assets.WALL, Assets.STAND, Assets.GAME_BACKGROUND, Assets.GOAL,
				Assets.HOLE, Assets.FISH_ITEM}, new FilmStrip[] {Assets.PLAYER_FILMSTRIPS[0], Assets.PLAYER_FILMSTRIPS[1]});
	    worldModel.populate();
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

		for (Obstacle obj : worldModel.getObjects()) {
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

		if (worldModel.isComplete()) {
			displayFont.setColor(Color.YELLOW);
			canvas.drawTextCentered(worldModel.winner + "VICTORY!", displayFont, 0.0f);
		}

		canvas.end();

		if (debug) {
			canvas.beginDebug();
			for (Obstacle obj : worldModel.getObjects()) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
    }
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
	    worldModel.dispose();
		addQueue.clear();
		addQueue = null;
		canvas = null;
	}

	// TODO: QUEUED OBJECT (Should probably move this to WorldModel)
	/**
	 *
	 * Adds a physics object in to the insertion queue.
	 *
	 * Objects on the queue are added just before collision processing.  We do this to 
	 * control object creation.
	 *
	 * param obj The object to add
	 */
	// public void addQueuedObject(Obstacle obj) {
	// 	assert inBounds(obj) : "Object is not in bounds";
	// 	addQueue.add(obj);
	// }

	/**
	 * Immediately adds the object to the physics world
	 *
	 * param obj The object to add
	 */
	// protected void addObject(Obstacle obj) {
	// 	assert inBounds(obj) : "Object is not in bounds";
	// 	objects.add(obj);
	// 	obj.activatePhysics(world);
	// }

	public void reset() {
		// TODO: Reset should basically throw away WorldModel and make a new one
        worldModel = new WorldModel();
        worldModel.setScale(canvas.getWidth()/worldModel.getWidth(), canvas.getHeight()/worldModel.getHeight());
        CollisionController c = new CollisionController(worldModel);
        worldModel.setContactListener(c);
        // TODO: WHAT
		// Vector2 gravity = new Vector2( world.getGravity() );

		// for(Obstacle obj : objects) {
		// 	obj.deactivatePhysics(world);
		// }
		// objects.clear();
		// addQueue.clear();
		// world.dispose();

		// world = new World(gravity,false);
		// world.setContactListener(this);
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

		if (input.didExit()) {
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		} else if (worldModel.isDone()){
		    // TODO: Bruh i can actually just reset it here
			listener.exitScreen(this, EXIT_NEXT);
			return false;
		}
		return true;
	}

	public void update(float dt) {
		// TODO: Refactor all player movement

		MechanicManager manager = MechanicManager.getInstance(object_list);

		// TODO peer review below
		// TODO: Wait for item refactor
		ItemModel item = worldModel.getItem();
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
			// TODO: player model refactor
			p = worldModel.getPlayers()[i];

			// handle player facing left-right
			if (playerHorizontal != 0 && playerHorizontal != prev_hori_dir[i]) {
				p.playerTexture.flip(true, false);
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
			if (playerHorizontal != 0 || playerVertical != 0) {
				p.setWalk();
			} else {
				p.setStatic();
			}

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

			// player updates (for respawn and dash cool down)
			p.update();

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
		// TODO: ADDQUEUE
		// TODO: Although if we do implement this probably don't do it here and do it in worldmodel
		// Add any objects created by actions
		// while (!addQueue.isEmpty()) {
		// 	addObject(addQueue.poll());
		// }
		
		// Turn the physics engine crank.
		worldModel.worldStep(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);


		// TODO: Maybe move this to WorldController
		// Garbage collect the deleted objects.
		// Note how we use the linked list nodes to delete O(1) in place.
		// This is O(n) without copying.
		Iterator<PooledList<Obstacle>.Entry> iterator = worldModel.objectEntryIter();
		while (iterator.hasNext()) {
			PooledList<Obstacle>.Entry entry = iterator.next();
			Obstacle obj = entry.getValue();
			if (obj.isRemoved()) {
				obj.deactivatePhysics(worldModel.world);
				entry.remove();
			} else {
				// Note that update is called last!
				obj.update(dt);
			}
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