/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.util.XBox360Controller;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
	// Sensitivity for moving crosshair with gameplay
	private static final float GP_ACCELERATE = 1.0f;
	private static final float GP_MAX_SPEED  = 10.0f;
	private static final float GP_THRESHOLD  = 0.01f;
	private static final float DEADZONE = 0.3f;

	/** The singleton instance of the input controller */
	private static InputController theController = null;
	
	/** 
	 * Return the singleton instance of the input controller
	 *
	 * @return the singleton instance of the input controller
	 */
	public static InputController getInstance() {
		if (theController == null) {
			theController = new InputController();
		}
		return theController;
	}
	
	// Fields to manage buttons
	/** Whether the reset button was pressed. */
	private boolean resetPressed;
	private boolean resetPrevious;
	/** Whether the debug toggle was pressed. */
	private boolean debugPressed;
	private boolean debugPrevious;
	/** Whether the exit button was pressed. */
	private boolean exitPressed;
	private boolean exitPrevious;

	// for player A // will refactor next week loool
	/** How much did we move horizontally? */
	private float horizontalA;
	/** How much did we move vertically? */
	private float verticalA;
	/** Whether the boost button was pressed. */
	private boolean boostPressedA;
	private boolean boostPreviousA;

	// for player B
	/** How much did we move horizontally? */
	private float horizontalB;
	/** How much did we move vertically? */
	private float verticalB;
	/** Whether the boost button was pressed. */
	private boolean boostPressedB;
	private boolean boostPreviousB;

	/** The crosshair position (for raddoll) */
	private Vector2 crosshair;
	/** The crosshair cache (for using as a return value) */
	private Vector2 crosscache;
	/** For the gamepad crosshair control */
	private float momentum;
	
	/** An X-Box controller (if it is connected) */
	XBox360Controller xboxA;

	XBox360Controller xboxB;

	/**
	 * Returns the amount of sideways movement.
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement.
	 */
	public float getHorizontalA() { return horizontalA; }

	/**
	 * Returns the amount of vertical movement.
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement.
	 */
	public float getVerticalA() { return verticalA; }

	/**
	 * Returns the amount of sideways movement.
	 *
	 * -1 = left, 1 = right, 0 = still
	 *
	 * @return the amount of sideways movement.
	 */
	public float getHorizontalB() { return horizontalB; }

	/**
	 * Returns the amount of vertical movement.
	 *
	 * -1 = down, 1 = up, 0 = still
	 *
	 * @return the amount of vertical movement.
	 */
	public float getVerticalB() { return verticalB; }

	/**
	 * Returns true if the reset button was pressed.
	 *
	 * @return true if the reset button was pressed.
	 */
	public boolean didReset() { return resetPressed && !resetPrevious; }
	
	/**
	 * Returns true if the player wants to go toggle the debug mode.
	 *
	 * @return true if the player wants to go toggle the debug mode.
	 */
	public boolean didDebug() {
		return debugPressed && !debugPrevious;
	}
	
	/**
	 * Returns true if the exit button was pressed.
	 *
	 * @return true if the exit button was pressed.
	 */
	public boolean didExit() {
		return exitPressed && !exitPrevious;
	}

	public boolean didBoostA() {
		return boostPressedA && !boostPreviousA;
	}

	public boolean didBoostB() {
		return boostPressedB && !boostPreviousB;
	}
	
	/**
	 * Creates a new input controller
	 * 
	 * The input controller attempts to connect to the X-Box controller at device 0,
	 * if it exists.  Otherwise, it falls back to the keyboard control.
	 */
	public InputController() { 
		// If we have a game-pad for id, then use it.
		xboxA = new XBox360Controller(1);
		xboxB = new XBox360Controller(0);
		crosshair = new Vector2();
		crosscache = new Vector2();
	}

	/**
	 * Reads the input for the player and converts the result into game logic.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	public void readInput(Rectangle bounds, Vector2 scale) {
		// Copy state from last animation frame
		// Helps us ignore buttons that are held down
		resetPrevious  = resetPressed;
		debugPrevious  = debugPressed;
		exitPrevious = exitPressed;
		boostPreviousA = boostPressedA;
		boostPreviousB = boostPressedB;
		
		// Check to see if a GamePad is connected
		if (xboxA.isConnected()) {
			readGamepad(bounds, scale, 0);
			readKeyboard(bounds, scale, true, 0); // Read as a back-up
		} else {
			readKeyboard(bounds, scale, false, 0);
		}

		if (xboxB.isConnected()) {
			readGamepad(bounds, scale, 1);
			readKeyboard(bounds, scale, true, 1); // Read as a back-up
		} else {
			readKeyboard(bounds, scale, false, 1);
		}

	}

	/**
	 * Reads input from an X-Box controller connected to this computer.
	 *
	 * The method provides both the input bounds and the drawing scale.  It needs
	 * the drawing scale to convert screen coordinates to world coordinates.  The
	 * bounds are for the crosshair.  They cannot go outside of this zone.
	 *
	 * @param bounds The input bounds for the crosshair.  
	 * @param scale  The drawing scale
	 */
	private void readGamepad(Rectangle bounds, Vector2 scale, int device) {
		if (device == 0) {
			resetPressed = xboxA.getStart();
			exitPressed  = xboxA.getBack();
			debugPressed  = xboxA.getY();
			boostPressedA = xboxA.getB();

			// Increase animation frame, but only if trying to move
			horizontalA = xboxA.getLeftX();
			verticalA   = xboxA.getLeftY();
		}
		else {
			boostPressedB = xboxB.getB();

			horizontalB = xboxB.getLeftX();
			verticalB   = xboxB.getLeftY();
		}

		clampPosition(bounds);
	}


	private boolean notDeadZoned(float vert, float hori) {
		return Math.abs(vert) > DEADZONE || Math.abs(hori) > DEADZONE;
	}

	/**
	 * Reads input from the keyboard.
	 *
	 * This controller reads from the keyboard regardless of whether or not an X-Box
	 * controller is connected.  However, if a controller is connected, this method
	 * gives priority to the X-Box controller.
	 *
	 * @param secondary true if the keyboard should give priority to a gamepad
	 */
	private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary, int device) {
		if (device == 0) {
			// Give priority to gamepad results
			resetPressed = (secondary && resetPressed) || (Gdx.input.isKeyPressed(Input.Keys.R));
			debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.Y));
			exitPressed  = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));

			// FOR PLAYER A

			// Directional controls
			horizontalA = (secondary && notDeadZoned(horizontalA, verticalA) ? horizontalA : 0.0f);
			if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				horizontalA += 1.0f;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				horizontalA -= 1.0f;
			}

			verticalA = (secondary  && notDeadZoned(horizontalA, verticalA) ? verticalA : 0.0f);
			if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
				verticalA += 1.0f;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
				verticalA -= 1.0f;
			}

			// boost
			boostPressedA = (secondary && boostPressedA) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

		} else {
			// FOR PLAYER B
			// Directional controls
			horizontalB = (secondary  && notDeadZoned(horizontalB, verticalB) ? horizontalB : 0.0f);
			if (Gdx.input.isKeyPressed(Input.Keys.D)) {
				horizontalB += 1.0f;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.A)) {
				horizontalB -= 1.0f;
			}

			verticalB = (secondary && notDeadZoned(horizontalB, verticalB) ? verticalB : 0.0f);
			if (Gdx.input.isKeyPressed(Input.Keys.W)) {
				verticalB += 1.0f;
			}
			if (Gdx.input.isKeyPressed(Input.Keys.S)) {
				verticalB -= 1.0f;
			}

			// boost
			boostPressedB = (secondary && boostPressedB) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

		}
		clampPosition(bounds);
	}
	
	/**
	 * Clamp the cursor position so that it does not go outside the window
	 *
	 * While this is not usually a problem with mouse control, this is critical 
	 * for the gamepad controls.
	 */
	private void clampPosition(Rectangle bounds) {
		crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
		crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
	}
}