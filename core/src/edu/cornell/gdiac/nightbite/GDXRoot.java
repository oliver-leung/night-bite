/*
 * GDXRoot.java
 *
 * This is the primary class file for running the game.  It is the "static main" of
 * LibGDX.  In the first lab, we extended ApplicationAdapter.  In previous lab
 * we extended Game.  This is because of a weird graphical artifact that we do not
 * understand.  Transparencies (in 3D only) is failing when we use ApplicationAdapter. 
 * There must be some undocumented OpenGL code in setScreen.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
 package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * Root class for a LibGDX.
 * <p>
 * This class is technically not the ROOT CLASS. Each platform has another class above
 * this (e.g. PC games use DesktopLauncher) which serves as the true root.  However,
 * those classes are unique to each platform, while this class is the same across all
 * plaforms. In addition, this functions as the root class all intents and purposes,
 * and you would draw it as a root class in an architecture specification.
 */
public class GDXRoot extends Game implements ScreenListener {
	/**
	 * AssetManager to load game assets (textures, sounds, etc.)
	 */
	private AssetManager manager;
	/**
	 * Asset handler
	 */
	private Assets assets;
	/**
	 * Drawing context to display graphics (VIEW CLASS)
	 */
	private GameCanvas canvas;
	/**
	 * Player mode for the asset loading screen (CONTROLLER CLASS)
	 */
	private LoadingMode loading;
	/**
	 * Player mode for the asset loading screen (CONTROLLER CLASS)
	 */
	private LevelSelectMode levelSelect;
	/**
	 * List of all WorldControllers
	 */
	private WorldController controller;

	/**
	 * Creates a new game from the configuration settings.
	 * <p>
	 * This method configures the asset manager, but does not load any assets
	 * or assign any screen.
	 */
	public GDXRoot() {
		// Start loading with the asset manager
		manager = new AssetManager();
		
		// Add font support to the asset manager
		FileHandleResolver resolver = new InternalFileHandleResolver();
		manager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
		manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));
	}

	/** 
	 * Called when the Application is first created.
	 * 
	 * This is method immediately loads assets for the loading screen, and prepares
	 * the asynchronous loader for all other assets.
	 */
	public void create() {
		canvas = new GameCanvas();
		loading = new LoadingMode(canvas, manager, 1);
		levelSelect = new LevelSelectMode(canvas);

		assets = new Assets(manager);
		assets.preLoadContent(manager);
		controller = new WorldController();

		loading.setScreenListener(this);
		setScreen(loading);

		// Create logger
		Gdx.app.setApplicationLogger(new Logger());
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
	}

	/** 
	 * Called when the Application is destroyed. 
	 *
	 * This is preceded by a call to pause().
	 */
	public void dispose() {
		// Call dispose on our children
		setScreen(null);
		assets.unloadContent(manager);
		controller.dispose();

		canvas.dispose();
		canvas = null;

		// Unload all of the resources
		manager.clear();
		manager.dispose();
		super.dispose();
	}

	/**
	 * Called when the Application is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to create().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// TODO: Add logic for affine-transforming objects and hitboxes when resizing. Perhaps GameCanvas.computeTransform would be useful for this?
		canvas.resize();
		super.resize(width, height);
	}
	
	/**
	 * The given screen has made a request to exit its player mode.
	 *
	 * The value exitCode can be used to implement menu options.
	 *
	 * @param screen   The screen requesting to exit
	 * @param exitCode The state of the screen upon exit
	 */
	public void exitScreen(Screen screen, int exitCode) { // TODO fix whack shit
		if (screen == loading) {
			if (levelSelect == null) {
				levelSelect = new LevelSelectMode(canvas);
			}
			levelSelect.setScreenListener(this);
			setScreen(levelSelect);

			loading.dispose();
			loading = null;
		} else if (screen == levelSelect) {
			if (exitCode == levelSelect.EXIT_START) {
				assets.loadContent(manager);
				controller.setScreenListener(this);
				controller.setCanvas(canvas);
				controller.setLevel(levelSelect.getSelectedLevelJSON());

				controller.reset();
				setScreen(controller);

				levelSelect.dispose();
				levelSelect = null;
			} else if (exitCode == levelSelect.EXIT_MENU) {
				loading = new LoadingMode(canvas, manager, 1);
				loading.setScreenListener(this);
				setScreen(loading);

				levelSelect.dispose();
				levelSelect = null;
			}
		} else if (exitCode == WorldController.EXIT_QUIT) {
			// We quit the main application
			Gdx.app.exit();
		} else if (exitCode == WorldController.EXIT_NEXT) {
			controller.reset();
		}
	}

}
