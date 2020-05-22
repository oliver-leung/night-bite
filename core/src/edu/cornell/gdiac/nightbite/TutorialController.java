package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.util.ExitCodes;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.ScreenListener;

public class TutorialController implements Screen, InputProcessor {

    private GameCanvas canvas;
    private ScreenListener listener;

    private FilmStrip popup;
    private BitmapFont displayFont;
    private int NUM_FRAMES;

    public float screenWidth;
    public float screenHeight;

    private static int STANDARD_WIDTH = 1920;
    private static int STANDARD_HEIGHT = 960;

    private static final float ANIMATION_SPEED = 0.2f;
    private static final float SLOW_ANIMATION_SPEED = 0.05f;
    private static final float MAX_FRAMES = 120f;

    // jank stuff for timer to be slower
    private boolean slowFrames;

    private int POPUP_WIDTH;
    private int POPUP_HEIGHT;

    private boolean active;
    private float frame;
    private float scale;
    private int pressState;

    private TextureRegion overlay;
    private Color tint;

    // TODO jank
    private WorldController game;

    public TutorialController(GameCanvas canvas) {
        overlay = new TextureRegion(new Texture("background/blue/Blue_Hole.png"));
        tint = new Color(1,1,1,0.5f);

        this.canvas = canvas;
        active = true;
        slowFrames = false;
    }

    public void loadContent() {
        displayFont = Assets.getFont();
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
        Gdx.input.setInputProcessor(this);
    }

    private void update() {
        frame += (slowFrames ? SLOW_ANIMATION_SPEED : ANIMATION_SPEED);
        if (frame >= MAX_FRAMES) {
            frame = 0f;
        }
        int frameIdx = ((int) frame) % NUM_FRAMES;
        if (frameIdx > 40) {
            slowFrames = true;
        }
        if (frameIdx == 0) {
            slowFrames = false;
        }
        popup.setFrame(frameIdx);
    }

    public void draw(float delta) {
        game.draw(delta);
//        canvas.clear();
        canvas.begin();
//        canvas.draw(background, Color.WHITE, 0, 0, 0, 0, 0, scale, scale);
        canvas.draw(overlay, tint, 0, 0, 0, 0, 0, screenWidth, screenHeight);
        canvas.drawTextCentered("Press any key to continue!", displayFont, - screenHeight*3/8);
        canvas.draw(popup, Color.WHITE, POPUP_WIDTH/2, POPUP_HEIGHT/2,
                screenWidth/2, screenHeight/2,  0, scale, scale);
        canvas.end();
    }

    public void setLevel(int levelSelectChoiceIndex, WorldController game) {
        this.game = game;
        switch (levelSelectChoiceIndex) {
            case 0:
                POPUP_WIDTH = 640;
                POPUP_HEIGHT = 512;
                popup = Assets.getFilmStrip("tutorial/Tutorial1_FS_full.png", POPUP_WIDTH, POPUP_HEIGHT);
                NUM_FRAMES = popup.getSize();
                break;
            case 1:
                POPUP_WIDTH = 560;
                POPUP_HEIGHT = 384;
                popup = Assets.getFilmStrip("tutorial/PanTutorial_FS_5_v3.png", POPUP_WIDTH, POPUP_HEIGHT);
                NUM_FRAMES = popup.getSize();
                break;
            case 2:
                POPUP_WIDTH = 560;
                POPUP_HEIGHT = 384;
                popup = Assets.getFilmStrip("tutorial/OilTutorial_FS_5.png", POPUP_WIDTH, POPUP_HEIGHT);
                NUM_FRAMES = popup.getSize();
                break;
            case 3:
                POPUP_WIDTH = 560;
                POPUP_HEIGHT = 448;
                popup = Assets.getFilmStrip("tutorial/ThiefTutorial_FS_5.png", POPUP_WIDTH, POPUP_HEIGHT);
                NUM_FRAMES = popup.getSize();
                break;
            default:
                POPUP_WIDTH = 560;
                POPUP_HEIGHT = 384;
                popup = Assets.getFilmStrip("tutorial/Tutorial1_FS_full.png", POPUP_WIDTH, POPUP_HEIGHT);
                NUM_FRAMES = popup.getSize();
                break;
        }
    }


    @Override
    public boolean keyDown(int i) {
        if (pressState == 1 || pressState == 2) return true;
        pressState = 1;
        return true;
    }

    @Override
    public boolean keyUp(int i) {
        if (pressState == 1) {
            pressState = 2;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        return false;
    }

    @Override
    public boolean touchDown(int i, int i1, int i2, int i3) {
        if (pressState == 1 || pressState == 2) return true;
        pressState = 1;
        return true;
    }

    @Override
    public boolean touchUp(int i, int i1, int i2, int i3) {
        if (pressState == 1) {
            pressState = 2;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int i, int i1, int i2) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(int i) {
        return false;
    }

    @Override
    public void show() {
        active = true;
    }

    @Override
    public void render(float delta) {
        if (active) {
            update();
            draw(delta);
        }
        if (pressState == 2 && listener != null) {
            listener.exitScreen(this, ExitCodes.LEVEL);
        }
    }

    @Override
    public void resize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
        float sx = ((float)width) / STANDARD_WIDTH;
        float sy = ((float)height) / STANDARD_HEIGHT;
        scale = Math.max(sx, sy);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        active = false;
    }

    @Override
    public void dispose() {
        pressState = 0;
        frame = 0;
    }
}
