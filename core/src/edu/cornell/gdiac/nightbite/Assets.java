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

        for (String fileName : FILE_NAMES_JSON.get("character filmstrip").asStringArray()) {
            TextureRegion rawTexture = new TextureRegion(manager.get(fileName, Texture.class));
            int[] dims = getFilmStripDimensions(rawTexture, 64);
            TEXTURES.put(fileName, createFilmStrip(manager, fileName, dims[0], dims[1], dims[2]));
        }

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
        int rows = textureRegion.getRegionWidth() / pixels;
        int cols = textureRegion.getRegionHeight() / pixels;
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
