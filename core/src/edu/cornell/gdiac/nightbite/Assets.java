package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.SoundController;

import java.util.HashMap;
import java.util.Map;

// The whole point of this class is to get all textures in a unified location.
// This makes it easier for assets to be data driven (defining assets in json files)

// It also makes more sense than making asset loading span across LoadingMode and WorldController
// (And also slightly better than cramming everything into LoadingMode, which also needs to handle
// drawing/other functions

public class Assets {
    /** Sound effect volume */
    public static float VOLUME = 0.1f;
    /** Asset Manager */
    private static AssetManager manager;
    /** Mapping from file names to in-game film strip assets */
    private static Map<String, FilmStrip> filmStrips = new HashMap<>();
    /** Mapping from file names to in-game texture assets */
    private static Map<String, TextureRegion> textureRegions = new HashMap<>();
    /** In-game music assets */
    private static Map<String, Music> musics = new HashMap<>();
    /** Currently played music */
    private static Music activeMusic;
    /** Whether sound effects/music are muted */
    private static boolean isMuted = false;
    /** In-game font asset */
    private static BitmapFont font;
    /** Reference to the sound effect controller */
    private final SoundController soundController = SoundController.getInstance();
    /** Track all loaded assets (for unloading purposes) */
    private Array<String> assets = new Array<>();
    /** Names of all files to be loaded */
    private String[] fileNames;
    /** Track load status */
    private boolean isLoaded = false;

    public Assets(AssetManager manager) {
        Assets.manager = manager;

        JsonReader jsonReader = new JsonReader();
        FileHandle assetsJson = Gdx.files.internal("assets.json");
        fileNames = jsonReader.parse(assetsJson).get("assets").asStringArray();

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
     * @param pixels        height/width of one frame in pixels
     * @return [rows, cols, size]
     */
    private static int[] getFilmStripDimensions(TextureRegion textureRegion, int pixels) {
        int rows = textureRegion.getRegionHeight() / pixels;
        int cols = textureRegion.getRegionWidth() / pixels;
        int size = rows * cols;

        return new int[]{rows, cols, size};
    }

    /**
     * Get the FilmStrip dimensions of a TextureRegion (if it were a FilmStrip)
     *
     * @param textureRegion Raw texture region
     * @param width         The width of one frame
     * @param height        The height of one frame
     * @return [rows, cols, size]
     */
    private static int[] getFilmStripDimensions(TextureRegion textureRegion, int width, int height) {
        int rows = textureRegion.getRegionHeight() / height;
        int cols = textureRegion.getRegionWidth() / width;
        int size = rows * cols;

        return new int[]{rows, cols, size};
    }

    /**
     * Get the FilmStrip associated with this filename, assuming that each frame is 64 x 64 pixels
     *
     * @param fileName File name of FilmStrip
     * @return Associated FilmStrip
     */
    public static FilmStrip getFilmStrip(String fileName) {
        return getFilmStrip(fileName, 64);
    }

    /**
     * Get the FilmStrip associated with this filename
     *
     * @param fileName File name of FilmStrip
     * @param pixels   Width/height of each frame in pixels
     * @return Associated FilmStrip
     */
    public static FilmStrip getFilmStrip(String fileName, int pixels) {
        if (filmStrips.get(fileName) == null) {
            TextureRegion rawTexture = textureRegions.get(fileName);
            int[] dims = getFilmStripDimensions(rawTexture, pixels);
            filmStrips.put(fileName, createFilmStrip(manager, fileName, dims[0], dims[1], dims[2]));
        }
        return new FilmStrip(filmStrips.get(fileName));
    }

    /**
     * Get the FilmStrip associated with this filename
     *
     * @param fileName File name of FilmStrip
     * @param width    The width of one frame
     * @param height   The height of one frame
     * @return Associated FilmStrip
     */
    public static FilmStrip getFilmStrip(String fileName, int width, int height) {
        if (filmStrips.get(fileName) == null) {
            TextureRegion rawTexture = textureRegions.get(fileName);
            int[] dims = getFilmStripDimensions(rawTexture, width, height);
            filmStrips.put(fileName, createFilmStrip(manager, fileName, dims[0], dims[1], dims[2]));
        }
        return new FilmStrip(filmStrips.get(fileName));
    }

    public static Music getMusic(String filename) {
        return musics.get(filename);
    }

    protected static TextureRegion createTexture(AssetManager manager, String file) {
        return createTexture(manager, file, true);
    }

    /**
     * Returns a newly loaded filmstrip for the given file.
     * <p>
     * This helper methods is used to set texture settings (such as scaling, and
     * the number of animation frames) after loading.
     */
    private static FilmStrip createFilmStrip(AssetManager manager, String file, int rows, int cols, int size) {
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
    private static TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
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

    public static Music getActiveMusic() {
        return activeMusic;
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

    private void loadSound(String filePath) {
        manager.load(filePath, Sound.class);
        assets.add(filePath);
    }

    private void loadMusic(String filePath) {
        manager.load(filePath, Music.class);
        assets.add(filePath);
    }

    private void loadTexture(String filePath) {
        manager.load(filePath, Texture.class);
        assets.add(filePath);
    }

    private void loadFont(String fontPath, int fontSize) {
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
                    loadFont(fileName, 36); // TODO: Let's make this a more reasonable size
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
        isLoaded = true;
    }

    public static void playMusic(String fileName, boolean loop) {
        if (activeMusic != null) {
            stopMusic();
        }

        activeMusic = getMusic(fileName);
        activeMusic.setOnCompletionListener(music -> {System.out.println("done");});
        activeMusic.setLooping(loop);
        // activeMusic.setVolume(0);
        activeMusic.setVolume(VOLUME * 0.7f);
        activeMusic.play();
        // activeMusic.setVolume(0);
        // activeMusic.setVolume(VOLUME * 0.7f);
    }

    public static void pauseMusic() {
        if (activeMusic != null) {
            activeMusic.pause();

        }
    }

    public static void resumeMusic() {
        if (activeMusic != null) {
            activeMusic.play();

        }
    }

    public static void stopMusic() {
        if (activeMusic != null) {
            activeMusic.stop();
        }
        activeMusic = null;
    }

    public static void changeMute() {
        if (!isMuted) {
            isMuted = true;
            VOLUME = 0;
        } else {
            isMuted = false;
            VOLUME = 0.1f;
        }
        changeMusicVolume();
    }

    private static void changeMusicVolume() {
        if (activeMusic != null) {
//            System.out.println("vol");
            activeMusic.setVolume(VOLUME * 0.7f);
        }
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
     * Retrieves a texture's center (x coordinate) as an int
     * @param texture
     * @return Center x coordinate
     */
    public static int getTextureCenterX (TextureRegion texture) {
        return texture.getRegionWidth() / 2;
    }

    /**
     * Retrieves a texture's center (y coordinate) as an int
     * @param texture
     * @return Center y coordinate
     */
    public static int getTextureCenterY (TextureRegion texture) {
        return texture.getRegionHeight() / 2;
    }

}
