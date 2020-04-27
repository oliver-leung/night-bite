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

import java.util.HashMap;

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
    static String PLAYER_FILMSTRIP_FILE = "character/lin_dash_64_fs.png";
    static int PLAYER_FILMSTRIP_ROW = 1;
    static int PLAYER_FILMSTRIP_COL = 8;
    static int PLAYER_FILMSTRIP_SIZE = 8;
    static String PLAYER_HOLDING_FILMSTRIP_FILE = "character/P1_Holding_8.png";
    static int PLAYER_HOLDING_FILMSTRIP_ROW = 1;
    static int PLAYER_HOLDING_FILMSTRIP_COL = 8;
    static int PLAYER_HOLDING_FILMSTRIP_SIZE = 8;
    static String WOK_FILE = "character/wok_64_nohand.png";
    static String PLAYER_SHADOW_FILE = "character/shadow.png";
    static String PLAYER_ARROW_FILE = "character/arrow.png";

    /* Firecracker filmstrip files */
    static String FIRECRACKER_FILE = "item/firecracker_64.png";
    static String FIRECRACKER_LIT_FILE = "item/firecracker_fuse_64_fs.png";
    static String FIRECRACKER_DET_FILE = "item/firecracker_detonating_64_fs.png";

    // Item
    static String FISH_ITEM_FILE = "item/food1_64.png";

    // Obstacle
    static String WALL_PA1_FILE = "environment/Box_64.png";
    static String WALL_PA2_FILE = "environment/box_palette2_64.png";

    // Hole
    static String HOLE_FILE = "environment/hole4_64.png";

    // Home stall
    static String HOME_STALL_FILE = "environment/StallHome1_64.png";

    public static HashMap<String, TextureRegion> FILES;
    static String[] FILE_NAMES = {
            "background/ground_64.png",
            "background/ground_palette2_64.png",
            "background/groundbrick_64.png",
            "background/groundbrick_palette2_64.png",
            "background/grounddark1_64.png",
            "background/grounddark1_palette2_64.png",
            "background/grounddark2_64.png",
            "background/grounddark2_palette2_64.png",
            "background/groundgrass_64.png",
            "background/groundgrass_palette2_64.png",

            "environment/Box_32.png",
            "environment/Box_64.png",
            "environment/box_palette2_32.png",
            "environment/box_palette2_64.png",
            "environment/brick.png",
            "environment/car.psd",
            "environment/car_32.png",
            "environment/car_64.png",
            "environment/carfront.psd",
            "environment/carfront_32.png",
            "environment/carfront_64.png",
            "environment/cobble.png",
            "environment/DirectionsSign_32.png",
            "environment/DirectionsSign_64.png",
            "environment/DirectionsSign_small_64.png",
            "environment/firecracker_filmstrip.png",
            "environment/goaldoor.png",
            "environment/Hanging_Lantern_32_FS_30.png",
            "environment/HangingLantern_32.png",
            "environment/HangingLantern_64.png",
            "environment/HangingLantern_64_Filmstrip_30.png",
            "environment/hole0_64.png",
            "environment/hole0_palette2_64.png",
            "environment/hole1_64.png",
            "environment/hole1_palette2_64.png",
            "environment/hole2.png",
            "environment/hole2adj_64.png",
            "environment/hole2adj_palette2_64.png",
            "environment/hole2opp_64.png",
            "environment/hole2opp_palette2_64.png",
            "environment/hole3_64.png",
            "environment/hole3_palette2_64.png",
            "environment/hole4_64.png",
            "environment/hole4_palette2_64.png",
            "environment/redcar_32.png",
            "environment/redcar_64.png",
            "environment/redcarfront_32.png",
            "environment/redcarfront_64.png",
            "environment/Shrub_32.png",
            "environment/Shrub_32_Filmstrip_30.png",
            "environment/Shrub_64.png",
            "environment/Shrub_64_Filmstrip_30.png",
            "environment/Shrub_64_palette3.png",
            "environment/Shrub_palette2_64.png",
            "environment/shrub_palette2_filmstrip.png",
            "environment/StallHome1_64.png",
            "environment/StallHome2_64.png",
            "environment/StallHome3_64.png",
            "environment/StallHome4_64.png",
            "environment/StallItem1_64.png",
            "environment/StallOther1_64.png",
            "environment/StallOther2_64.png",
            "environment/StallOther_palette2_64.png",
            "environment/StallOtherWide_64.png",
            "environment/Stalls_32.png",
            "environment/Stalls_32_Grid.png",
            "environment/Stalls_64.png",
            "environment/Stalls_64_Darkbg.png",
            "environment/stand-border.png",
            "environment/Tree_32.png",
            "environment/Tree_64_palette2.png",

            "background/Box_64.png",
            "background/box_palette2_64.png",
            "background/brick.png",
            "background/Box_64.png",
            "background/box_palette2_64.png",
            "background/brick.png",
            "background/cobble.png",
            "background/DirectionsSign_small_64.png",
            "background/goaldoor.png",
            "background/HangingLantern_32.png",
            "background/hole0_64.png",
            "background/hole0_palette2_64.png",
            "background/hole1_64.png",
            "background/hole1_palette2_64.png",
            "background/hole2.png",
            "background/hole2adj_64.png",
            "background/hole2adj_palette2_64.png",
            "background/hole2opp_64.png",
            "background/hole2opp_palette2_64.png",
            "background/hole3_64.png",
            "background/hole3_palette2_64.png",
            "background/hole4_64.png",
            "background/hole4_palette2_64.png",
            "background/StallHome1_64.png",
            "background/StallHome2_64.png",
            "background/StallHome3_64.png",
            "background/StallHome4_64.png",
            "background/StallItem1_64.png",
            "background/StallOther1_64.png",
            "background/StallOther2_64.png",
            "background/StallOther_palette2_64.png",
            "background/stand-border.png"
    };

    // Background
    static String GAME_BACKGROUND_FILE = "background/ground_64.png";

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
    public static FilmStrip PLAYER_FILMSTRIP;
    public static FilmStrip PLAYER_HOLD_FILMSTRIP;
    public static TextureRegion WOK;
    public static TextureRegion PLAYER_SHADOW;
    public static TextureRegion PLAYER_ARROW;
    public static TextureRegion HOME_STALL;
    public static TextureRegion FISH_ITEM;
    public static TextureRegion WALL;
    public static TextureRegion HOLE;
    public static TextureRegion GAME_BACKGROUND;
    public static TextureRegion GOAL;

    public static FilmStrip FIRECRACKER;
    public static FilmStrip FIRECRACKER_LIT;
    public static FilmStrip FIRECRACKER_DET;

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
        loadTexture(WOK_FILE);
        loadTexture(PLAYER_SHADOW_FILE);
        loadTexture(PLAYER_ARROW_FILE);
        loadTexture(WALL_PA1_FILE);
        loadTexture(HOLE_FILE);
        loadTexture(GAME_BACKGROUND_FILE);
        loadTexture(GOAL_FILE);

        loadTexture(FIRECRACKER_FILE);
        loadTexture(FIRECRACKER_LIT_FILE);
        loadTexture(FIRECRACKER_DET_FILE);

        loadTexture(PLAYER_FILMSTRIP_FILE);
        loadTexture(PLAYER_HOLDING_FILMSTRIP_FILE);
        loadTexture(HOME_STALL_FILE);

        for (String filename : FILE_NAMES) {
            try {
                loadTexture(filename);
            } catch (Exception ignored) {
            }
        }

        // Load Font
        loadFont(RETRO_FONT_FILE, RETRO_FONT_SIZE);
    }

    /** Preloads the texture and sound information for the game :
     *  extracting assets from the manager after it has finished loading them */
    public void loadContent(AssetManager manager) {
        FISH_ITEM = createTexture(manager, FISH_ITEM_FILE, true);
        WALL = createTexture(manager, WALL_PA1_FILE, true);
        HOLE = createTexture(manager, HOLE_FILE, true);
        GAME_BACKGROUND = createTexture(manager, GAME_BACKGROUND_FILE, true);
        GOAL = createTexture(manager, GOAL_FILE, true);

        // Start music
        music = Gdx.audio.newMusic(Gdx.files.internal("music/Night_Bite_(Theme).mp3"));
        music.setLooping(true);
        music.play();
        music.setVolume(0.1f);

        FILES = new HashMap<>();
        // Make background textures
        for (String filename : FILE_NAMES) {
            FILES.put(filename, createTexture(manager, filename, false));
        }

        // Player & Items
//        int num_players = PLAYER_FILMSTRIP_FILES.length;
        PLAYER_FILMSTRIP = createFilmStrip(manager, PLAYER_FILMSTRIP_FILE, PLAYER_FILMSTRIP_ROW,
                PLAYER_FILMSTRIP_COL, PLAYER_FILMSTRIP_SIZE);;
        PLAYER_HOLD_FILMSTRIP = createFilmStrip(manager, PLAYER_HOLDING_FILMSTRIP_FILE, PLAYER_HOLDING_FILMSTRIP_ROW,
                PLAYER_HOLDING_FILMSTRIP_COL, PLAYER_HOLDING_FILMSTRIP_SIZE);;
        WOK = createTexture(manager, WOK_FILE, true);
        PLAYER_SHADOW = createTexture(manager, PLAYER_SHADOW_FILE, true);
        PLAYER_ARROW = createTexture(manager, PLAYER_ARROW_FILE, true);

        // Firecracker filmstrip
        // TODO don't hardcode the rows/cols/size
        FIRECRACKER = createFilmStrip(manager, FIRECRACKER_FILE, 1, 1, 1);
        FIRECRACKER_LIT = createFilmStrip(manager, FIRECRACKER_LIT_FILE, 1, 5, 5);
        FIRECRACKER_DET = createFilmStrip(manager, FIRECRACKER_DET_FILE, 1, 7, 7);


        // Home stall textures
        HOME_STALL = createTexture(manager, HOME_STALL_FILE, true);

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

    /**
     * @param filename Name of the file
     * @return The TextureRegion from the given filename
     */
    public static TextureRegion get(String filename) {
        return FILES.get(filename);
    }
}
