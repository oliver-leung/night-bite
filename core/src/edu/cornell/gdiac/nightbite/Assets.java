package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

// The whole point of this class is to get all textures in a unified location.
// This makes it easier for assets to be data driven (defining assets in json files)

// It also makes more sense than making asset loading span across LoadingMode and WorldController
// (And also slightly better than cramming everything into LoadingMode, which also needs to handle
// drawing/other functions

public class Assets {
    private HashMap<String, Array<String>> assets;
    private boolean isLoaded;
    private AssetManager manager;

    private static Assets instance;

    public static Assets getInstance() {
        if (instance == null) {
            instance = new Assets();
        }
        return instance;
    }

    private Assets() {
    }

    public void setManager(AssetManager manager) {
        this.manager = manager;
    }

    public static TextureRegion createTexture(String name) {
        return null;
    }

    public boolean getLoaded() {
        return isLoaded;
    }

    public void load() {
        // assets = new HashMap<>();


    }

    public void loadFile(AssetManager manager, String name, String fileLocation) {
        manager.load(fileLocation, Texture.class);
    }

    public void loadFilmStrip() {

    }

    public void unload() {

    }
}
