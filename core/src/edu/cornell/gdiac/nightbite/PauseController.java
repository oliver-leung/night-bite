package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
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
    /** The current state of the resume button */
    private int pressStateControls;

    /** Coordinate variables */
    private int heightY = 0;

    private int[] posTitle = new int[]{0, 0};
    private int[] posResume = new int[]{0, 0};
    private int[] posMenu = new int[]{0, 0};
    private int[] posWASD = new int[]{0, 0};
    private int[] posEsc = new int[]{0, 0};
    private int[] posM = new int[]{0, 0};

    private int[] posControlsText = new int[]{0, 0};
    private int[] posMoveText = new int[]{0, 0};
    private int[] posEscText = new int[]{0, 0};
    private int[] posWhackText = new int[]{0, 0};
    private int[] posMuteText = new int[]{0, 0};

    private int[] posMouse = new int[]{0, 0};
    private int[] posEnter = new int[]{0, 0};

    /** Display font */
    private BitmapFont displayFont;

    /** Textures */
    private TextureRegion menuTexture;
    private TextureRegion pauseTexture;
    private TextureRegion resumeTexture;
    private TextureRegion background;

    private TextureRegion wasdTexture;
    private TextureRegion escTexture;
    private TextureRegion mTexture;

    private TextureRegion mouseActiveTexture;
    private TextureRegion mouseDisabledTexture;
    private TextureRegion enterActiveTexture;
    private TextureRegion enterDisabledTexture;

    /** Controls whether the controls are mouse or not */
    private boolean isMouseControls = true;
    private boolean escapeButton = false;

    public PauseController(GameCanvas canvas) {
        this.canvas = canvas;
        active = true;
    }

    /** Loads UI assets */
    public void loadContent() {
        displayFont = Assets.getFont();

        background = Assets.getTextureRegion("pause/Background.png");
        menuTexture = Assets.getTextureRegion("pause/MainMenuButton.png");
        pauseTexture = Assets.getTextureRegion("pause/PauseTitle.png");
        resumeTexture = Assets.getTextureRegion("pause/ResumeButton.png");

        wasdTexture = Assets.getTextureRegion("pause/WASD.png");
        escTexture = Assets.getTextureRegion("pause/esc.png");
        mTexture = Assets.getTextureRegion("pause/M.png");

        mouseActiveTexture = Assets.getTextureRegion("pause/Mouse_Active.png");
        mouseDisabledTexture = Assets.getTextureRegion("pause/Mouse_Disabled.png");
        enterActiveTexture = Assets.getTextureRegion("pause/Enter_Active.png");
        enterDisabledTexture = Assets.getTextureRegion("pause/Enter_Disabled.png");

    }

    /** Sets this controller's screen listener */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
        Gdx.input.setInputProcessor(this);
    }

    /** Updates and sets filmstrip frames */
    private void update(float delta) {

    }

    /** Draw UI assets */
    public void draw() {
        canvas.begin();

        canvas.draw(background, Color.WHITE, 0, 0, 0, 0, 0, scale, scale);

        // Draw buttons
        Color tintMenu = (pressStateMenu == 1 ? Color.GRAY: Color.WHITE);
        Color tintResume = (pressStateResume == 1 && !escapeButton ? Color.GRAY: Color.WHITE);
        canvas.draw(pauseTexture, Color.WHITE,Assets.getTextureCenterX(pauseTexture), Assets.getTextureCenterY(pauseTexture),
                    posTitle[0], posTitle[1], 0, scale, scale);
        canvas.draw(menuTexture, tintMenu, 0, 0, posMenu[0], posMenu[1], 0, scale, scale);
        canvas.draw(resumeTexture, tintResume, Assets.getTextureCenterX(resumeTexture), Assets.getTextureCenterY(resumeTexture),
                    posResume[0], posResume[1], 0, scale, scale);

        if (isMouseControls) {
            canvas.draw(mouseActiveTexture, Color.WHITE, 0, 0, posMouse[0], posMouse[1], 0, scale, scale);
            canvas.draw(enterDisabledTexture, Color.WHITE, 0, 0, posEnter[0], posEnter[1], 0, scale, scale);
        } else {
            canvas.draw(mouseDisabledTexture, Color.WHITE, 0, 0, posMouse[0], posMouse[1], 0, scale, scale);
            canvas.draw(enterActiveTexture, Color.WHITE, 0, 0, posEnter[0], posEnter[1], 0, scale, scale);
        }

        // Draw filmstrips
        canvas.draw(wasdTexture, Color.WHITE, Assets.getTextureCenterX(wasdTexture), Assets.getTextureCenterY(wasdTexture),
                    posWASD[0], posWASD[1], 0, scale, scale);
        canvas.draw(escTexture, Color.WHITE, Assets.getTextureCenterX(escTexture), escTexture.getRegionHeight()-15,  // hardcode origin to almost top left
                    posEsc[0], posEsc[1], 0, scale, scale);
        canvas.draw(mTexture, Color.WHITE, Assets.getTextureCenterX(mTexture), mTexture.getRegionHeight()-15,
                    posM[0], posM[1], 0, scale, scale);

        // Draw text
        canvas.drawTextCentered("Controls", displayFont, posControlsText[1]);
        canvas.drawText("Move", displayFont, posMoveText[0], posMoveText[1]);
        canvas.drawText("Pause", displayFont, posEscText[0], posEscText[1]);
        canvas.drawText("Whack", displayFont, posWhackText[0], posWhackText[1]);
        canvas.drawText("Mute", displayFont, posMuteText[0], posMuteText[1]);

        canvas.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pressStateMenu == 2 || pressStateResume == 2 || pressStateControls == 2) return true;

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

        // change mouse/keyboard control state
        float halfWidthMouse = mouseDisabledTexture.getRegionWidth() / 2f;
        float halfHeightMouse = mouseDisabledTexture.getRegionHeight() / 2f;
        float halfWidthEnter = enterDisabledTexture.getRegionWidth() / 2f;
        float halfHeightEnter = enterDisabledTexture.getRegionHeight() / 2f;

        boolean inBoundXMouse = screenX >= posMouse[0]-halfWidthMouse && screenX <= posMouse[0]+halfWidthMouse;
        boolean inBoundYMouse = screenY >= posResume[1]-halfHeightMouse && screenY <= posMouse[1]+halfHeightMouse;
        boolean inBoundXEnter = screenX >= posEnter[0]-halfWidthEnter && screenX <= posEnter[0]+halfWidthEnter;
        boolean inBoundYEnter = screenY >= posEnter[1]-halfHeightEnter && screenY <= posEnter[1]+halfHeightEnter;

        if (inBoundXMouse && inBoundYMouse) {
            pressStateControls = 1;
            if (!isMouseControls) {
                isMouseControls = true;
            }
        } else if (inBoundXEnter && inBoundYEnter) {
            pressStateControls = 1;
            if (isMouseControls) {
                isMouseControls = false;
            }
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
        if (pressStateControls == 1) {
            pressStateControls = 2;
            return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (pressStateControls == 2) {
                KeyboardMap.mouse = isMouseControls;
                pressStateControls = 0;
            }

            if (pressStateResume == 2 && listener != null) {
                listener.exitScreen(this, ExitCodes.LEVEL);
            } else if (pressStateMenu == 2 && listener != null) {
                listener.exitScreen(this, ExitCodes.SELECT);
            }
        }
    }

    /**
     * Sets the asset location references on resize and start
     */
    @Override
    public void resize(int width, int height) {
        float sx = ((float)width) / STANDARD_WIDTH;
        float sy = ((float)height) / STANDARD_HEIGHT;
        scale = Math.max(sx, sy);
        heightY = height;

        int row1 = height * 11/16;
        int row2 = height * 19/32;
        int row3 = height * 7/16;
        int row4 = height * 11/32;

        int col1 = width * 8/32;
        int col2 = width * 22/32;

        // Top left corner
        posMenu[0] = width / 16;
        posMenu[1] = height * 14/16;

        // Bottom center
        posResume[0] = width / 2;
        posResume[1] = height / 8;

        // Top center
        posTitle[0] = width / 2;
        posTitle[1] = height * 7/8;
        posControlsText[1] = height * 8/32; // offset from center y

        // Col 1, up to down
        posMoveText[0] = col1;
        posMoveText[1] = row1;
        posWASD[0] = col1 + 35;
        posWASD[1] = row2 - 10;
        posWhackText[0] = col1 - 5;
        posWhackText[1] = row3;

        posMouse[0] = width * 6/32 - 30;
        posMouse[1] = row4 - 50;
        posEnter[0] = width * 10/32 - 30;
        posEnter[1] = row4 - 50;

        // Col 2, up to down
        posEscText[0] = col2;
        posEscText[1] = row1;
        posEsc[0] = col2 + 45;
        posEsc[1] = row2;
        posMuteText[0] = col2 + 10;
        posMuteText[1] = row3;
        posM[0] = col2 + 45;
        posM[1] = row4;

    }

    /** Default stubs */

    @Override
    public boolean keyDown(int keycode) {
        if (pressStateMenu == 2 || pressStateResume == 2 | pressStateControls == 2) return true;
        else if (keycode == Input.Keys.ESCAPE) {
            pressStateResume = 1;
            escapeButton = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (pressStateResume == 1) {
            pressStateResume = 2;
            escapeButton = false;
            return true;
        }
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
        pressStateControls = 0;
    }
}
