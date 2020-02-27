/* 
 * XBoxController.java
 *
 * Input handler for XBox 360 controller
 * 
 * While LibGDX has game controller support, it only support Ouya (really?)
 * out of the box.  For everything else, you must know exactly what numbers
 * map to what buttons.  Fortunately, the internet is good at finding these
 * things out for you.
 *
 * This class also shows another one of the hazards of cross-platform support.
 * While XBox controller support is (largely) built into Windows, it requires
 * a third party driver for Mac OS X.  And that driver has different button
 * mappings than the Windows driver.  So this class has to determine which OS
 * it is running on in order to work properly.
 *
 * Mac OS X driver support is provided by Colin Munro (updated by RodrigoCard):
 *
 * https://github.com/360Controller/360Controller/releases
 *
 * For XBox One S controllers and newer, we also support direct bluetooth connections.
 * However LibGDX apparently cannot recognize the triggers when you use this instead
 * of the 360Driver (which only allows wired connections).
 *
 * We have moved this class to a util package so that you do not waste time looking
 * at the code.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 5/4/2018
 */
package edu.cornell.gdiac.util;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.controllers.*;

/**
 * Class to support an XBox 360 controller
 * 
 * This is a wrapper class, which wraps around a Controller object to provide
 * concrete mappings for the buttons, joysticks and triggers.  It is simpler than
 * having to remember the exact device numbers (even as constants) for each button.
 * It is particularly important because different operating systems have different
 * mappings for the buttons.
 *
 * Each controller must have its own instance.  The constructor automatically 
 * determines what OS this controller is running on before assigning the mappings.
 * The constructor DOES NOT verify that the controller is indeed an XBox 360 
 * controller.
 */
public class XBoxController implements ControllerListener {
	/** The controller id number */
	private int deviceid;
	/** Reference to base controller object wrapped by this instance. */
	private XBoxInterface wrapper;
	/** Reference to base controller object wrapped by this instance. */
	private Controller controller;

	/**
	 * An interface for the various XBox button mappings
	 *
	 * XBox controller button mappings are both OS and driver specific.  This interface allows us
	 * to abstract these into a common interface.
	 */
	private interface XBoxInterface {
		/**
		 * Returns true if the start button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the start button is currently pressed
		 */
		public boolean getStart();

		/**
		 * Returns true if the back button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the back button is currently pressed
		 */
		public boolean getBack();

		/**
		 * Returns true if the X button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the X button is currently pressed
		 */
		public boolean getX();

		/**
		 * Returns true if the Y button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the Y button is currently pressed
		 */
		public boolean getY();

		/**
		 * Returns true if the A button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the A button is currently pressed
		 */
		public boolean getA();

		/**
		 * Returns true if the B button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the Y button is currently pressed
		 */
		public boolean getB();

		/**
		 * Returns true if the left bumper is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the left bumper is currently pressed
		 */
		public boolean getLB();

		/**
		 * Returns true if the left analog stick is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the left analog stick is currently pressed
		 */
		public boolean getL3();

		/**
		 * Returns true if the right bumper is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the right bumper is currently pressed
		 */
		public boolean getRB();

		/**
		 * Returns true if the right analog stick is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the right analog stick is currently pressed
		 */
		public boolean getR3();

		/**
		 * Returns true if the DPad Up button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Up button is currently pressed
		 */
		public boolean getDPadUp();

		/**
		 * Returns true if the DPad Down button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Down button is currently pressed
		 */
		public boolean getDPadDown();

		/**
		 * Returns true if the DPad Left button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Left button is currently pressed
		 */
		public boolean getDPadLeft();

		/**
		 * Returns true if the DPad Right button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Right button is currently pressed
		 */
		public boolean getDPadRight();

		/**
		 * Returns the current direction of the DPad
		 *
		 * The result will be one of the eight cardinal directions, or
		 * center if the DPad is not actively pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the current direction of the DPad
		 */
		public PovDirection getDPadDirection();

		/**
		 * Returns the X axis value of the left analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is to the left.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the X axis value of the left analog stick.
		 */
		public float getLeftX();

		/**
		 * Returns the Y axis value of the left analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is towards the bottom.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the Y axis value of the left analog stick.
		 */
		public float getLeftY();

		/**
		 * Returns the value of the left trigger.
		 *
		 * This is a value between 0 and 1, where 0 is no pressure.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the value of the left trigger.
		 */
		public float getLeftTrigger();

		/**
		 * Returns the X axis value of the right analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is to the left.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the X axis value of the right analog stick.
		 */
		public float getRightX();

		/**
		 * Returns the Y axis value of the right analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is towards the bottom.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the Y axis value of the right analog stick.
		 */
		public float getRightY();

		/**
		 * Returns the value of the right trigger.
		 *
		 * This is a value between 0 and 1, where 0 is no pressure.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the value of the right trigger.
		 */
		public float getRightTrigger();
	}

	/**
	 * Button mappings for Windows 10
	 *
	 * These button mappings are the built in mappings for Windows 10.  No special driver 
	 * support is necessary.
	 */
	private class XBoxWindowsController implements XBoxInterface {
		/** Reference to base controller object wrapped by this instance. */
		private Controller controller;

		/** Button identifier for the X-Button */
		private int button_x = 2;
		/** Button identifier for the Y-Button */
		private int button_y = 3;
		/** Button identifier for the A-Button */
		private int button_a = 0;
		/** Button identifier for the B-Button */
		private int button_b = 1;

		/** Button identifier for the Back Button */
		private int button_back  = 6;
		/** Button identifier for the Start Button */
		private int button_start = 7;

		/** Button identifier for the left bumper */
		private int button_lb = 4;
		/** Button identifier for the left analog stick */
		private int button_l3 = 8;
		/** Button identifier for the right bumper */
		private int button_rb = 5;
		/** Button identifier for the right analog stick */
		private int button_r3 = 9;

		/** POV identifier for the DPad */
		private int pov_index_dpad = 0;

		/** Axis identifier for left analog stick x-axis */
		private int axis_left_x = 1;
		/** Axis identifier for left analog stick y-axis */
		private int axis_left_y = 0;


		/** Axis identifier for right analog stick x-axis */
		private int axis_right_x = 3;
		/** Axis identifier for right analog stick y-axis */
		private int axis_right_y = 2;

		/** Axis for unified triggers (Windows is weird) */
		private int axis_trigger = 4;

		/** Trigger bug work around for Windows driver */
		private boolean axis_adjust  = false;

		/** OS X driver calibration errors */
		final private float WIN_EDGE_ERROR = 1-1.0f/(float)257;
		final private float WIN_ZERO_ERROR = -1.0f/(float)65536;

		/**
		 * Creates a new XBox interface for the given controller
		 *
		 * @param controller The controller to wrap
		 */
		public XBoxWindowsController(Controller controller) {
			this.controller = controller;
		}

		/**
		 * Returns true if the start button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the start button is currently pressed
		 */
		public boolean getStart()  {
			return controller.getButton(button_start);
		}

		/**
		 * Returns true if the back button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the back button is currently pressed
		 */
		public boolean getBack()  {
			return controller.getButton(button_back);
		}

		/**
		 * Returns true if the X button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the X button is currently pressed
		 */
		public boolean getX()   {
			return controller.getButton(button_x);
		}

		/**
		 * Returns true if the Y button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the Y button is currently pressed
		 */
		public boolean getY()   {
			return controller.getButton(button_y);
		}

		/**
		 * Returns true if the A button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the A button is currently pressed
		 */
		public boolean getA()   {
			return controller.getButton(button_a);
		}

		/**
		 * Returns true if the B button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the Y button is currently pressed
		 */
		public boolean getB()   {
			return controller.getButton(button_b);
		}

		/**
		 * Returns true if the left bumper is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the left bumper is currently pressed
		 */
		public boolean getLB()   {
			return controller.getButton(button_lb);
		}

		/**
		 * Returns true if the left analog stick is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the left analog stick is currently pressed
		 */
		public boolean getL3()   {
			return controller.getButton(button_l3);
		}

		/**
		 * Returns true if the right bumper is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the right bumper is currently pressed
		 */
		public boolean getRB()   {
			return controller.getButton(button_rb);
		}

		/**
		 * Returns true if the right analog stick is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the right analog stick is currently pressed
		 */
		public boolean getR3()   {
			return controller.getButton(button_r3);
		}

		/**
		 * Returns true if the DPad Up button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Up button is currently pressed
		 */
		public boolean getDPadUp() { return controller.getPov(pov_index_dpad) == PovDirection.north; }


		/**
		 * Returns true if the DPad Down button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Down button is currently pressed
		 */
		public boolean getDPadDown() { return controller.getPov(pov_index_dpad) == PovDirection.south; }

		/**
		 * Returns true if the DPad Left button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Left button is currently pressed
		 */
		public boolean getDPadLeft() { return controller.getPov(pov_index_dpad) == PovDirection.west; }

		/**
		 * Returns true if the DPad Right button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Right button is currently pressed
		 */
		public boolean getDPadRight() { return controller.getPov(pov_index_dpad) == PovDirection.east; }

		/**
		 * Returns the current direction of the DPad
		 *
		 * The result will be one of the eight cardinal directions, or
		 * center if the DPad is not actively pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the current direction of the DPad
		 */
		public PovDirection getDPadDirection() { return controller.getPov(pov_index_dpad); }

		/**
		 * Returns the X axis value of the left analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is to the left.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the X axis value of the left analog stick.
		 */
		public float getLeftX() { return controller.getAxis(axis_left_x); }

		/**
		 * Returns the Y axis value of the left analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is towards the bottom.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the Y axis value of the left analog stick.
		 */
		public float getLeftY() { return controller.getAxis(axis_left_y); }

		/**
		 * Returns the value of the left trigger.
		 *
		 * This is a value between 0 and 1, where 0 is no pressure.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the value of the left trigger.
		 */
		public float getLeftTrigger() {
			float value = controller.getAxis(axis_trigger);
			if (value != 0) {
				axis_adjust = true;
			}
			if (axis_adjust) {
				value += WIN_ZERO_ERROR;
				value /= WIN_EDGE_ERROR;
			}
			value = value <= 0.0f ? 0.0f : value;
			return value;
		}

		/**
		 * Returns the X axis value of the right analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is to the left.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the X axis value of the right analog stick.
		 */
		public float getRightX()  { return controller.getAxis(axis_right_x); }

		/**
		 * Returns the Y axis value of the right analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is towards the bottom.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the Y axis value of the right analog stick.
		 */
		public float getRightY() { return controller.getAxis(axis_right_y); }

		/**
		 * Returns the value of the right trigger.
		 *
		 * This is a value between 0 and 1, where 0 is no pressure.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the value of the right trigger.
		 */
		public float getRightTrigger()  {
			float value = controller.getAxis(axis_trigger);
			if (value != 0) {
				axis_adjust = true;
			}
			if (axis_adjust) {
				value += WIN_ZERO_ERROR;
				value /= WIN_EDGE_ERROR;
			}
			value = value >= 0.0f ? 0.0f : -value;
			return value;
		}
	}

	/**
	 * Button mappings for 360Controller on macOS
	 *
	 * This class provides the mappings for the 360Controller driver:
	 *
	 * https://github.com/360Controller/360Controller/releases
	 *
	 * This driver has known idiosyncracies and requires special button support and 
	 * calibration.
	 */
	private class XBoxMacOSWiredController implements XBoxInterface {
		/** Reference to base controller object wrapped by this instance. */
		private Controller controller;

		/** Button identifier for the X-Button */
		private int button_x = 13;
		/** Button identifier for the Y-Button */
		private int button_y = 14;
		/** Button identifier for the A-Button */
		private int button_a = 11;
		/** Button identifier for the B-Button */
		private int button_b = 12;

		/** Button identifier for the Back Button */
		private int button_back  = 5;
		/** Button identifier for the Start Button */
		private int button_start = 4;

		/** Button identifier for the left bumper */
		private int button_lb = 8;
		/** Button identifier for the left analog stick */
		private int button_l3 = 6;
		/** Button identifier for the right bumper */
		private int button_rb = 9;
		/** Button identifier for the right analog stick */
		private int button_r3 = 7;

		/** Button identifier for DPad Up button (OS X driver only) */
		private int button_dpad_up    = 0;
		/** Button identifier for DPad Down button (OS X driver only) */
		private int button_dpad_down  = 1;
		/** Button identifier for DPad Right button (OS X driver only) */
		private int button_dpad_right = 2;
		/** Button identifier for DPad Left button (OS X driver only) */
		private int button_dpad_left  = 3;

		/** Axis identifier for left analog stick x-axis */
		private int axis_left_x = 2;
		/** Axis identifier for left analog stick y-axis */
		private int axis_left_y = 3;
		/** Axis identifier for left trigger */
		private int axis_left_trigger = 0;

		/** Axis identifier for right analog stick x-axis */
		private int axis_right_x = 4;
		/** Axis identifier for right analog stick y-axis */
		private int axis_right_y = 5;
		/** Axis identifier for right trigger */
		private int axis_right_trigger = 1;

		/** Trigger bug work around for OS X driver */
		private boolean axis_left_adjust  = false;
		private boolean axis_right_adjust = false;

		/** OS X driver calibration errors */
		final private float MACOS_LO_ERROR = 1-1.0f/(float)16383;
		final private float MACOS_HI_ERROR = 1+1.0f/(float)32787;

		/**
		 * Creates a new XBox interface for the given controller
		 *
		 * @param controller The controller to wrap
		 */
		public XBoxMacOSWiredController(Controller controller) {
			this.controller = controller;
		}

		/**
		 * Returns true if the start button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the start button is currently pressed
		 */
		public boolean getStart()  {
			return controller.getButton(button_start);
		}

		/**
		 * Returns true if the back button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the back button is currently pressed
		 */
		public boolean getBack()  {
			return controller.getButton(button_back);
		}

		/**
		 * Returns true if the X button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the X button is currently pressed
		 */
		public boolean getX()   {
			return controller.getButton(button_x);
		}

		/**
		 * Returns true if the Y button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the Y button is currently pressed
		 */
		public boolean getY()   {
			return controller.getButton(button_y);
		}

		/**
		 * Returns true if the A button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the A button is currently pressed
		 */
		public boolean getA()   {
			return controller.getButton(button_a);
		}

		/**
		 * Returns true if the B button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the Y button is currently pressed
		 */
		public boolean getB()   {
			return controller.getButton(button_b);
		}

		/**
		 * Returns true if the left bumper is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the left bumper is currently pressed
		 */
		public boolean getLB()   {
			return controller.getButton(button_lb);
		}

		/**
		 * Returns true if the left analog stick is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the left analog stick is currently pressed
		 */
		public boolean getL3()   {
			return controller.getButton(button_l3);
		}

		/**
		 * Returns true if the right bumper is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the right bumper is currently pressed
		 */
		public boolean getRB()   {
			return controller.getButton(button_rb);
		}

		/**
		 * Returns true if the right analog stick is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the right analog stick is currently pressed
		 */
		public boolean getR3()   {
			return controller.getButton(button_r3);
		}

		/**
		 * Returns true if the DPad Up button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Up button is currently pressed
		 */
		public boolean getDPadUp() {
			return controller.getButton(button_dpad_up) &&
					!controller.getButton(button_dpad_left) &&
					!controller.getButton(button_dpad_right);
		}

		/**
		 * Returns true if the DPad Down button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Down button is currently pressed
		 */
		public boolean getDPadDown() {
			return controller.getButton(button_dpad_down) &&
					!controller.getButton(button_dpad_left) &&
					!controller.getButton(button_dpad_right);
		}

		/**
		 * Returns true if the DPad Left button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Left button is currently pressed
		 */
		public boolean getDPadLeft() {
			return controller.getButton(button_dpad_left) &&
					!controller.getButton(button_dpad_up) &&
					!controller.getButton(button_dpad_down);
		}

		/**
		 * Returns true if the DPad Right button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Right button is currently pressed
		 */
		public boolean getDPadRight() {
			return controller.getButton(button_dpad_right) &&
					!controller.getButton(button_dpad_up) &&
					!controller.getButton(button_dpad_down);
		}

		/**
		 * Returns the current direction of the DPad
		 *
		 * The result will be one of the eight cardinal directions, or
		 * center if the DPad is not actively pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the current direction of the DPad
		 */
		public PovDirection getDPadDirection() {
			if (controller.getButton( button_dpad_up )) {
				if (controller.getButton( button_dpad_left )) {
					return PovDirection.northWest;
				} else if (controller.getButton( button_dpad_right )) {
					return PovDirection.northEast;
				}
				return PovDirection.north;
			} else if (controller.getButton( button_dpad_down )) {
				if (controller.getButton( button_dpad_left )) {
					return PovDirection.southWest;
				} else if (controller.getButton( button_dpad_right )) {
					return PovDirection.southEast;
				}
				return PovDirection.south;
			} else if (controller.getButton( button_dpad_left )) {
				return PovDirection.west;
			} else if (controller.getButton( button_dpad_right )) {
				return PovDirection.east;
			}
			return PovDirection.center;
		}

		/**
		 * Returns the X axis value of the left analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is to the left.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the X axis value of the left analog stick.
		 */
		public float getLeftX() { return controller.getAxis(axis_left_x); }

		/**
		 * Returns the Y axis value of the left analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is towards the bottom.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the Y axis value of the left analog stick.
		 */
		public float getLeftY() { return controller.getAxis(axis_left_y); }

		/**
		 * Returns the value of the left trigger.
		 *
		 * This is a value between 0 and 1, where 0 is no pressure.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the value of the left trigger.
		 */
		public float getLeftTrigger() {
			float value = controller.getAxis(axis_left_trigger);
			// A bug in the OS X XBox driver requires this
			if (value > 0.0f) {
				axis_left_adjust = true;
			}
			if (axis_left_adjust) {
				value = (value + MACOS_LO_ERROR)/(MACOS_HI_ERROR+MACOS_LO_ERROR);
			}
			return value;
		}

		/**
		 * Returns the X axis value of the right analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is to the left.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the X axis value of the right analog stick.
		 */
		public float getRightX()  { return controller.getAxis(axis_right_x); }

		/**
		 * Returns the Y axis value of the right analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is towards the bottom.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the Y axis value of the right analog stick.
		 */
		public float getRightY() { return controller.getAxis(axis_right_y); }

		/**
		 * Returns the value of the right trigger.
		 *
		 * This is a value between 0 and 1, where 0 is no pressure.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the value of the right trigger.
		 */
		public float getRightTrigger()  {
			float value = controller.getAxis(axis_right_trigger);
			// A bug in the OS X XBox driver requires this
			if (value > 0.0f) {
				axis_right_adjust = true;
			}
			if (axis_right_adjust) {
				value = (value + MACOS_LO_ERROR)/(MACOS_HI_ERROR+MACOS_LO_ERROR);
			}
			return value;
		}
	}

	/**
	 * Button mappings for a Mac bluetooth connected XBox controller
	 *
	 * This is only supported for XBox One S controllers and newer. Everything seems to 
	 * work EXCEPT for triggers and the back button.  Nothing is recognized when those
	 * buttons are pressed (this appears to be an OS issue).
	 */
	private class XBoxMacOSBluetoothController implements XBoxInterface {
		/** Reference to base controller object wrapped by this instance. */
		private Controller controller;

		/** Button identifier for the X-Button */
		private int button_x = 3;
		/** Button identifier for the Y-Button */
		private int button_y = 4;
		/** Button identifier for the A-Button */
		private int button_a = 0;
		/** Button identifier for the B-Button */
		private int button_b = 1;

		/** Button identifier for the Start Button */
		private int button_start = 11;

		/** Button identifier for the left bumper */
		private int button_lb = 6;
		/** Button identifier for the left analog stick */
		private int button_l3 = 13;
		/** Button identifier for the right bumper */
		private int button_rb = 7;
		/** Button identifier for the right analog stick */
		private int button_r3 = 14;

		/** Button identifier for DPad Up button (OS X driver only) */
		private int button_dpad_up    = 15;
		/** Button identifier for DPad Down button (OS X driver only) */
		private int button_dpad_down  = 16;
		/** Button identifier for DPad Right button (OS X driver only) */
		private int button_dpad_right = 17;
		/** Button identifier for DPad Left button (OS X driver only) */
		private int button_dpad_left  = 18;

		/** Axis identifier for left analog stick x-axis */
		private int axis_left_x = 0;
		/** Axis identifier for left analog stick y-axis */
		private int axis_left_y = 1;
		/** Axis identifier for left trigger */
		private int axis_left_trigger = 4;

		/** Axis identifier for right analog stick x-axis */
		private int axis_right_x = 2;
		/** Axis identifier for right analog stick y-axis */
		private int axis_right_y = 3;
		/** Axis identifier for right trigger */
		private int axis_right_trigger = 5;

		/**
		 * Creates a new XBox interface for the given controller
		 *
		 * @param controller The controller to wrap
		 */
		public XBoxMacOSBluetoothController(Controller controller) {
			this.controller = controller;
		}

		/**
		 * Returns true if the start button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the start button is currently pressed
		 */
		public boolean getStart()  {
			return controller.getButton(button_start);
		}

		/**
		 * Returns true if the back button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the back button is currently pressed
		 */
		public boolean getBack()  {
			return false;
		}

		/**
		 * Returns true if the X button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the X button is currently pressed
		 */
		public boolean getX()   {
			return controller.getButton(button_x);
		}

		/**
		 * Returns true if the Y button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the Y button is currently pressed
		 */
		public boolean getY()   {
			return controller.getButton(button_y);
		}

		/**
		 * Returns true if the A button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the A button is currently pressed
		 */
		public boolean getA()   {
			return controller.getButton(button_a);
		}

		/**
		 * Returns true if the B button is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the Y button is currently pressed
		 */
		public boolean getB()   {
			return controller.getButton(button_b);
		}

		/**
		 * Returns true if the left bumper is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the left bumper is currently pressed
		 */
		public boolean getLB()   {
			return controller.getButton(button_lb);
		}

		/**
		 * Returns true if the left analog stick is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the left analog stick is currently pressed
		 */
		public boolean getL3()   {
			return controller.getButton(button_l3);
		}

		/**
		 * Returns true if the right bumper is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the right bumper is currently pressed
		 */
		public boolean getRB()   {
			return controller.getButton(button_rb);
		}

		/**
		 * Returns true if the right analog stick is currently pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the right analog stick is currently pressed
		 */
		public boolean getR3()   {
			return controller.getButton(button_r3);
		}

		/**
		 * Returns true if the DPad Up button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Up button is currently pressed
		 */
		public boolean getDPadUp() {
			return controller.getButton( button_dpad_up ) &&
					!controller.getButton( button_dpad_left ) &&
					!controller.getButton( button_dpad_right );
		}

		/**
		 * Returns true if the DPad Down button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Down button is currently pressed
		 */
		public boolean getDPadDown() {
			return controller.getButton( button_dpad_down ) &&
					!controller.getButton( button_dpad_left ) &&
					!controller.getButton( button_dpad_right );
		}

		/**
		 * Returns true if the DPad Left button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Left button is currently pressed
		 */
		public boolean getDPadLeft() {
			return controller.getButton(button_dpad_left) &&
					!controller.getButton(button_dpad_up) &&
					!controller.getButton(button_dpad_down);
		}

		/**
		 * Returns true if the DPad Right button currently pressed.
		 *
		 * This is method only returns true if the up button is pressed
		 * by itself.  If the up button is combined with left or right,
		 * it will return false.  For more flexible usage of the DPad,
		 * you should use the method getDPadDirection().
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return true if the DPad Right button is currently pressed
		 */
		public boolean getDPadRight() {
			return controller.getButton( button_dpad_right ) &&
					!controller.getButton( button_dpad_up ) &&
					!controller.getButton( button_dpad_down );
		}

		/**
		 * Returns the current direction of the DPad
		 *
		 * The result will be one of the eight cardinal directions, or
		 * center if the DPad is not actively pressed.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the current direction of the DPad
		 */
		public PovDirection getDPadDirection() {
			if (controller.getButton( button_dpad_up )) {
				if (controller.getButton( button_dpad_left )) {
					return PovDirection.northWest;
				} else if (controller.getButton( button_dpad_right )) {
					return PovDirection.northEast;
				}
				return PovDirection.north;
			} else if (controller.getButton( button_dpad_down )) {
				if (controller.getButton( button_dpad_left )) {
					return PovDirection.southWest;
				} else if (controller.getButton( button_dpad_right )) {
					return PovDirection.southEast;
				}
				return PovDirection.south;
			} else if (controller.getButton( button_dpad_left )) {
				return PovDirection.west;
			} else if (controller.getButton( button_dpad_right )) {
				return PovDirection.east;
			}
			return PovDirection.center;
		}

		/**
		 * Returns the X axis value of the left analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is to the left.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the X axis value of the left analog stick.
		 */
		public float getLeftX() { return controller.getAxis(axis_left_x); }

		/**
		 * Returns the Y axis value of the left analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is towards the bottom.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the Y axis value of the left analog stick.
		 */
		public float getLeftY() { return controller.getAxis(axis_left_y); }

		/**
		 * Returns the value of the left trigger.
		 *
		 * This is a value between 0 and 1, where 0 is no pressure.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the value of the left trigger.
		 */
		public float getLeftTrigger() { return controller.getAxis(axis_left_trigger); }

		/**
		 * Returns the X axis value of the right analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is to the left.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the X axis value of the right analog stick.
		 */
		public float getRightX()  { return controller.getAxis(axis_right_x); }

		/**
		 * Returns the Y axis value of the right analog stick.
		 *
		 * This is a value between -1 and 1, where -1 is towards the bottom.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the Y axis value of the right analog stick.
		 */
		public float getRightY() { return controller.getAxis(axis_right_y); }

		/**
		 * Returns the value of the right trigger.
		 *
		 * This is a value between 0 and 1, where 0 is no pressure.
		 *
		 * This is a polling operation.  If the player pressed the button faster
		 * than an animation frame (unlikely), the input will be lost.
		 *
		 * @return the value of the right trigger.
		 */
		public float getRightTrigger()  { return controller.getAxis(axis_right_trigger); }
	}

	/**
	 * Creates a new (potential) XBox 360 input controller.
	 *
	 * This method will search the list of connected controllers.  If the controller
	 * at position device is an XBox controller, it will connect.  Otherwise, it 
	 * will wait until and X-Box controller is connected to that device.
	 *
	 * @param device The device id to treat as an X-Box controller
	 */
	public XBoxController(int device) {
		deviceid = device;
		if (Controllers.getControllers().size > deviceid) {
			initialize(Controllers.getControllers().get(deviceid));
		}
		Controllers.addListener(this);
	}
	
	/**
	 * Initializes this input controller as a wrapper around the given controller
	 *
	 * @param controller The base controller to wrap
	 */
	protected void initialize(Controller controller) {
		if (!controller.getName().toLowerCase().contains("xbox")) {
			wrapper = null;
			this.controller = null;
			return;
		}

		boolean macosx = System.getProperty("os.name").equals("Mac OS X");
		boolean wired = false;
		if (macosx) {
			// See if we are using the wired driver
			wired = controller.getName().endsWith("Wired Controller");
		}

		if (macosx && wired) {
			wrapper = new XBoxMacOSWiredController(controller);
		} else if (macosx) {
			wrapper = new XBoxMacOSBluetoothController(controller);
		} else {
			wrapper = new XBoxWindowsController(controller);
		}
		this.controller = controller;
	}
	
	/**
	 * Returns true if there is an X-Box 360 controller connected 
	 *
	 * @return true if there is an X-Box 360 controller connected 
 	 */
 	public boolean isConnected() {
 		return controller != null;
 	}
 	
	/**
	 * Returns true if the start button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the start button is currently pressed
	 */
	public boolean getStart() { return wrapper != null && wrapper.getStart(); }

	/**
	 * Returns true if the back button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the back button is currently pressed
	 */
	public boolean getBack() { return wrapper != null && wrapper.getBack(); }

	/**
	 * Returns true if the X button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the X button is currently pressed
	 */
	public boolean getX() { return wrapper != null && wrapper.getX(); }

	/**
	 * Returns true if the Y button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the Y button is currently pressed
	 */
	public boolean getY() { return wrapper != null && wrapper.getY(); }

	/**
	 * Returns true if the A button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the A button is currently pressed
	 */
	public boolean getA() { return wrapper != null && wrapper.getA(); }

	/**
	 * Returns true if the B button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the Y button is currently pressed
	 */
	public boolean getB() { return wrapper != null && wrapper.getB(); }

	/**
	 * Returns true if the left bumper is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the left bumper is currently pressed
	 */
	public boolean getLB() { return wrapper != null && wrapper.getLB(); }

	/**
	 * Returns true if the left analog stick is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the left analog stick is currently pressed
	 */
	public boolean getL3() { return wrapper != null && wrapper.getL3(); }

	/**
	 * Returns true if the right bumper is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the right bumper is currently pressed
	 */
	public boolean getRB() { return wrapper != null && wrapper.getRB(); }

	/**
	 * Returns true if the right analog stick is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the right analog stick is currently pressed
	 */
	public boolean getR3() { return wrapper != null && wrapper.getR3(); }

	/**
	 * Returns true if the DPad Up button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right, 
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the DPad Up button is currently pressed
	 */
	public boolean getDPadUp() { return wrapper != null && wrapper.getDPadUp(); }

	/**
	 * Returns true if the DPad Down button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right, 
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the DPad Down button is currently pressed
	 */
	public boolean getDPadDown() { return wrapper != null && wrapper.getDPadDown(); }

	/**
	 * Returns true if the DPad Left button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right, 
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the DPad Left button is currently pressed
	 */
	public boolean getDPadLeft() { return wrapper != null && wrapper.getDPadLeft(); }

	/**
	 * Returns true if the DPad Right button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right, 
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the DPad Right button is currently pressed
	 */
	public boolean getDPadRight() { return wrapper != null && wrapper.getDPadRight(); }

	/**
	 * Returns the current direction of the DPad
	 * 
	 * The result will be one of the eight cardinal directions, or
	 * center if the DPad is not actively pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the current direction of the DPad
	 */	 
	public PovDirection getDPadDirection() { return wrapper == null ? PovDirection.center : wrapper.getDPadDirection(); }
	
	/**
	 * Returns the X axis value of the left analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is to the left.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the X axis value of the left analog stick.
	 */
	public float getLeftX() { return wrapper == null ? 0 : wrapper.getLeftX(); }

	/**
	 * Returns the Y axis value of the left analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is towards the bottom.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the Y axis value of the left analog stick.
	 */
	public float getLeftY() { return wrapper == null ? 0 :wrapper.getLeftY(); }

	/**
	 * Returns the value of the left trigger.
	 *
	 * This is a value between 0 and 1, where 0 is no pressure.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the value of the left trigger.
	 */
	public float getLeftTrigger() { return wrapper == null ? 0 :wrapper.getLeftTrigger(); }

	/**
	 * Returns the X axis value of the right analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is to the left.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the X axis value of the right analog stick.
	 */
	public float getRightX() { return wrapper == null ? 0 :wrapper.getRightX(); }

	/**
	 * Returns the Y axis value of the right analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is towards the bottom.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the Y axis value of the right analog stick.
	 */
	public float getRightY() { return wrapper == null ? 0 :wrapper.getRightY(); }

	/**
	 * Returns the value of the right trigger.
	 *
	 * This is a value between 0 and 1, where 0 is no pressure.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the value of the right trigger.
	 */
	public float getRightTrigger() { return wrapper == null ? 0 :wrapper.getRightTrigger(); }

	// METHODS FOR CONTROLLER LISTENER

	/** 
	 * A Controller got connected.
	 *
	 * @param controller	The controller interface
	 */
	public void connected (Controller controller) {
		if (controller == null && Controllers.getControllers().size > deviceid) {
			initialize(Controllers.getControllers().get(deviceid));
		}
	}

	/** 
	 * A Controller got disconnected.
	 *
	 * @param controller	The controller interface
	 */
	public void disconnected (Controller controller) {
		if (this.controller == controller) {
			this.controller = null;
			wrapper = null;
		}
	}

	/** 
	 * A button on the Controller was pressed. 
	 * 
	 * The buttonCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts button constants for known controllers.
	 *
	 * @param controller	The controller interface
	 * @param buttonCode	The button pressed
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean buttonDown (Controller controller, int buttonCode) { return true; }

	/** 
	 * A button on the Controller was released. 
	 *
	 * The buttonCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts button constants for known controllers.
	 *
	 * @param controller	The controller interface
	 * @param buttonCode	The button released
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean buttonUp (Controller controller, int buttonCode) { return true; }

	/** 
	 * An axis on the Controller moved. 
	 *
	 * The axisCode is controller specific. The axis value is in the range [-1, 1]. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts axes constants for 
	 * known controllers.
	 *
	 * @param controller	The controller interface
	 * @param axisCode		The axis identifier
	 * @param value 		The axis value, -1 to 1
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean axisMoved (Controller controller, int axisCode, float value) { return true; }

	/** 
	 * A POV on the Controller moved. 
	 *
	 * The povCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts POV constants for known controllers.
	 *
	 * @param controller	The controller interface
	 * @param povCode		The POV identifier
	 * @param value			The POV value, -1 to 1
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean povMoved (Controller controller, int povCode, PovDirection value) { return true; }

	/** 
	 * An x-slider on the Controller moved. 
	 *
	 * The sliderCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts slider constants for known controllers.
	 *
	 * @param controller	The controller interface
	 * @param sliderCode	The slider identifier
	 * @param value			The value 0 to 1
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) { return true; }

	/** 
	 * An y-slider on the Controller moved. 
	 *
	 * The sliderCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts slider constants for known controllers.
	 *
	 * @param controller	The controller interface
	 * @param sliderCode	The slider identifier
	 * @param value			The value 0 to 1
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) { return true; }

	/** 
	 * An accelerometer value on the Controller changed. 
	 *
	 * The accelerometerCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts slider constants for known controllers. The value is a Vector3
	 * representing the acceleration on a 3-axis accelerometer in m/s^2.
	 *
	 * @param controller	The controller interface
	 * @param accelCode		The accelerometer identifier
	 * @param value			The acceleration vector
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean accelerometerMoved (Controller controller, int accelCode, Vector3 value) { return true; }
}
