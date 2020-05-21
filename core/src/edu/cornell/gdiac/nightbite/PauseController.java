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

    /** Coordinate variables */
    private int heightY = 0;

    private int[] posTitle = new int[]{0, 0};
    private int[] posResume = new int[]{0, 0};
    private int[] posMenu = new int[]{0, 0};
    private int[] posWASDSprite = new int[]{0, 0};
    private int[] posWASD = new int[]{0, 0};
    private int[] posWhackSprite = new int[]{0, 0};
    private int[] posMouse = new int[]{0, 0};
    private int[] posPickupSprite = new int[]{0, 0};
    private int[] posSpace = new int[]{0, 0};
    private int[] posEsc = new int[]{0, 0};
    private int[] posM = new int[]{0, 0};
    private int[] posR = new int[]{0, 0};

    private int[] posControlsText = new int[]{0, 0};
    private int[] posMoveText = new int[]{0, 0};
    private int[] posEscText = new int[]{0, 0};
    private int[] posWhackText = new int[]{0, 0};
    private int[] posMuteText = new int[]{0, 0};
    private int[] posPickupText = new int[]{0, 0};
    private int[] posRestartText = new int[]{0, 0};

    /** Display font */
    private BitmapFont displayFont;

    /** Textures */
    private TextureRegion menuTexture;
    private TextureRegion pauseTexture;
    private TextureRegion resumeTexture;
    private TextureRegion background;

    private FilmStrip wasdSpriteTexture;
    private FilmStrip wasdTexture;
    private FilmStrip whackSpriteTexture;
    private FilmStrip mouseTexture;
    private FilmStrip spaceTexture;
    private FilmStrip pickupSpriteTexture;
    private FilmStrip escTexture;
    private FilmStrip mTexture;
    private FilmStrip rTexture;

    private boolean escapeButton = false;

    /** How fast we change frames (one frame per 5 calls to update */
    private static final float ANIMATION_SPEED = 0.2f;
    /** Maximum number of frames before resetting to 0 */
    private static final float MAX_FRAMES = 120f;
    /** Current animation frame */
    private float frame;
    /** Number of frames in animations */
    private int NUM_FRAMES_WASDSPRITE;
    private int NUM_FRAMES_WASD;
    private int NUM_FRAMES_WHACKSPRITE;
    private int NUM_FRAMES_MOUSE;
    private int NUM_FRAMES_PICKUPSPRITE;
    private int NUM_FRAMES_SPACE;
    private int NUM_FRAMES_ESC;
    private int NUM_FRAMES_M;
    private int NUM_FRAMES_R;


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

        // WARNING: MAGIC NUMBERS for frame sizes
        wasdTexture = Assets.getFilmStrip("pause/WASD_FS.png", 144, 105);
        NUM_FRAMES_WASD = wasdTexture.getSize();
        wasdSpriteTexture = Assets.getFilmStrip("pause/WASDSprite_FS.png", 128, 128);
        NUM_FRAMES_WASDSPRITE = wasdSpriteTexture.getSize();
        mouseTexture = Assets.getFilmStrip("pause/Mouse_FS.png", 78, 100);
        NUM_FRAMES_MOUSE = mouseTexture.getSize();
        whackSpriteTexture = Assets.getFilmStrip("pause/Whack_FS_5.png", 152, 96);
        NUM_FRAMES_WHACKSPRITE = whackSpriteTexture.getSize();
        pickupSpriteTexture = Assets.getFilmStrip("pause/PickUpThrow_FS_5.png", 128, 128);
        NUM_FRAMES_PICKUPSPRITE = pickupSpriteTexture.getSize();
        spaceTexture = Assets.getFilmStrip("pause/Space_FS_5.png", 246, 55);
        NUM_FRAMES_SPACE = spaceTexture.getSize();
        escTexture = Assets.getFilmStrip("pause/esc_FS.png", 48, 53);
        NUM_FRAMES_ESC = escTexture.getSize();
        mTexture = Assets.getFilmStrip("pause/M_FS.png", 48, 53);
        NUM_FRAMES_M = mTexture.getSize();
        rTexture = Assets.getFilmStrip("pause/R_FS.png", 48, 53);
        NUM_FRAMES_R = rTexture.getSize();
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

        wasdTexture.setFrame(((int) frame) % NUM_FRAMES_WASD);
        wasdSpriteTexture.setFrame(((int) frame) % NUM_FRAMES_WASDSPRITE);
        mouseTexture.setFrame(((int) frame) % NUM_FRAMES_MOUSE);
        whackSpriteTexture.setFrame(((int) frame) % NUM_FRAMES_WHACKSPRITE);
        pickupSpriteTexture.setFrame(((int) frame) % NUM_FRAMES_PICKUPSPRITE);
        spaceTexture.setFrame(((int) frame) % NUM_FRAMES_SPACE);
        escTexture.setFrame(((int) frame) % NUM_FRAMES_ESC);
        mTexture.setFrame(((int) frame) % NUM_FRAMES_M);
        rTexture.setFrame(((int) frame) % NUM_FRAMES_R);
    }

    /** Draw UI assets */
    public void draw() {
        canvas.begin();

        canvas.draw(background, Color.WHITE, 0, 0, 0, 0, 0, scale, scale);

        // Draw buttons
        Color tintMenu = (pressStateMenu == 1 ? Color.GRAY: Color.WHITE);
        Color tintResume = (pressStateResume == 1 && escapeButton == false ? Color.GRAY: Color.WHITE);
        canvas.draw(pauseTexture, Color.WHITE,Assets.getTextureCenterX(pauseTexture), Assets.getTextureCenterY(pauseTexture),
                    posTitle[0], posTitle[1], 0, scale, scale);
        canvas.draw(menuTexture, tintMenu, 0, 0, posMenu[0], posMenu[1], 0, scale, scale);
        canvas.draw(resumeTexture, tintResume, Assets.getTextureCenterX(resumeTexture), Assets.getTextureCenterY(resumeTexture),
                    posResume[0], posResume[1], 0, scale, scale);

        // Draw filmstrips
        canvas.draw(wasdTexture, Color.WHITE, Assets.getTextureCenterX(wasdTexture), Assets.getTextureCenterY(wasdTexture),
                    posWASD[0], posWASD[1], 0, scale, scale);
        canvas.draw(wasdSpriteTexture, Color.WHITE, Assets.getTextureCenterX(wasdSpriteTexture), Assets.getTextureCenterY(wasdSpriteTexture),
                    posWASDSprite[0], posWASDSprite[1], 0, scale, scale);
        canvas.draw(mouseTexture, Color.WHITE, Assets.getTextureCenterX(mouseTexture), Assets.getTextureCenterY(mouseTexture),
                    posMouse[0], posMouse[1], 0, scale, scale);
        canvas.draw(whackSpriteTexture, Color.WHITE, Assets.getTextureCenterX(whackSpriteTexture), Assets.getTextureCenterY(whackSpriteTexture),
                    posWhackSprite[0], posWhackSprite[1], 0, scale, scale);
        canvas.draw(pickupSpriteTexture, Color.WHITE, Assets.getTextureCenterX(pickupSpriteTexture), Assets.getTextureCenterY(pickupSpriteTexture),
                    posPickupSprite[0], posPickupSprite[1], 0, scale, scale);
        canvas.draw(spaceTexture, Color.WHITE, Assets.getTextureCenterX(spaceTexture), Assets.getTextureCenterY(spaceTexture),
                    posSpace[0], posSpace[1], 0, scale, scale);
        canvas.draw(escTexture, Color.WHITE, Assets.getTextureCenterX(escTexture), escTexture.getRegionHeight()-15,  // hardcode origin to almost top left
                    posEsc[0], posEsc[1], 0, scale, scale);
        canvas.draw(mTexture, Color.WHITE, Assets.getTextureCenterX(mTexture), mTexture.getRegionHeight()-15,
                    posM[0], posM[1], 0, scale, scale);
        canvas.draw(rTexture, Color.WHITE, Assets.getTextureCenterX(rTexture), rTexture.getRegionHeight()-15,
                    posR[0], posR[1], 0, scale, scale);

        // Draw text
        canvas.drawTextCentered("Controls", displayFont, posControlsText[1]);
        canvas.drawText("Move", displayFont, posMoveText[0], posMoveText[1]);
        canvas.drawText("Pause", displayFont, posEscText[0], posEscText[1]);
        canvas.drawText("Whack", displayFont, posWhackText[0], posWhackText[1]);
        canvas.drawText("Pick Up /", displayFont, posPickupText[0], posPickupText[1]);
        canvas.drawText("Throw", displayFont, posPickupText[0], posPickupText[1]-40);  // draw below pick up
        canvas.drawText("Mute", displayFont, posMuteText[0], posMuteText[1]);
        canvas.drawText("Restart Level", displayFont, posRestartText[0], posRestartText[1]);

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
     * Sets the asset location references on resize and start
     */
    @Override
    public void resize(int width, int height) {
        float sx = ((float)width) / STANDARD_WIDTH;
        float sy = ((float)height) / STANDARD_HEIGHT;
        scale = Math.max(sx, sy);
        heightY = height;

        int row1 = height * 10/16;
        int row2 = height * 8/16;
        int row3 = height * 6/16;

        int col1 = width * 4/32;
        int col2 = width * 9/32;
        int col3 = width * 13/32;
        int col4 = width * 21/32;
        int col5 = width * 23/32;

        // Top left corner
        posMenu[0] = width / 16;
        posMenu[1] = height * 14/16;

        // Bottom center
        posResume[0] = width / 2;
        posResume[1] = height / 8;

        // Top center
        posTitle[0] = width / 2;
        posTitle[1] = height * 7/8;
        posControlsText[1] = height * 7/32; // offset from center y

        // Row 1, left to right
        posWASDSprite[0] = col1;
        posWASDSprite[1] = row1;
        posWASD[0] = col2;
        posWASD[1] = row1;
        posMoveText[0] = col3;
        posMoveText[1] = row1;
        posEsc[0] = col4;
        posEsc[1] = row1;
        posEscText[0] = col5;
        posEscText[1] = row1;

        // Row 2, left to right
        posWhackSprite[0] = col1;
        posWhackSprite[1] = row2;
        posMouse[0] = col2;
        posMouse[1] = row2;
        posWhackText[0] = col3;
        posWhackText[1] = row2;
        posM[0] = col4;
        posM[1] = row2;
        posMuteText[0] = col5;
        posMuteText[1] = row2;

        // Row 3, left to right
        posPickupSprite[0] = col1;
        posPickupSprite[1] = row3;
        posSpace[0] = col2;
        posSpace[1] = row3;
        posPickupText[0] = col3;
        posPickupText[1] = row3;
        posR[0] = col4;
        posR[1] = row3;
        posRestartText[0] = col5;
        posRestartText[1] = row3;
    }

    /** Default stubs */

    @Override
    public boolean keyDown(int keycode) {
        if (pressStateMenu == 2 || pressStateResume == 2) return true;
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
        frame = 0f;
    }
}
