package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.util.FilmStrip;

// The whole point of this class is to get all textures in a unified location.
// This makes it easier for assets to be data driven (defining assets in json files)

// It also makes more sense than making asset loading span across LoadingMode and WorldController
// (And also slightly better than cramming everything into LoadingMode, which also needs to handle
// drawing/other functions

public class Assets {
    /** Asset Manager */
    private AssetManager manager;

    /** Track load status */
    private boolean isLoaded = false;

    /** Track all loaded assets (for unloading purposes */
    protected Array<String> assets = new Array<>();

    /** RESOURCES */
    // Character
    static String[] PLAYER_FILMSTRIP_FILES = {"character/granny_walkcycle_64_fs_v1.png", "character/lin_walkcycle_64_fs_v2.png"};
    static int PLAYER_FILMSTRIP_ROW = 1;
    static int PLAYER_FILMSTRIP_COL = 4;
    static int PLAYER_FILMSTRIP_SIZE = 4;

    // Item
    static  String FISH_ITEM_FILE = "item/fish.png";

    // Obstacle
    static String WALL_FILE = "environment/brick.png";
    static String HOLE_FILE = "environment/hole2.png";
    static String STAND_FILE = "environment/stand-border.png";

    // Background
    static String GAME_BACKGROUND_FILE = "environment/cobble.png";

    // Other
    static String GOAL_FILE = "environment/goaldoor.png";

    // Font
    static String RETRO_FONT_FILE = "font/RetroGame.ttf";
    static int RETRO_FONT_SIZE = 12;

    // Sound
    static String MUSIC_FILE = "music/Night_Bite_(Theme).mp3";

    /*
     * TODO: A future goal for this class would be to also make the file paths above and the loaded assets below
     * data-driven.
     */

    /**
     * LOADED ASSETS
     */
    public static FilmStrip[] PLAYER_FILMSTRIPS;
    public static TextureRegion FISH_ITEM;
    public static TextureRegion WALL;
    public static TextureRegion HOLE;
    public static TextureRegion STAND;
    public static TextureRegion GAME_BACKGROUND;
    public static TextureRegion GOAL;
    public static BitmapFont RETRO_FONT;
    public static Music music;

    public Assets(AssetManager manager) {
        setManager(manager);
    }

    public void setManager(AssetManager manager) {
        this.manager = manager;
    }

    public boolean getLoaded() {
        return isLoaded;
    }

    /** Preloads the texture and sound information for the game :
     *  telling manager what to load */
    public void preLoadContent(AssetManager manager) {
        // Load Textures
        loadTexture(FISH_ITEM_FILE);
        loadTexture(WALL_FILE);
        loadTexture(HOLE_FILE);
        loadTexture(STAND_FILE);
        loadTexture(GAME_BACKGROUND_FILE);
        loadTexture(GOAL_FILE);
        for (String player_file : PLAYER_FILMSTRIP_FILES) {
            loadTexture(player_file);
        }

        // Load Font
        loadFont(RETRO_FONT_FILE, RETRO_FONT_SIZE);
    }

    /** Preloads the texture and sound information for the game :
     *  extracting assets from the manager after it has finished loading them */
    public void loadContent(AssetManager manager) {
        FISH_ITEM = createTexture(manager, FISH_ITEM_FILE, true);
        WALL = createTexture(manager, WALL_FILE, true);
        HOLE = createTexture(manager, HOLE_FILE, true);
        STAND = createTexture(manager, STAND_FILE, true);
        GAME_BACKGROUND = createTexture(manager, GAME_BACKGROUND_FILE, true);
        GOAL = createTexture(manager, GOAL_FILE, true);

        // Start music
        music = Gdx.audio.newMusic(Gdx.files.internal("music/Night_Bite_(Theme).mp3"));
        music.setLooping(true);
        music.play();
        music.setVolume(0.3f);

        // Player Filmstrips
        int num_players = PLAYER_FILMSTRIP_FILES.length;
        PLAYER_FILMSTRIPS = new FilmStrip[num_players];
        for (int i = 0; i < num_players; i++) {
            FilmStrip player = createFilmStrip(manager, PLAYER_FILMSTRIP_FILES[i], PLAYER_FILMSTRIP_ROW,
                    PLAYER_FILMSTRIP_COL, PLAYER_FILMSTRIP_SIZE);
            PLAYER_FILMSTRIPS[i] = player;
        }

        // Allocate Font
        if (manager.isLoaded(RETRO_FONT_FILE)) {
            RETRO_FONT = manager.get(RETRO_FONT_FILE, BitmapFont.class);
        } else {
            RETRO_FONT = null;
        }

        this.isLoaded = true;
    }

    /** Unloads the assets for this game.*/
    public void unloadContent(AssetManager manager) {
        for(String s : assets) {
            if (manager.isLoaded(s)) {
                manager.unload(s);
            }
        }
        this.isLoaded = false;
    }

    public void loadTexture(String filePath) {
        manager.load(filePath, Texture.class);
        assets.add(filePath);
    }

    public void loadFont(String fontPath, int fontSize) {
        FreetypeFontLoader.FreeTypeFontLoaderParameter size2Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size2Params.fontFileName = fontPath;
        size2Params.fontParameters.size = fontSize;
        manager.load(fontPath, BitmapFont.class, size2Params);
        assets.add(fontPath);
    }


    /**
     * Returns a newly loaded filmstrip for the given file.
     * <p>
     * This helper methods is used to set texture settings (such as scaling, and
     * the number of animation frames) after loading.
     */
    protected static FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
        if (manager.isLoaded(file)) {
            FilmStrip strip = new FilmStrip(manager.get(file, Texture.class), rows, cols, size);
            strip.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return strip;
        }
        return null;
    }

    /**
     * Returns a newly loaded texture region for the given file.
     * <p>
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     */
    protected static TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
        if (manager.isLoaded(file)) {
            TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (repeat) {
                region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            }
            return region;
        }
        return null;
    }
}
