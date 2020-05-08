package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    /** Flag to resume the game */
    private boolean resumeGame;
    /** The current state of the resume button */
    private int pressState;

    /** Exit code for heading back to game menu */
    public static final int EXIT_MENU = 1;
    /** Exit code for returning to the game */
    public static final int EXIT_RESUME = 2;

    /** Coordinate variables */
    private int heightY = 0;

    private int xLeftEnd = 0;
    private int yposTop = 0;

    private int xposMenu = 0;
    private int yposMenu = 0;

    /** Textures */
    private TextureRegion menuTexture;
    // private TextureRegion pauseTexture;
    // private TextureRegion resumeTexture;
    private TextureRegion background;


    public PauseController(GameCanvas canvas) {
        this.canvas = canvas;
        active = true;
        resumeGame = false;
    }

    /** Loads UI assets */
    public void loadContent() {
        background = Assets.getTextureRegion("level_select/Background.png");
        menuTexture = Assets.getTextureRegion("pause/MainMenuButton.png");
        // pauseTexture = Assets.getTextureRegion("pause/PauseTitle.png");
        // resumeTexture = Assets.getTextureRegion("pause/ResumeButton.png");
    }

    /** Sets this controller's screen listener */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
        Gdx.input.setInputProcessor(this);
    }

    /** TODO Not sure if need yet */
    private void update(float delta) { }

    /** Draw UI assets */
    public void draw() {
        canvas.begin();

        canvas.draw(background, Color.WHITE, 0, 0, 0, 0, 0, scale, scale);
        canvas.draw(menuTexture, Color.WHITE, menuTexture.getRegionWidth() / 2f, menuTexture.getRegionHeight() / 2f,
                    xposMenu, yposMenu, 0, scale, scale);

        canvas.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pressState == 2) return true;

        // flip to match graphics coordinates, origin on bottom left
        screenY = heightY - screenY;

        // change press state
        float halfWidthMenu = menuTexture.getRegionWidth() / 2f;
        float halfHeightMenu = menuTexture.getRegionHeight() / 2f;

        boolean inBoundXMenu = screenX >= xposMenu-halfWidthMenu && screenX <= xposMenu+halfWidthMenu;
        boolean inBoundYMenu = screenY >= yposMenu-halfHeightMenu && screenY <= yposMenu+halfHeightMenu;

        if (inBoundXMenu && inBoundYMenu) {
            pressState = 1;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressState == 1) {
            pressState = 2;
            return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (resumeGame && listener != null) {
                listener.exitScreen(this, EXIT_RESUME);
            } else if (pressState == 2 && listener != null) {
                listener.exitScreen(this, EXIT_MENU);
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

        xposMenu = width / 2;
        yposMenu = width / 4;

        // todo
        xLeftEnd = width / 16;
        yposTop = height * 7/8;
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
        pressState = 0;
        resumeGame = false;
    }
}
