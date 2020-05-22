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

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.entity.*;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.*;

// import java.time.Duration;
// import java.time.Instant;

/**
 * Base class for a world-specific controller.
 * <p>
 * <p>
 * A world has its own objects, assets, and input controller.  Thus this is
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 * <p>
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it censures that multiple instances
 * place nicely with the static assets.
 */
public class WorldController implements Screen, InputProcessor {
    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1 / 60.0f;
    /** Number of velocity iterations for the constrain solvers. */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers. */
    public static final int WORLD_POSIT = 2;
    private static String FX_PICKUP_FILE = "audio/pickup.wav";

    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** Queue for adding objects */
    protected PooledList<Obstacle> addQueue = new PooledList<>();
    /** The font for giving messages to the player */
    protected BitmapFont displayFont;
    protected BitmapFont timerFont;
    /** Textures for in-game UI */
    protected TextureRegion timerTexture;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;
    /** Reference to custom Box2D physics world */
    private WorldModel worldModel;
    /** Whether or not this is an active controller */
    private boolean active;
    /** Whether or not debug mode is active */
    private boolean debug;
    /** Path to the level JSON that is currently loaded */
    private String selectedLevelJSON;
    private String levelItemName;
    private int selectedLevelIndex;
    private Vector2 pointWokDir;

    public float screenWidth;
    public float screenHeight;

    private static int GAME_DURATION = 120;  // in seconds
    private long timerStart;  // in nanoseconds
    private long timerEnd;
    private float timeElapsed;

    private boolean tutorialPopup;

    /** Create a new game world */
    protected WorldController() {
        setDebug(false);
        worldModel = new WorldModel();
        debug = false;
        active = false;
        pointWokDir = new Vector2();
        resetTimer();
    }

    public void resetTimer() {
        timeElapsed = 0;
    }

    /** ??? Adds some time to the timeElapsed? This is so freaking jank */
    public void accumTimer(float delta) {
        timeElapsed += delta;
    }

    public void checkTimeOut() {
        if (timeElapsed > GAME_DURATION) {
            worldModel.completeLevel(false);
        }
    }

    /**
     * Returns true if debug mode is active.
     * <p>
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
     * <p>
     * The canvas is shared across all controllers
     */
    public GameCanvas getCanvas() {
        return canvas;
    }

    /**
     * Sets the canvas associated with this controller
     * <p>
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        // worldModel.setScale(canvas.getWidth()/worldModel.getWidth(), canvas.getHeight()/worldModel.getHeight());
        worldModel.setPixelBounds();
    }

    public void setLevel(String selectedLevelJSON, String itemName, int selectedLevelIndex) {
        this.selectedLevelJSON = selectedLevelJSON;
        this.levelItemName = itemName;
        this.selectedLevelIndex = selectedLevelIndex;
    }

    private FireEnemyModel enemy;

    public void populateLevel() {
        // TODO: Add this to the Assets HashMap
        displayFont = Assets.getFont();
        timerFont = new BitmapFont(displayFont.getData(), displayFont.getRegion(), displayFont.usesIntegerPositions());
        timerFont.setColor(Color.BLACK);
        timerFont.getData().setScale(0.8f);
        timerTexture = Assets.getTextureRegion("ui/TimerNew.png");
        GAME_DURATION = LevelController.getInstance().populate(worldModel, selectedLevelJSON, levelItemName);
        worldModel.initializeAI();
    }

    /**
     * Draw the physics objects to the canvas in the following order:
     * <p>
     * 1. Background tiles
     * 2. Box2D objects
     * 3. Decorations
     *
     * @param delta time from last frame
     */
    public void draw(float delta) {
        canvas.clear();
        canvas.begin();

        // Draw background -> brick decorations -> lantern decorations
        worldModel.drawBackground();
        worldModel.drawDecorations(true);
        worldModel.drawDecorations(false);

        // Draw objects
        for (Obstacle obj : worldModel.getObjects()) {
            if (obj.draw) {
                obj.draw(canvas);
            }
        }

        // Draw timer red if one fourth time left
        if (timeElapsed > GAME_DURATION * 3/4) {
            canvas.draw(timerTexture, Color.RED, 0, 0, 20f, canvas.getHeight()-120f, timerTexture.getRegionWidth(), timerTexture.getRegionHeight());
        } else {
            canvas.draw(timerTexture, Color.WHITE, 0, 0, 20f, canvas.getHeight()-120f, timerTexture.getRegionWidth(), timerTexture.getRegionHeight());
        }

        canvas.drawText(secondsToStringTime((int) (GAME_DURATION - timeElapsed)), timerFont, 76f, canvas.getHeight()-49f);

        if (worldModel.isComplete()) {
            if (worldModel.getLevelExitCode() == ExitCodes.LEVEL_PASS) {
                listener.exitScreen(this, ExitCodes.LEVEL_PASS);
            } else {
                listener.exitScreen(this, ExitCodes.LEVEL_FAIL);
            }
        }
        if (debug) {
            canvas.drawText(String.format("fps: %f", 1/delta), displayFont, 800f, canvas.getHeight() - 6 * 5f);
        }
        canvas.end();

        // Draw with rayhandler
        RayHandler rayhandler = worldModel.getRayhandler();
        if (rayhandler != null) {
            rayhandler.render();
        }

        if (debug) {
            canvas.beginDebug();
            for (Obstacle obj : worldModel.getObjects()) {
                obj.drawDebug(canvas);
            }
            // debugGrid();
            worldModel.debugAI(canvas);
            worldModel.debug.drawPathfinding(canvas, worldModel.scale);
            canvas.endDebug();
        }
    }

    /**
     * Draw points at the corners of each tile in the world.
     */
    private void debugGrid() {
        for (int i = 0; i < worldModel.getWidth(); i++) {
            for (int j = 0; j < worldModel.getHeight(); j++) {
                canvas.drawPoint(i * worldModel.scale.x, j * worldModel.scale.y, Color.GREEN);
            }
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
        resetTimer();
    }

    public void reset() {
        // TODO: Reset should basically throw away WorldModel and make a new one
        if (worldModel != null) {
            worldModel.dispose();
        }
        worldModel = new WorldModel();
        worldModel.setPixelBounds();
        CollisionController c = new CollisionController(worldModel);
        worldModel.setContactListener(c);
        worldModel.initLighting(canvas);
        populateLevel();
        resetTimer();
        for (LightSource l : worldModel.getLights()) {
            l.setActive(true);
        }
        // TODO not hardcode this
        tutorialPopup = selectedLevelIndex >= 0 && selectedLevelIndex <= 3;
    }

    /**
     * Returns whether to process the update loop
     * <p>
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        MechanicManager input = MechanicManager.getInstance();
        input.update(); // TODO: do we need bounds and scale?

        // TODO: use listener properly? maybe?
        if (listener == null) {
            return true;
        }
        if (tutorialPopup) {
            tutorialPopup = false;
            listener.exitScreen(this, ExitCodes.TUTORIAL);
            return false;
        }

        // Toggle debug
        if (input.didDebug()) {
            debug = !debug;
        }

        // Handle resets
        if (input.didReset()) {
            listener.exitScreen(this, ExitCodes.SELECT);
            return false;
        }

        if (input.didPause()) {
            listener.exitScreen(this, ExitCodes.PAUSE);
            return false;
        }

        if (input.didExit()) {
            listener.exitScreen(this, ExitCodes.QUIT);
			return false;
		} else if (worldModel.isDone()) {
			listener.exitScreen(this, ExitCodes.SELECT);
			return false;
		}
		return true;
    }

    public void update(float dt) {
        accumTimer(dt);
        checkTimeOut();
        // TODO: Refactor all player movement

        MechanicManager manager = MechanicManager.getInstance();

        // TODO: IMPORTANT: All UPDATE METHODS FOR OBJECTS SHOULD NOT BE CALLED HERE
        // TODO: BUT IN POST UPDATE
        // I love abusing todos so stuff i write is highlighted

        PlayerModel p;
        float playerHorizontal;
        float playerVertical;
        boolean playerDidBoost;
        boolean playerDidThrow;
        Vector2 slideDirection;

        // TODO for refactoring update
        int NUM_PLAYERS = 1;
        for (int i = 0; i < NUM_PLAYERS; i++) {

            playerHorizontal = manager.getVelX(i);
            playerVertical = manager.getVelY(i);
            playerDidBoost = manager.isDashing(i);
            playerDidThrow = manager.isThrowing(i);

            p = worldModel.getPlayers().get(i);

            // if player is sliding, disregard user input
            if (p.isSliding()) {
                p.updateSlidingTexture();
                slideDirection = p.getSlideDirection();
                p.setIX(slideDirection.x);
                p.setIY(slideDirection.y);
                playerDidBoost = false;
                playerDidThrow = false;
                SoundController.getInstance().play("audio/sliding.wav", "audio/sliding.wav", false, Assets.VOLUME);
            } else {
                // update player state
                if (playerVertical != 0 || playerHorizontal != 0) {
                    p.setWalk(dt);
                    if (p.hasItem()) {
                        p.setHoldTexture();
                    }
                    p.updateDirectionState(playerVertical, playerHorizontal);
                } else {
                    p.setStatic();
                }

                // handle player facing left-right
//                if (playerHorizontal != 0 && playerHorizontal != p.getPrevHoriDir()) {
//                    p.flipTexture();
//                }

                // Set player movement impulse
                p.setIX(playerHorizontal);
                p.setIY(playerVertical);
                // If player dashed whiled moving, set boost impulse
                if (playerDidBoost && (playerHorizontal != 0 || playerVertical != 0)) {
                    p.setBoostImpulse(playerHorizontal, playerVertical);
                }
            }

            // update horizontal direction
//            if (playerHorizontal != 0) {
//                p.setPrevHoriDir(playerHorizontal);
//            }

            // Move player
            p.applyImpulse();

            /* Play state */
            if (!p.isAlive()) {
                p.respawn();
                p.setFallingTexture();
                p.resetSliding();
            }
            p.setActive(p.isAlive());

            /* Items */

            /* IF PLAYER GRABS ITEM */
            for (int j = 0; j < worldModel.getNumItems(); j++) {
                ItemModel item = worldModel.getItem(j);
                if (!item.isHeld() && worldModel.getOverlapItem(j) && !item.isDead()) {
                    item.setHeld(p);
                    p.startgrabCooldown();
                    SoundController.getInstance().play(FX_PICKUP_FILE, FX_PICKUP_FILE, false, Assets.VOLUME);
                }
                j++;
            }

            /* IF ITEM IN PLAYER HANDS */
            if (p.hasItem()) {
                float offset = 1;
                for (ItemModel heldItem : p.getItems()) {
                    heldItem.setPosition(p.getX(), p.getY() + offset);
                    offset += 0.6;
                }
            }

            p.setSlideDirection(playerHorizontal, playerVertical);

            if (!KeyboardMap.mouse && manager.isWhack() && !p.hasItem()) {
                // This is also a side effect of the prevHoriDir and how it can't be set to 0
                float x = p.getX() + p.getPrevHoriDir();
                if (p.getVX() > -1 && p.getVX() < 1) {
                    x = p.getX();
                }
                p.swingWok(new Vector2(x, p.getY() + p.getPrevVertDir()),
                        worldModel.getFirecrackers(), worldModel.getEnemies());
            }

            // player updates (for respawn and dash cool down)
            if (KeyboardMap.mouse) {
                pointWokDir.set(Gdx.input.getX() * worldModel.getWidth() / screenWidth, (screenHeight - Gdx.input.getY()) * worldModel.getHeight() / screenHeight);
            } else {
                pointWokDir.set(playerHorizontal, playerVertical);
            }
            p.update(pointWokDir);

            p.playWalkSound();

            // Must always update sound controller!
            SoundController.getInstance().update();
        }

        Vector2 dir = new Vector2(0,0);

        for (HumanoidModel e : worldModel.getEnemies()) {
            p = worldModel.getPlayers().get(0);
            // if (p.isAlive()) {
                if (e instanceof EnemyModel) {
                    dir = ((EnemyModel) e).update(p);
                } else if (e instanceof CrowdUnitModel) {
                    dir = ((CrowdUnitModel) e).getDir();
                }

                int enemyHorizontal = (int) Math.signum(dir.x);
                // handle enemy facing left-right
                if (enemyHorizontal != 0 && enemyHorizontal != e.getPrevHoriDir()) {
                    e.flipTexture();
                }

                if (e.getLinearVelocity().epsilonEquals(Vector2.Zero, 0.2f)) {
                    e.setStaticTexture();
                } else {
                    e.setWalkTexture(dt);
                }


                /* IF ITEM IN ENEMY HANDS */
                if (e.hasItem()) {
                    float offset = 1;
                    for (ItemModel heldItem : e.getItems()) {
                        heldItem.setPosition(e.getX(), e.getY() + offset);
                        offset += 0.6;
                    }
                }

                // update horizontal direction
                if (enemyHorizontal != 0) {
                    e.setPrevHoriDir(enemyHorizontal);
                }

                /* Play state */
                if (!e.isAlive()) {
                    e.respawn();
                    e.setFallingTexture();
                }

                e.setActive(e.isAlive());
            }
        // }

        for (CrowdModel crowd : worldModel.getCrowds()) {
            crowd.update(dt);
        }
    }

    /**
     * Processes physics
     * <p>
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

        RayHandler rayhandler = worldModel.getRayhandler();
        if (rayhandler != null) {
            rayhandler.update();
        }

        Assets.changeMute();
        Assets.changeMute();

        // Turn the physics engine crank.
        worldModel.worldStep(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        // Also update all objects lol
        worldModel.updateAndCullObjects(dt);
    }

    /**
     * Converts the number of seconds remaining to the following String format:
     * M:SS
     */
    public String secondsToStringTime(int seconds) {
        StringBuilder time = new StringBuilder();

        int min = seconds / 60;
        int sec = seconds % 60;

        time.append(min).append(":");
        if (sec < 10) time.append(0);
        if (sec < 0 && min == 0) sec = 0;
        time.append(sec);
        return time.toString();
    }


    /**
     * Called when the Screen is resized.
     * <p>
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // IGNORE FOR NOW
        screenWidth = width;
        screenHeight = height;
    }

    /**
     * Called when the Screen should render itself.
     * <p>
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        // Instant start = Instant.now();
        if (active) {
            if (preUpdate(delta)) {
                update(delta);
                postUpdate(delta);
            }
            draw(delta);
        }
        // timeElapsed += (double) Duration.between(start, Instant.now()).toNanos() / 1000000000;
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub
    }

    /**
     * Called when the Screen is resumed from a paused state.
     * <p>
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
        Gdx.input.setInputProcessor(this);
	}

	/** Input processor methods (for handling mouse click) */

    @Override
    public boolean keyDown(int keycode) { return true; }

    @Override
    public boolean keyUp(int keycode) { return true; }

    @Override
    public boolean keyTyped(char character) { return true; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!KeyboardMap.mouse) return true;
        float clickX = screenX * worldModel.getWidth() / screenWidth;
        float clickY = worldModel.getHeight() - (screenY * worldModel.getHeight() / screenHeight);
        // Swing wok only if player doesn't have an item
        PlayerModel player = worldModel.getPlayers().get(0);
        if (!player.hasItem()) player.swingWok(new Vector2(clickX, clickY), worldModel.getFirecrackers(), worldModel.getEnemies());
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return true; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return true; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return true; }

    @Override
    public boolean scrolled(int amount) { return false; }
}