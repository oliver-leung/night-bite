package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.util.ExitCodes;
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
    private int xposResume = 0;
    private int yposResume = 0;
    private int xposMenu = 0;
    private int yposMenu = 0;

    /** Textures */
    private TextureRegion menuTexture;
    // private TextureRegion pauseTexture;
    private TextureRegion resumeTexture;
    private TextureRegion background;


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

        // Draw buttons
        Color tintMenu = (pressStateMenu == 1 ? Color.GRAY: Color.WHITE);
        Color tintResume = (pressStateResume == 1 ? Color.GRAY: Color.WHITE);
        canvas.draw(menuTexture, tintMenu, 0, 0,
                    xposMenu, yposMenu, 0, scale, scale);
        canvas.draw(resumeTexture, tintResume, resumeTexture.getRegionWidth() / 2f, resumeTexture.getRegionHeight() / 2f,
                    xposResume, yposResume, 0, scale, scale);

        canvas.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pressStateMenu == 2 || pressStateResume == 2) return true;

        // flip to match graphics coordinates, origin on bottom left
        screenY = heightY - screenY;

        // change menu press state
        // jank alert: hard coded because of how the texture is drawn with origin 0,0 instead of a midway coord.
        boolean inBoundXMenu = screenX >= xposMenu && screenX <= xposMenu+menuTexture.getRegionWidth();
        boolean inBoundYMenu = screenY >= yposMenu && screenY <= yposMenu+menuTexture.getRegionHeight();

        if (inBoundXMenu && inBoundYMenu) {
            pressStateMenu = 1;
        }

        // change resume press state
        float halfWidthResume = resumeTexture.getRegionWidth() / 2f;
        float halfHeightResume = resumeTexture.getRegionHeight() / 2f;

        boolean inBoundXResume = screenX >= xposResume-halfWidthResume && screenX <= xposResume+halfWidthResume;
        boolean inBoundYResume = screenY >= yposResume-halfHeightResume && screenY <= yposResume+halfHeightResume;

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
        xposMenu = width / 16;
        yposMenu = height * 14/16;

        // Bottom middle
        xposResume = width / 2;
        yposResume = height / 4;
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
    }
}
