package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.SoundController;

import java.util.HashMap;

// The whole point of this class is to get all textures in a unified location.
// This makes it easier for assets to be data driven (defining assets in json files)

// It also makes more sense than making asset loading span across LoadingMode and WorldController
// (And also slightly better than cramming everything into LoadingMode, which also needs to handle
// drawing/other functions

public class Assets {
    /** Mapping from file names to in-game texture assets */
    public static HashMap<String, TextureRegion> TEXTURES = new HashMap<>();
    /** In-game music asset */
    public static Music MUSIC;
    /** Mapping from file names to in-game sound effects */
    public static HashMap<String, Sound> SOUND_EFFECTS = new HashMap<>();
    /** In-game font asset */
    public static BitmapFont FONT;
    /** Asset Manager */
    private AssetManager manager;
    /** Categorized file names of all assets */
    private final String FILE_NAMES = "assets.json";
    /** Track load status */
    private boolean isLoaded = false;
    /** Keys that map to static texture file names */
    private final String[] textureKeys = new String[]{"ground", "decoration", "hole", "character", "team", "wall", "item"};
    // TODO: Delete these filmstrip constants
    int PLAYER_FILMSTRIP_ROW = 1;
    int PLAYER_FILMSTRIP_COL = 8;
    int PLAYER_FILMSTRIP_SIZE = 8;
    /** Track all loaded assets (for unloading purposes) */
    private Array<String> assets = new Array<>();
    /** JSON Reader */
    private JsonReader jsonReader = new JsonReader();
    /** JSON of asset file names */
    private JsonValue FILE_NAMES_JSON;

    /** RESOURCES */
    // Character
    static String PLAYER_FILMSTRIP_FILE = "character/Filmstrip/Player 1/P1_Dash_5.png";
    static String PLAYER_HOLDING_FILMSTRIP_FILE = "character/Filmstrip/Player 1/P1_Holding_8.png";
    static int PLAYER_HOLDING_FILMSTRIP_ROW = 1;
    static int PLAYER_HOLDING_FILMSTRIP_COL = 8;
    static int PLAYER_HOLDING_FILMSTRIP_SIZE = 8;
    static String PLAYER_FALLING_FILMSTRIP_FILE = "character/P1_Falling_5.png";
    static int PLAYER_FALLING_FILMSTRIP_ROW = 1;
    static int PLAYER_FALLING_FILMSTRIP_COL = 6;
    static int PLAYER_FALLING_FILMSTRIP_SIZE = 6;
    static String WOK_FILE = "character/wok_64_nohand.png";
    static String PLAYER_SHADOW_FILE = "character/shadow.png";
    static String PLAYER_ARROW_FILE = "character/arrow.png";

    /* Enemies */
    static String FIRE_ENEMY_WALK_FILE = "character/Enemies/E1_64_Walk_FS_8.png";
    static String FIRE_ENEMY_FALL_FILE = "character/Enemies/E1_64_Falling_FS_5.png";
    static String OIL_ENEMY_WALK_FILE = "character/Enemies/E2_64_Walk_FS_8.png";
    static String OIL_ENEMY_FALL_FILE = "character/Enemies/E2_64_Falling_FS_5.png";

    /* Firecracker filmstrip files */
    static String FIRECRACKER_FILE = "item/firecracker_64.png";
    static String FIRECRACKER_LIT_FILE = "item/firecracker_fuse_64_fs.png";
    static String FIRECRACKER_DET_FILE = "item/firecracker_detonating_64_fs.png";

    /* Oil */
    static String OIL_SPILLING_FILE = "item/oil_64_filmstrip.png";
    static String OIL_TILE_FILE = "item/oiltile_64.png";

    // Item
    static String FISH_ITEM_FILE = "item/food1_64.png";

    // Obstacle
    static String WALL_PA1_FILE = "environment/Box_64.png";
    static String WALL_PA2_FILE = "environment/box_palette2_64.png";

    // Home stall
    static String HOME_STALL_FILE = "environment/StallHome_64_fs.png";

    // Level select screen
    private static final String LEVEL_SELECT_BACKGROUND_FILE = "level_select/Background.png";
    private static final String LEVEL1_TILE_FILE = "level_select/#1.png";
    private static final String LEVEL2_TILE_FILE = "level_select/#2.png";
    private static final String LEVEL3_TILE_FILE = "level_select/#3.png";
    private static final String LEVEL4_TILE_FILE = "level_select/#4.png";
    private static final String LEVEL5_TILE_FILE = "level_select/#5.png";
    private static final String LEVEL6_TILE_FILE = "level_select/#6.png";
    private static final String LEVEL1_STALL_FILE = "level_select/LVL1_Stall.png";
    private static final String LEVEL2_STALL_FILE = "level_select/LVL2_Stall.png";
    private static final String LEVEL3_STALL_FILE = "level_select/LVL3_Stall.png";
    private static final String ARROW_BUTTON_FILE = "level_select/Arrow.png";
    private static final String BACK_BUTTON_FILE = "level_select/Back.png";
    private static final String HEADER_FILE = "level_select/Header.png";
    private static final String PLAYER_FILE = "level_select/Lin_128px.png";

    // Sound
    public static float EFFECT_VOLUME = 0.1f;
    static String MUSIC_FILE = "audio/Night_Bite_(Theme)_v2.wav";
    public static String FX_DELIVER_FILE = "audio/delivered.wav";
    public static String FX_PICKUP_FILE = "audio/pickup.wav";
    public static String FX_FIRECRACKER_FILE = "audio/firecracker.wav";
    public static String FX_FALL_FILE = "audio/whistle.wav";

    /**
     * LOADED ASSETS
     */
    public static FilmStrip PLAYER_FILMSTRIP;
    public static FilmStrip PLAYER_HOLD_FILMSTRIP;
    public static FilmStrip PLAYER_FALL_FILMSTRIP;
    public static TextureRegion WOK;
    public static TextureRegion PLAYER_SHADOW;
    public static TextureRegion PLAYER_ARROW;
    public static FilmStrip HOME_STALL;
    public static TextureRegion FISH_ITEM;
    public static TextureRegion WALL;
    public static TextureRegion HOLE;
    public static TextureRegion GAME_BACKGROUND;
    public static TextureRegion GOAL;

    public static FilmStrip FIRE_ENEMY_WALK;
    public static FilmStrip FIRE_ENEMY_FALL;
    public static FilmStrip OIL_ENEMY_WALK;
    public static FilmStrip OIL_ENEMY_FALL;

    public static FilmStrip FIRECRACKER;
    public static FilmStrip FIRECRACKER_LIT;
    public static FilmStrip FIRECRACKER_DET;

    public static FilmStrip OIL_SPILLING;
    public static FilmStrip OIL_TILE;

    public static BitmapFont RETRO_FONT;

    // Level select
    public static TextureRegion LEVEL_SELECT_BACKGROUND;
    public static TextureRegion TILE_1_TEXTURE;
    public static TextureRegion TILE_2_TEXTURE;
    public static TextureRegion TILE_3_TEXTURE;
    public static TextureRegion TILE_4_TEXTURE;
    public static TextureRegion TILE_5_TEXTURE;
    public static TextureRegion TILE_6_TEXTURE;
    public static TextureRegion STORE_1_TEXTURE;
    public static TextureRegion STORE_2_TEXTURE;
    public static TextureRegion STORE_3_TEXTURE;
    public static TextureRegion ARROW_TEXTURE;
    public static TextureRegion ARROW_TEXTURE_FLIPPED;
    public static TextureRegion BACK_TEXTURE;
    public static TextureRegion HEADER_TEXTURE;
    public static TextureRegion PLAYER_TEXTURE;

    public Assets(AssetManager manager) {
        setManager(manager);
        FILE_NAMES_JSON = jsonReader.parse(Gdx.files.internal(FILE_NAMES));
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
    protected static TextureRegion createTexture(AssetManager manager, String file) {
        if (manager.isLoaded(file)) {
            TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return region;
        }
        return null;
    }

    public void setManager(AssetManager manager) {
        this.manager = manager;
    }

    public boolean getLoaded() {
        return isLoaded;
    }

    /** Preload the texture and sound information for the game */
    public void preLoadContent() {
        loadTexture(FISH_ITEM_FILE);
        loadTexture(WOK_FILE);
        loadTexture(PLAYER_SHADOW_FILE);
        loadTexture(PLAYER_ARROW_FILE);
        loadTexture(WALL_PA1_FILE);

        loadTexture(FIRE_ENEMY_FALL_FILE);
        loadTexture(FIRE_ENEMY_WALK_FILE);
        loadTexture(OIL_ENEMY_FALL_FILE);
        loadTexture(OIL_ENEMY_WALK_FILE);

        loadTexture(FIRECRACKER_FILE);
        loadTexture(FIRECRACKER_LIT_FILE);
        loadTexture(FIRECRACKER_DET_FILE);

        loadTexture(OIL_SPILLING_FILE);
        loadTexture(OIL_TILE_FILE);

        loadTexture(PLAYER_FILMSTRIP_FILE);
        loadTexture(PLAYER_HOLDING_FILMSTRIP_FILE);
        loadTexture(PLAYER_FALLING_FILMSTRIP_FILE);
        loadTexture(HOME_STALL_FILE);

        loadTexture(LEVEL_SELECT_BACKGROUND_FILE);
        loadTexture(LEVEL1_TILE_FILE);
        loadTexture(LEVEL2_TILE_FILE);
        loadTexture(LEVEL3_TILE_FILE);
        loadTexture(LEVEL4_TILE_FILE);
        loadTexture(LEVEL5_TILE_FILE);
        loadTexture(LEVEL6_TILE_FILE);
        loadTexture(LEVEL1_STALL_FILE);
        loadTexture(LEVEL2_STALL_FILE);
        loadTexture(LEVEL3_STALL_FILE);
        loadTexture(ARROW_BUTTON_FILE);
        loadTexture(BACK_BUTTON_FILE);
        loadTexture(HEADER_FILE);
        loadTexture(PLAYER_FILE);

        // Static textures
        for (String key : textureKeys) {
            for (String fileName : FILE_NAMES_JSON.get(key).asStringArray()) {
                loadTexture(fileName);
            }
        }

        // Filmstrips
        for (String fileName : FILE_NAMES_JSON.get("character filmstrip").asStringArray()) {
            loadTexture(fileName);
        }

        loadMusic(FILE_NAMES_JSON.getString("music"));

        // Sound effects
        for (String fileName : FILE_NAMES_JSON.get("sound effect").asStringArray()) {
            loadSound(fileName);
        }

        loadFont(FILE_NAMES_JSON.getString("font"), 12);

        // Load sounds
        loadSound(FX_DELIVER_FILE);
        loadSound(FX_FALL_FILE);
        loadSound(FX_FIRECRACKER_FILE);
        loadSound(FX_PICKUP_FILE);
    }

    /** Preloads the texture and sound information for the game :
     *  extracting assets from the manager after it has finished loading them */
    public void loadContent(AssetManager manager) {
        // Static textures
        for (String key : textureKeys) {
            for (String fileName : FILE_NAMES_JSON.get(key).asStringArray()) {
                TEXTURES.put(fileName, createTexture(manager, fileName));
            }
        }

        // Start music // TODO fix this when I'm not sleepy
        // MUSIC = manager.get("audio/Night_Bite_(Theme).mp3");

        MUSIC = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_FILE));
        MUSIC.setLooping(true);
        MUSIC.play();
        MUSIC.setVolume(0.1f);

        for (String fileName : FILE_NAMES_JSON.get("character filmstrip").asStringArray()) {
            TextureRegion rawTexture = new TextureRegion(manager.get(fileName, Texture.class));
            int[] dims = getFilmStripDimensions(rawTexture, 64);
            TEXTURES.put(fileName, createFilmStrip(manager, fileName, dims[0], dims[1], dims[2]));
        }

        SoundController sounds = SoundController.getInstance();
        sounds.allocate(manager, FX_DELIVER_FILE);
        sounds.allocate(manager, FX_FALL_FILE);
        sounds.allocate(manager, FX_FIRECRACKER_FILE);
        sounds.allocate(manager, FX_PICKUP_FILE);

        // Player & Items
        PLAYER_FILMSTRIP = createFilmStrip(manager, PLAYER_FILMSTRIP_FILE, PLAYER_FILMSTRIP_ROW,
                PLAYER_FILMSTRIP_COL, PLAYER_FILMSTRIP_SIZE);
        PLAYER_HOLD_FILMSTRIP = createFilmStrip(manager, PLAYER_HOLDING_FILMSTRIP_FILE, PLAYER_HOLDING_FILMSTRIP_ROW,
                PLAYER_HOLDING_FILMSTRIP_COL, PLAYER_HOLDING_FILMSTRIP_SIZE);
        PLAYER_FALL_FILMSTRIP = createFilmStrip(manager, PLAYER_FALLING_FILMSTRIP_FILE, PLAYER_FALLING_FILMSTRIP_ROW,
            PLAYER_FALLING_FILMSTRIP_COL, PLAYER_FALLING_FILMSTRIP_SIZE);
        WOK = createTexture(manager, WOK_FILE, true);
        PLAYER_SHADOW = createTexture(manager, PLAYER_SHADOW_FILE, true);
        PLAYER_ARROW = createTexture(manager, PLAYER_ARROW_FILE, true);

        // Enemies
        FIRE_ENEMY_WALK = createFilmStrip(manager, FIRE_ENEMY_WALK_FILE, 1,8,8);
        FIRE_ENEMY_FALL = createFilmStrip(manager, FIRE_ENEMY_FALL_FILE, 1,6,6);
        OIL_ENEMY_WALK = createFilmStrip(manager, OIL_ENEMY_WALK_FILE,1,8,8);
        OIL_ENEMY_FALL = createFilmStrip(manager, OIL_ENEMY_FALL_FILE, 1, 6,6);

        // Firecracker filmstrip
        // TODO don't hardcode the rows/cols/size
        FIRECRACKER = createFilmStrip(manager, FIRECRACKER_FILE, 1, 1, 1);
        FIRECRACKER_LIT = createFilmStrip(manager, FIRECRACKER_LIT_FILE, 1, 5, 5);
        FIRECRACKER_DET = createFilmStrip(manager, FIRECRACKER_DET_FILE, 1, 7, 7);

        // Oil
        OIL_SPILLING = createFilmStrip(manager, OIL_SPILLING_FILE,1,12,12);
        OIL_TILE = createFilmStrip(manager, OIL_TILE_FILE,1,1,1);

        // Home stall textures
        HOME_STALL = createFilmStrip(manager, HOME_STALL_FILE, 1, 4, 4);

        // Level select screen
        LEVEL_SELECT_BACKGROUND = createTexture(manager, LEVEL_SELECT_BACKGROUND_FILE, true);
        TILE_1_TEXTURE = createTexture(manager, LEVEL1_TILE_FILE, true);
        TILE_2_TEXTURE = createTexture(manager, LEVEL2_TILE_FILE, true);
        TILE_3_TEXTURE = createTexture(manager, LEVEL3_TILE_FILE, true);
        TILE_4_TEXTURE = createTexture(manager, LEVEL4_TILE_FILE, true);
        TILE_5_TEXTURE = createTexture(manager, LEVEL5_TILE_FILE, true);
        TILE_6_TEXTURE = createTexture(manager, LEVEL6_TILE_FILE, true);
        STORE_1_TEXTURE = createTexture(manager, LEVEL1_STALL_FILE, true);
        STORE_2_TEXTURE = createTexture(manager, LEVEL2_STALL_FILE, true);
        STORE_3_TEXTURE = createTexture(manager, LEVEL3_STALL_FILE, true);
        ARROW_TEXTURE = createTexture(manager, ARROW_BUTTON_FILE, true);
        ARROW_TEXTURE_FLIPPED = createTexture(manager, ARROW_BUTTON_FILE, true);
        BACK_TEXTURE = createTexture(manager, BACK_BUTTON_FILE, true);
        HEADER_TEXTURE = createTexture(manager, HEADER_FILE, true);
        PLAYER_TEXTURE = createTexture(manager, PLAYER_FILE, true);

        FONT = manager.get(FILE_NAMES_JSON.getString("font"));

        isLoaded = true;
    }

    /** Unloads the assets for this game. */
    public void unloadContent(AssetManager manager) {
        for (String s : assets) {
            if (manager.isLoaded(s)) {
                manager.unload(s);
            }
        }
        this.isLoaded = false;
    }

    /**
     * Get the dimensions a film strip, given its raw texture size (assuming that tiles are 64 x 64 pixels).
     *
     * @param textureRegion Raw texture region
     * @return [rows, cols, size]
     */
    private int[] getFilmStripDimensions(TextureRegion textureRegion, int pixels) {
        int rows = textureRegion.getRegionHeight() / pixels;
        int cols = textureRegion.getRegionWidth() / pixels;
        int size = rows * cols;

        return new int[]{rows, cols, size};
    }

    public void loadSound(String filePath) {
        manager.load(filePath, Sound.class);
        assets.add(filePath);
    }

    public void loadMusic(String filePath) {
        manager.load(filePath, Music.class);
        assets.add(filePath);
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
