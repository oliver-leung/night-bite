/*
 * DesktopLauncher.java
 * 
 * LibGDX is a cross-platform development library. You write all of your code in 
 * the core project.  However, you still need some extra classes if you want to
 * deploy on a specific platform (e.g. PC, Android, Web).  That is the purpose
 * of this class.  It deploys your game on a PC/desktop computer.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/3/2015
 */
package edu.cornell.gdiac.nightbite.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import edu.cornell.gdiac.nightbite.GDXRoot;

/**
 * The main class of the game.
 * 
 * This class sets the window size and launches the game.  Aside from modifying
 * the window size, you should almost never need to modify this class.
 */
public class DesktopLauncher {
	
	/**
	 * Classic main method that all Java programmers know.
	 * 
	 * This method simply exists to start a new LwjglApplication.  For desktop games,
	 * LibGDX is built on top of LWJGL (this is not the case for Android).
	 * 
	 * @param arg Command line arguments
	 */
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		// These are the configuration attributes
		config.width = 1920;
		config.height = 1080;
		config.resizable = true;
		config.foregroundFPS = 60;

		// THERE IS NO FULLSCREEN SUPPORT FOR RETINA MACS...
		// ... until LibGDX updates to LWGJL 3 (still testing as of December 2015) 
		config.fullscreen = false;

		// Create the new application and run it.
		new LwjglApplication(new GDXRoot(), config);
	}
}