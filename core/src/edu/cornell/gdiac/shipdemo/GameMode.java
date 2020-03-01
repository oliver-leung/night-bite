/*
 * GameMode.java
 *
 * This is the primary class file for running the game.  You should study this file for
 * ideas on how to structure your own root class. This class follows a 
 * model-view-controller pattern fairly strictly.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.shipdemo;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Rectangle;
import edu.cornell.gdiac.util.FilmStrip;

/**
 * The primary controller class for the game.
 *
 * While GDXRoot is the root class, it delegates all of the work to the player mode
 * classes. This is the player mode class for running the game. In initializes all 
 * of the other classes in the game and hooks them together.  It also provides the
 * basic game loop (update-draw).
 */
public class GameMode implements ModeController {
	// GRAPHICS AND SOUND RESOURCES
	// Pathnames to texture  and sound assets
	/** The background image for the battle */
	private static final String BACKGROUND_FILE = "images/ice.png";
	/** The image for a single proton */
	private final static String PHOTON_TEXTURE = "images/photon.png";	
	/** Texture for the ship (colored for each player) */
	private static final String SHIP_TEXTURE = "images/ships.png";	
	/** Texture for the target reticule */
	private static final String TARG_TEXTURE = "images/target.png";
	/** The weapon fire sound for the blue player */
	private static final String FUSION_FILE = "sounds/fusion.mp3";
	/** The weapon fire sound for the red player */
	private static final String LASER_FILE  = "sounds/laser.mp3";
	
	/** Number of rows in the ship image filmstrip */
	private static final int SHIP_ROWS = 4;
	/** Number of columns in this ship image filmstrip */
	private static final int SHIP_COLS = 5;
	/** Number of elements in this ship image filmstrip */
	private static final int SHIP_SIZE = 18;
	
	// Asset loading is handled statically, so these are static variables
	/** The background image for the battle */
	private static Texture background;
	/**
	 * The image for a single proton
	 */
	private static Texture photonTexture;
	/**
	 * Texture for the ship (colored for each player)
	 */
	private static Texture shipTexture;
	/**
	 * Texture for the target reticule
	 */
	private static Texture targetTexture;
	/**
	 * The weapon fire sound for the blue player
	 */
	private static Sound blueSound;
	/**
	 * The weapon fire sound for the red player
	 */
	private static Sound redSound;

	private float backgroundAngle = 0;

	/**
	 * Preloads the texture and sound information for the game.
	 *
	 * All instance of the game use the same assets, so this is a static method.  
	 * This keeps us from loading the assets multiple times.
	 *
	 * The asset manager for LibGDX is asynchronous.  That means that you
	 * tell it what to load and then wait while it loads them.  This is 
	 * the first step: telling it what to load.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public static void PreLoadContent(AssetManager manager) {
		manager.load(BACKGROUND_FILE,Texture.class);
		manager.load(PHOTON_TEXTURE,Texture.class);
		manager.load(SHIP_TEXTURE,Texture.class);
		manager.load(TARG_TEXTURE,Texture.class);
		manager.load(FUSION_FILE,Sound.class);
		manager.load(LASER_FILE, Sound.class);
	}

	/** 
	 * Loads the texture information for the ships.
	 * 
	 * All instance of the game use the same assets, so this is a static method.  
	 * This keeps us from loading the assets multiple times.
	 *
	 * The asset manager for LibGDX is asynchronous.  That means that you
	 * tell it what to load and then wait while it loads them.  This is 
	 * the second step: extracting assets from the manager after it has
	 * finished loading them.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public static void LoadContent(AssetManager manager) {
		background    = manager.get(BACKGROUND_FILE, Texture.class);
		photonTexture = manager.get(PHOTON_TEXTURE, Texture.class);
		shipTexture   = manager.get(SHIP_TEXTURE, Texture.class);
		targetTexture = manager.get(TARG_TEXTURE, Texture.class);
		blueSound = manager.get(LASER_FILE,  Sound.class);
		redSound  = manager.get(FUSION_FILE, Sound.class);
		
		// Make the ship content prettier
		shipTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}

	/** 
	 * Unloads the texture information for the ships.
	 * 
	 * This method erases the static variables.  It also deletes the
	 * associated textures from the assert manager.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public static void UnloadContent(AssetManager manager) {
		manager.unload(BACKGROUND_FILE);
		manager.unload(PHOTON_TEXTURE);
		manager.unload(SHIP_TEXTURE);
		manager.unload(TARG_TEXTURE);
		manager.unload(FUSION_FILE);
		manager.unload(LASER_FILE);
	}
	
    // Instance variables
	/** Read input for blue player from keyboard or game pad (CONTROLLER CLASS) */
	protected InputController blueController;
	/** Read input for red player from keyboard or game pad (CONTROLLER CLASS) */
	protected InputController redController;
    /** Handle collision and physics (CONTROLLER CLASS) */
    protected CollisionController physicsController;

	/**
	 * Location and animation information for blue ship (MODEL CLASS)
	 */
	protected Player playerBlue;
	/**
	 * Location and animation information for red ship (MODEL CLASS)
	 */
	protected Player playerRed;
	/**
	 * Shared memory pool for photons. (MODEL CLASS)
	 */
	protected PhotonQueue photons;

	/** Store the bounds to enforce the playing region */	
	private Rectangle bounds;

	/**
	 * Creates a new game with a playing field of the given size.
	 *
	 * This constructor initializes the models and controllers for the game.  The
	 * view has already been initialized by the root class.
	 *
	 * @param width The width of the game window
	 * @param height The height of the game window
	 */
	public GameMode(float width, float height) {
		// Initialize the photons.
		photons = new PhotonQueue();
		photons.setTexture(photonTexture);
		bounds = new Rectangle(0, 0, width, height);

		// Create the two ships and place them across from each other.
		// There are a lot of magic numbers here, but we only use them once.

		// RED PLAYER
		playerRed = new Player(width * (1.0f / 3.0f), height * (1.0f / 2.0f), 0, 81);
		playerRed.setFilmStrip(new FilmStrip(shipTexture, SHIP_ROWS, SHIP_COLS, SHIP_SIZE));
		playerRed.setTargetTexture(targetTexture);
		playerRed.setColor(new Color(1.0f, 0.25f, 0.25f, 1.0f));  // Red, but makes texture easier to see

		// BLUE PLAYER
		playerBlue = new Player(width * (2.0f / 3.0f), height * (1.0f / 2.0f), 180, 200);
		playerBlue.setFilmStrip(new FilmStrip(shipTexture, SHIP_ROWS, SHIP_COLS, SHIP_SIZE));
		playerBlue.setTargetTexture(targetTexture);
		playerBlue.setColor(new Color(0.5f, 0.5f, 1.0f, 1.0f));   // Blue, but makes texture easier to see

		// Create the input controllers.
		redController = new InputController(1);
		blueController = new InputController(0);
		physicsController = new CollisionController();
	}

	/** 
	 * Read user input, calculate physics, and update the models.
	 *
	 * This method is HALF of the basic game loop.  Every graphics frame 
	 * calls the method update() and the method draw().  The method update()
	 * contains all of the calculations for updating the world, such as
	 * checking for collisions, gathering input, and playing audio.  It
	 * should not contain any calls for drawing to the screen.
	 */
	@Override
	public void update() {
		// Read the keyboard for each controller.
		redController.readInput();
		blueController.readInput();

		// Move the photons forward, and add new ones if necessary.
		//photons.move (width,height);
		if (redController.didPressFire() && firePhoton(playerRed, photons)) {
			redSound.play();
		}
		if (blueController.didPressFire() && firePhoton(playerBlue, photons)) {
			blueSound.stop();
			blueSound.play();
		}

		// Move the ships forward (ignoring collisions)
		playerRed.move(redController.getForward(), redController.getTurn());
		playerBlue.move(blueController.getForward(), blueController.getTurn());
		photons.move(bounds);

		// Change the target position.
		playerRed.acquireTarget(playerBlue);
		playerBlue.acquireTarget(playerRed);

		// This call handles BOTH ships.
		physicsController.checkForCollision(playerBlue, playerRed);
		for (int i = photons.head; i <= photons.tail; i++) {
			physicsController.checkForCollision(playerBlue, photons.queue[i]);
			physicsController.checkForCollision(playerRed, photons.queue[i]);
		}
		physicsController.wrapAroundBounds(playerBlue, bounds);
		physicsController.wrapAroundBounds(playerRed, bounds);
	}

	/**
	 * Draw the game on the provided GameCanvas
	 *
	 * There should be no code in this method that alters the game state.  All 
	 * assignments should be to local variables or cache fields only.
	 *
	 * @param canvas The drawing context
	 */
	@Override
	public void draw(GameCanvas canvas) {
		backgroundAngle += 0.25f;
		Affine2 transform = new Affine2();
		transform.setToRotation(backgroundAngle);
		canvas.draw(background, Color.GREEN, transform);

		// First drawing pass (ships + shadows)
		if (redController.getVisible()) {
			playerRed.drawShip(canvas);
		}
		if (blueController.getVisible()) {
			playerBlue.drawShip(canvas);        // Draw Red and Blue ships
		}

		// Second drawing pass (photons)
		canvas.setBlendState(GameCanvas.BlendState.ADDITIVE);
		if (blueController.getVisible() && redController.getVisible()) {
			playerBlue.drawTarget(canvas);  // Draw target
			playerRed.drawTarget(canvas);   // Draw target
		}
		photons.draw(canvas);         // Draw Photons
		canvas.setBlendState(GameCanvas.BlendState.ALPHA_BLEND);
	}

	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		// Garbage collection here is sufficient.  Nothing to do
	}
	
	/**
	 * Resize the window for this player mode to the given dimensions.
	 *
	 * This method is not guaranteed to be called when the player mode
	 * starts.  If the window size is important to the player mode, then
	 * these values should be passed to the constructor at start.
	 *
	 * @param width The width of the game window
	 * @param height The height of the game window
	 */
	public void resize(int width, int height) {
		bounds.set(0, 0, width, height);
	}

	/**
	 * Fires a photon from the ship, adding it to the PhotonQueue.
	 * <p>
	 * This is not inside either PhotonQueue or Ship because it is a relationship
	 * between to objects.  As we will see in class, we do not want to code binary
	 * relationships that way (because it increases dependencies).
	 *
	 * @param player  Ship firing the photon
	 * @param photons PhotonQueue for allocation
	 */
	private boolean firePhoton(Player player, PhotonQueue photons) {
		// Only process if enough time has passed since last.
		if (player.canFireWeapon()) {
			photons.addPhoton(player.getPosition(), player.getVelocity(), player.getAngle(), player.getColor());
			player.reloadWeapon();
			return true;
		}
		return false;
	}
}