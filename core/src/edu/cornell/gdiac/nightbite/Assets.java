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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// The whole point of this class is to get all textures in a unified location.
// This makes it easier for assets to be data driven (defining assets in json files)

// It also makes more sense than making asset loading span across LoadingMode and WorldController
// (And also slightly better than cramming everything into LoadingMode, which also needs to handle
// drawing/other functions

public class Assets {
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
    // Sound
    public static float EFFECT_VOLUME = 0.1f;
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
    public static FilmStrip FIRE_ENEMY_WALK;
    public static FilmStrip FIRE_ENEMY_FALL;
    public static FilmStrip OIL_ENEMY_WALK;
    public static FilmStrip OIL_ENEMY_FALL;
    public static FilmStrip FIRECRACKER;
    public static FilmStrip FIRECRACKER_LIT;
    public static FilmStrip FIRECRACKER_DET;
    public static FilmStrip OIL_SPILLING;
    public static FilmStrip OIL_TILE;
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
    static String MUSIC_FILE = "audio/Night_Bite_(Theme)_v2.wav";
    /** Mapping from file names to in-game texture assets */
    private static Map<String, TextureRegion> textureRegions = new HashMap<>();
    private static Map<String, FilmStrip> filmStrips = new HashMap<>();
    /** In-game music asset */
    private static Map<String, Music> musics = new HashMap<>();
    /** Mapping from file names to in-game sound effects */
    private static Map<String, Sound> sounds = new HashMap<>();
    /** In-game font asset */
    private static BitmapFont font;
    /** Keys that map to static texture file names */
    private final String[] textureKeys = new String[]{"ground", "decoration", "hole", "character", "team", "wall", "item"};
    private final SoundController soundController = SoundController.getInstance();
    // TODO: Delete these filmstrip constants
    int PLAYER_FILMSTRIP_ROW = 1;
    int PLAYER_FILMSTRIP_COL = 8;
    int PLAYER_FILMSTRIP_SIZE = 8;
    private URI uri;
    /** Asset Manager */
    private static AssetManager manager;
    /** Track load status */
    private boolean isLoaded = false;
    /** Track all loaded assets (for unloading purposes) */
    private Array<String> assets = new Array<>();
    /** JSON Reader */
    private JsonReader jsonReader = new JsonReader();
    /** JSON of asset file names */
    private JsonValue FILE_NAMES_JSON;
    private List<String> fileNames = new ArrayList<>();

    public Assets(AssetManager manager) {
        Assets.manager = manager;
        FILE_NAMES_JSON = jsonReader.parse(Gdx.files.internal("assets.json"));

        File assets = new File(Gdx.files.getLocalStoragePath());
        uri = assets.toURI();

        listAssets(assets);
        preLoadContent();
    }

    public static BitmapFont getFont() {
        return font;
    }

    public static TextureRegion getTextureRegion(String fileName) {
        return new TextureRegion(textureRegions.get(fileName));
    }

    /**
     * Get the FilmStrip dimensions of a TextureRegion (if it were a FilmStrip)
     *
     * @param textureRegion Raw texture region
     * @return [rows, cols, size]
     */
    private static int[] getFilmStripDimensions(TextureRegion textureRegion) {
        int rows = textureRegion.getRegionHeight() / 64;
        int cols = textureRegion.getRegionWidth() / 64;
        int size = rows * cols;

        return new int[]{rows, cols, size};
    }

    public static FilmStrip getFilmStrip(String fileName) {
        if (filmStrips.get(fileName) == null) {
            TextureRegion rawTexture = textureRegions.get(fileName);
            int[] dims = getFilmStripDimensions(rawTexture);
            filmStrips.put(fileName, createFilmStrip(manager, fileName, dims[0], dims[1], dims[2]));
        }
        return new FilmStrip(filmStrips.get(fileName));
    }

    public static Music getMusic(String filename) {
        return musics.get(filename);
    }

    /**
     * Returns the file extension in a file name. If it doesn't have one, return an empty string.
     *
     * @param fileName File name
     * @return File extension of fileName in lower case
     */
    private String getExtension(String fileName) {
        String extension = "";
        int idx = fileName.lastIndexOf(".");
        if (idx > 0) extension = fileName.substring(idx + 1).toLowerCase();

        return extension;
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
     * Recursively add all files contained by a file/directory to the list of all file names
     *
     * @param asset File/directory to be searched
     */
    private void listAssets(File asset) {
        if (asset.isFile()) {
            fileNames.add(uri.relativize(asset.toURI()).toString());
        } else if (asset.isDirectory()) {
            File[] subAssets = asset.listFiles();
            assert subAssets != null;

            for (File subAsset : subAssets) {
                listAssets(subAsset);
            }
        }
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

    /** Preload the texture and sound information for the game */
    private void preLoadContent() {
        for (String fileName : fileNames) {
            switch (getExtension(fileName)) {
                case "png":
                    loadTexture(fileName);
                    break;
                case "mp3":
                    loadMusic(fileName);
                    break;
                case "wav":
                    loadSound(fileName);
                    break;
                case "ttf":
                    loadFont(fileName, 12); // TODO: Let's make this a more reasonable size
                    break;
            }
        }
    }

    private Music createMusic(AssetManager manager, String file) {
        if (manager.isLoaded(file)) {
            return manager.get(file, Music.class);
        }
        return null;
    }

    /**
     * Loads the texture and sound information for the game :
     * extracting assets from the manager after it has finished loading them
     */
    public void loadContent(AssetManager manager) {
        for (String fileName : fileNames) {
            switch (getExtension(fileName)) {
                case "png":
                    textureRegions.put(fileName, createTexture(manager, fileName));
                    break;
                case "mp3":
                    musics.put(fileName, createMusic(manager, fileName));
                    break;
                case "wav":
                    soundController.allocate(manager, fileName);
                    break;
                case "ttf":
                    font = manager.get(fileName);
                    break;
            }
        }

        playMusic();

        // Player & Items
        PLAYER_FILMSTRIP = getFilmStrip("character/Filmstrip/Player 1/P1_Dash_5.png");
        PLAYER_HOLD_FILMSTRIP = getFilmStrip("character/Filmstrip/Player_1/P1_Holding_8.png");
        PLAYER_FALL_FILMSTRIP = createFilmStrip(manager, PLAYER_FALLING_FILMSTRIP_FILE, PLAYER_FALLING_FILMSTRIP_ROW,
                PLAYER_FALLING_FILMSTRIP_COL, PLAYER_FALLING_FILMSTRIP_SIZE);
        WOK = createTexture(manager, WOK_FILE, true);
        PLAYER_SHADOW = createTexture(manager, PLAYER_SHADOW_FILE, true);
        PLAYER_ARROW = createTexture(manager, PLAYER_ARROW_FILE, true);

        // Enemies
        FIRE_ENEMY_WALK = createFilmStrip(manager, FIRE_ENEMY_WALK_FILE, 1, 8, 8);
        FIRE_ENEMY_FALL = createFilmStrip(manager, FIRE_ENEMY_FALL_FILE, 1, 6, 6);
        OIL_ENEMY_WALK = createFilmStrip(manager, OIL_ENEMY_WALK_FILE, 1, 8, 8);
        OIL_ENEMY_FALL = createFilmStrip(manager, OIL_ENEMY_FALL_FILE, 1, 6, 6);

        // Firecracker filmstrip
        // TODO don't hardcode the rows/cols/size
        FIRECRACKER = createFilmStrip(manager, FIRECRACKER_FILE, 1, 1, 1);
        FIRECRACKER_LIT = createFilmStrip(manager, FIRECRACKER_LIT_FILE, 1, 5, 5);
        FIRECRACKER_DET = createFilmStrip(manager, FIRECRACKER_DET_FILE, 1, 7, 7);

        // Oil
        OIL_SPILLING = createFilmStrip(manager, OIL_SPILLING_FILE, 1, 12, 12);
        OIL_TILE = createFilmStrip(manager, OIL_TILE_FILE, 1, 1, 1);

        // Home stall textures
        HOME_STALL = createFilmStrip(manager, HOME_STALL_FILE, 1, 4, 4);

        // Level select screen
        LEVEL_SELECT_BACKGROUND = createTexture(manager, LEVEL_SELECT_BACKGROUND_FILE, true);
        TILE_1_TEXTURE = createTexture(manager, LEVEL1_TILE_FILE, true);
        TILE_2_TEXTURE = createTexture(manager, LEVEL2_TILE_FILE, true);
        TILE_3_TEXTURE = createTexture(manager, LEVEL3_TILE_FILE, true);
        STORE_1_TEXTURE = createTexture(manager, LEVEL1_STALL_FILE, true);
        STORE_2_TEXTURE = createTexture(manager, LEVEL2_STALL_FILE, true);
        STORE_3_TEXTURE = createTexture(manager, LEVEL3_STALL_FILE, true);
        ARROW_TEXTURE = createTexture(manager, ARROW_BUTTON_FILE, true);
        BACK_TEXTURE = createTexture(manager, BACK_BUTTON_FILE, true);
        HEADER_TEXTURE = createTexture(manager, HEADER_FILE, true);
        PLAYER_TEXTURE = createTexture(manager, PLAYER_FILE, true);

        isLoaded = true;
    }

    // TODO: Potentially refactor this out to another class, and change to v2
    private void playMusic() {
        Music music = musics.get("audio/Night_Bite_(Theme).mp3");
        music.setLooping(true);
        music.play();
        music.setVolume(0.1f);
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
}
