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

    // Home stall
    static String HOME_STALL_FILE = "environment/StallHome1_64.png";

    // Level select screen
    private static final String LEVEL_SELECT_BACKGROUND_FILE = "level_select/Background.png";
    private static final String LEVEL1_TILE_FILE = "level_select/#1.png";
    private static final String LEVEL2_TILE_FILE = "level_select/#2.png";
    private static final String LEVEL3_TILE_FILE = "level_select/#3.png";
    private static final String LEVEL1_STALL_FILE = "level_select/LVL1_Stall.png";
    private static final String LEVEL2_STALL_FILE = "level_select/LVL2_Stall.png";
    private static final String LEVEL3_STALL_FILE = "level_select/LVL3_Stall.png";
    private static final String ARROW_BUTTON_FILE = "level_select/Arrow.png";
    private static final String BACK_BUTTON_FILE = "level_select/Back.png";
    private static final String HEADER_FILE = "level_select/Header.png";
    private static final String PLAYER_FILE = "level_select/Lin_128px.png";

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

    // Level select
    public static TextureRegion LEVEL_SELECT_BACKGROUND;
    public static TextureRegion TILE_1_TEXTURE;
    public static TextureRegion TILE_2_TEXTURE;
    public static TextureRegion TILE_3_TEXTURE;
    public static TextureRegion STORE_1_TEXTURE;
    public static TextureRegion STORE_2_TEXTURE;
    public static TextureRegion STORE_3_TEXTURE;
    public static TextureRegion ARROW_TEXTURE;
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

        loadTexture(FIRECRACKER_FILE);
        loadTexture(FIRECRACKER_LIT_FILE);
        loadTexture(FIRECRACKER_DET_FILE);

        loadTexture(PLAYER_FILMSTRIP_FILE);
        loadTexture(PLAYER_HOLDING_FILMSTRIP_FILE);
        loadTexture(HOME_STALL_FILE);

        loadTexture(LEVEL_SELECT_BACKGROUND_FILE);
        loadTexture(LEVEL1_TILE_FILE);
        loadTexture(LEVEL2_TILE_FILE);
        loadTexture(LEVEL3_TILE_FILE);
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
        music = Gdx.audio.newMusic(Gdx.files.internal("music/Night_Bite_(Theme).mp3"));
        music.setLooping(true);
        music.play();
        music.setVolume(0.1f);

        for (String fileName : FILE_NAMES_JSON.get("character filmstrip").asStringArray()) {
            TextureRegion rawTexture = new TextureRegion(manager.get(fileName, Texture.class));
            int[] dims = getFilmStripDimensions(rawTexture, 64);
            TEXTURES.put(fileName, createFilmStrip(manager, fileName, dims[0], dims[1], dims[2]));
        }

        // Player & Items
//        int num_players = PLAYER_FILMSTRIP_FILES.length;
        PLAYER_FILMSTRIP = createFilmStrip(manager, PLAYER_FILMSTRIP_FILE, PLAYER_FILMSTRIP_ROW,
                PLAYER_FILMSTRIP_COL, PLAYER_FILMSTRIP_SIZE);
        PLAYER_HOLD_FILMSTRIP = createFilmStrip(manager, PLAYER_HOLDING_FILMSTRIP_FILE, PLAYER_HOLDING_FILMSTRIP_ROW,
                PLAYER_HOLDING_FILMSTRIP_COL, PLAYER_HOLDING_FILMSTRIP_SIZE);
        WOK = createTexture(manager, WOK_FILE);
        PLAYER_SHADOW = createTexture(manager, PLAYER_SHADOW_FILE);
        PLAYER_ARROW = createTexture(manager, PLAYER_ARROW_FILE);
                PLAYER_HOLDING_FILMSTRIP_COL, PLAYER_HOLDING_FILMSTRIP_SIZE);;
        WOK = createTexture(manager, WOK_FILE);
        PLAYER_SHADOW = createTexture(manager, PLAYER_SHADOW_FILE);
        PLAYER_ARROW = createTexture(manager, PLAYER_ARROW_FILE);

        // Firecracker filmstrip
        // TODO don't hardcode the rows/cols/size
        FIRECRACKER = createFilmStrip(manager, FIRECRACKER_FILE, 1, 1, 1);
        FIRECRACKER_LIT = createFilmStrip(manager, FIRECRACKER_LIT_FILE, 1, 5, 5);
        FIRECRACKER_DET = createFilmStrip(manager, FIRECRACKER_DET_FILE, 1, 7, 7);


        // Home stall textures
        HOME_STALL = createTexture(manager, HOME_STALL_FILE);

        // Level select screen
        LEVEL_SELECT_BACKGROUND = createTexture(manager, LEVEL_SELECT_BACKGROUND_FILE);
        TILE_1_TEXTURE = createTexture(manager, LEVEL1_TILE_FILE);
        TILE_2_TEXTURE = createTexture(manager, LEVEL2_TILE_FILE);
        TILE_3_TEXTURE = createTexture(manager, LEVEL3_TILE_FILE);
        STORE_1_TEXTURE = createTexture(manager, LEVEL1_STALL_FILE);
        STORE_2_TEXTURE = createTexture(manager, LEVEL2_STALL_FILE);
        STORE_3_TEXTURE = createTexture(manager, LEVEL3_STALL_FILE);
        ARROW_TEXTURE = createTexture(manager, ARROW_BUTTON_FILE);
        BACK_TEXTURE = createTexture(manager, BACK_BUTTON_FILE);
        HEADER_TEXTURE = createTexture(manager, HEADER_FILE);
        PLAYER_TEXTURE = createTexture(manager, PLAYER_FILE);

        FONT = manager.get(FILE_NAMES_JSON.getString("font"));
        MUSIC = manager.get("audio/Night_Bite_(Theme).mp3");

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
}
