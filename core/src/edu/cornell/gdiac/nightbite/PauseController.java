package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.util.ExitCodes;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * Pause screen controller
 */
public class PauseController implements Screen, InputProcessor {

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Screen listener for input */
    private ScreenListener listener;

    /** Scaling factor for when player changes the resolution. */
    private float scale;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH = 1920;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 960;

    /** Whether this player mode is still active */
    private boolean active;
    /** The current state of the menu button */
    private int pressStateMenu;
    /** The current state of the resume button */
    private int pressStateResume;

    /** Coordinate variables */
    private int heightY = 0;

    private int[] posResume = new int[]{0, 0};
    private int[] posMenu = new int[]{0, 0};
    private int[] posWASD = new int[]{0, 0};

    /** Textures */
    private TextureRegion menuTexture;
    // private TextureRegion pauseTexture;
    private TextureRegion resumeTexture;
    private TextureRegion background;

    private FilmStrip wasdTexture;

    /** How fast we change frames (one frame per 2 calls to update */
    private static final float ANIMATION_SPEED = 0.5f;
    /** Maximum number of frames before resetting to 0 */
    private static final float MAX_FRAMES = 120f;
    /** Current animation frame */
    private float frame;
    /** Number of frames in WASD animation */
    private int NUM_FRAMES_WASD;


    public PauseController(GameCanvas canvas) {
        this.canvas = canvas;
        active = true;
    }

    /** Loads UI assets */
    public void loadContent() {
        background = Assets.getTextureRegion("pause/Background.png");
        menuTexture = Assets.getTextureRegion("pause/MainMenuButton.png");
        // pauseTexture = Assets.getTextureRegion("pause/PauseTitle.png");
        resumeTexture = Assets.getTextureRegion("pause/ResumeButton.png");

        // WARNING: MAGIC NUMBERS for frame sizes
        wasdTexture = Assets.getFilmStrip("pause/WASD_FS.png", 144, 105);
        NUM_FRAMES_WASD = wasdTexture.getSize();
        System.out.println(NUM_FRAMES_WASD);
    }

    /** Sets this controller's screen listener */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
        Gdx.input.setInputProcessor(this);
    }

    /** Updates and sets filmstrip frames */
    private void update(float delta) {
        frame += ANIMATION_SPEED;
        if (frame >= MAX_FRAMES) {
            frame = 0f;
        }

        ((FilmStrip) wasdTexture).setFrame(((int) frame) % NUM_FRAMES_WASD);
    }

    /** Draw UI assets */
    public void draw() {
        canvas.begin();

        canvas.draw(background, Color.WHITE, 0, 0, 0, 0, 0, scale, scale);

        // Draw buttons
        Color tintMenu = (pressStateMenu == 1 ? Color.GRAY: Color.WHITE);
        Color tintResume = (pressStateResume == 1 ? Color.GRAY: Color.WHITE);
        canvas.draw(menuTexture, tintMenu, 0, 0,
                    posMenu[0], posMenu[1], 0, scale, scale);
        canvas.draw(resumeTexture, tintResume, Assets.getTextureCenterX(resumeTexture), Assets.getTextureCenterY(resumeTexture),
                    posResume[0], posResume[1], 0, scale, scale);

        // Draw filmstrips
        canvas.draw(wasdTexture, Color.WHITE, Assets.getTextureCenterX(wasdTexture), Assets.getTextureCenterY(wasdTexture),
                    posWASD[0], posWASD[1], 0, scale, scale);

        canvas.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pressStateMenu == 2 || pressStateResume == 2) return true;

        // flip to match graphics coordinates, origin on bottom left
        screenY = heightY - screenY;

        // change menu press state
        // jank alert: hard coded because of how the texture is drawn with origin 0,0 instead of a midway coord.
        boolean inBoundXMenu = screenX >= posMenu[0] && screenX <= posMenu[0]+menuTexture.getRegionWidth();
        boolean inBoundYMenu = screenY >= posMenu[1] && screenY <= posMenu[1]+menuTexture.getRegionHeight();

        if (inBoundXMenu && inBoundYMenu) {
            pressStateMenu = 1;
        }

        // change resume press state
        float halfWidthResume = resumeTexture.getRegionWidth() / 2f;
        float halfHeightResume = resumeTexture.getRegionHeight() / 2f;

        boolean inBoundXResume = screenX >= posResume[0]-halfWidthResume && screenX <= posResume[0]+halfWidthResume;
        boolean inBoundYResume = screenY >= posResume[1]-halfHeightResume && screenY <= posResume[1]+halfHeightResume;

        if (inBoundXResume && inBoundYResume) {
            pressStateResume = 1;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressStateMenu == 1) {
            pressStateMenu = 2;
            return true;
        }
        if (pressStateResume == 1) {
            pressStateResume = 2;
            return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (pressStateResume == 2 && listener != null) {
                listener.exitScreen(this, ExitCodes.LEVEL);
            } else if (pressStateMenu == 2 && listener != null) {
                listener.exitScreen(this, ExitCodes.SELECT);
            }
        }
    }

    /**
     * Sets the asset location references on resize
     */
    @Override
    public void resize(int width, int height) {
        float sx = ((float)width) / STANDARD_WIDTH;
        float sy = ((float)height) / STANDARD_HEIGHT;
        scale = Math.max(sx, sy);
        heightY = height;

        // Top left corner
        posMenu[0] = width / 16;
        posMenu[1] = height * 14/16;

        // Bottom middle
        posResume[0] = width / 2;
        posResume[1] = height / 4;

        // Tutorial images
        posWASD[0] = width / 4;
        posWASD[1] = height * 3/4;
    }

    /** Default stubs */

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(int amount) { return false; }

    @Override
    public void show() { active = true; }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { active = false; }

    @Override
    public void dispose() {
        pressStateMenu = 0;
        pressStateResume = 0;
        frame = 0f;
    }
}
