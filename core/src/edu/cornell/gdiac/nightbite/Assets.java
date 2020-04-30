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
    /** Sound effect volume */
    public static float EFFECT_VOLUME = 0.1f;
    /** Asset Manager */
    private static AssetManager manager;
    /** Mapping from file names to in-game film strip assets */
    private static Map<String, FilmStrip> filmStrips = new HashMap<>();
    /** Reference to the sound effect controller */
    private final SoundController soundController = SoundController.getInstance();
    /** File path to assets */
    private URI assetsUri;
    /** Track all loaded assets (for unloading purposes) */
    private Array<String> assets = new Array<>();

    /** Mapping from file names to in-game texture assets */
    private static Map<String, TextureRegion> textureRegions = new HashMap<>();
    /** Names of all files to be loaded */
    private List<String> fileNames = new ArrayList<>();
    /** In-game music asset */
    private static Map<String, Music> musics = new HashMap<>();
    /** In-game font asset */
    private static BitmapFont font;
    /** Track load status */
    private boolean isLoaded = false;

    public Assets(AssetManager manager) {
        Assets.manager = manager;

        File assets = new File(Gdx.files.getLocalStoragePath());
        assetsUri = assets.toURI();

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
     * Get the FilmStrip associated with this filename, assuming that each frame is 64 x 64 pixels
     *
     * @param fileName File name of FilmStrip
     * @return Associated FilmStrip
     */
    public static FilmStrip getFilmStrip(String fileName) {
        return getFilmStrip(fileName, 64);
    }

    public static FilmStrip getFilmStrip(String fileName, int pixels) {
        if (filmStrips.get(fileName) == null) {
            TextureRegion rawTexture = textureRegions.get(fileName);
            int[] dims = getFilmStripDimensions(rawTexture, pixels);
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

    protected static TextureRegion createTexture(AssetManager manager, String file) {
        return createTexture(manager, file, true);
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
            fileNames.add(assetsUri.relativize(asset.toURI()).toString());
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
