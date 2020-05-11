package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.util.ExitCodes;
import edu.cornell.gdiac.util.ScreenListener;

/**
 * Controller for the victory/failure screen
 */
public class LevelEndedController implements Screen, InputProcessor {

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

    /** Whether this is a win screen or not */
    private boolean isWin;
    /** Whether this player mode is still active */
    private boolean active;
    /** The current state of the replay button */
    private int pressStateLeft;
    /** The current state of the next level / level select button */
    private int pressStateRight;

    /** Textures */
    private TextureRegion background;
    private TextureRegion completedTexture;
    private TextureRegion failedTexture;
    private TextureRegion winTexture;
    private TextureRegion loseTexture;
    private TextureRegion replayTexture;
    private TextureRegion nextTexture;      // next level
    private TextureRegion levelsTexture;    // level select

    /** Coordinate variables */
    private int heightY = 0;
    private int[] posHeader = new int[]{0, 0};
    private int[] posGraphic = new int[]{0, 0};
    private int[] posLeft = new int[]{0, 0};
    private int[] posRight = new int[]{0, 0};

    public LevelEndedController(GameCanvas canvas) {
        this.canvas = canvas;
        active = true;
        isWin = false;
    }

    public void loadContent() {
        background = Assets.getTextureRegion("pause/Background.png");  // use bg from pause screen
        completedTexture = Assets.getTextureRegion("level_ended/CompletedHeader.png");
        failedTexture = Assets.getTextureRegion("level_ended/FailedHeader.png");
        winTexture = Assets.getTextureRegion("level_ended/PlayerWinning.png");
        loseTexture = Assets.getTextureRegion("level_ended/PlayerLost.png");
        replayTexture = Assets.getTextureRegion("level_ended/ReplayButton.png");
        nextTexture = Assets.getTextureRegion("level_ended/NextLevelButton.png");
        levelsTexture = Assets.getTextureRegion("level_ended/LevelsButton.png");
    }

    /** Sets this screen to be a win screen or not */
    public void setWinScreen(boolean isWin) {
        this.isWin = isWin;
    }

    /** Sets this controller's screen listener */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
        Gdx.input.setInputProcessor(this);
    }

    /**
     * Draws UI assets
     * Will draw different textures based on whether isWin is set to true or not
     */
    public void draw() {
        canvas.begin();

        Color tintLeft = (pressStateLeft == 1 ? Color.GRAY: Color.WHITE);
        Color tintRight = (pressStateRight == 1 ? Color.GRAY: Color.WHITE);

        canvas.draw(background, Color.WHITE, 0, 0, 0, 0, 0, scale, scale);

        canvas.draw(replayTexture, tintLeft, Assets.getTextureCenterX(replayTexture), Assets.getTextureCenterY(replayTexture),
            posLeft[0], posLeft[1], 0, scale, scale);

        if (isWin) {
            canvas.draw(completedTexture, Color.WHITE, Assets.getTextureCenterX(completedTexture), Assets.getTextureCenterY(completedTexture),
                posHeader[0], posHeader[1], 0, scale, scale);
            canvas.draw(winTexture, Color.WHITE, Assets.getTextureCenterX(winTexture), Assets.getTextureCenterY(winTexture),
                posGraphic[0], posGraphic[1], 0, scale, scale);
            canvas.draw(nextTexture, tintRight, Assets.getTextureCenterX(nextTexture), Assets.getTextureCenterY(nextTexture),
                posRight[0], posRight[1], 0, scale, scale);
        } else {
            canvas.draw(failedTexture, Color.WHITE, Assets.getTextureCenterX(failedTexture), Assets.getTextureCenterY(failedTexture),
                posHeader[0], posHeader[1], 0, scale, scale);
            canvas.draw(loseTexture, Color.WHITE, Assets.getTextureCenterX(loseTexture), Assets.getTextureCenterY(loseTexture),
                posGraphic[0], posGraphic[1], 0, scale, scale);
            canvas.draw(levelsTexture, tintRight, Assets.getTextureCenterX(levelsTexture), Assets.getTextureCenterY(levelsTexture),
                posRight[0], posRight[1], 0, scale, scale);
        }

        canvas.end();
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pressStateRight == 2 || pressStateLeft == 2) return true;

        // flip to match graphics coordinates, origin on bottom left
        screenY = heightY - screenY;

        // change left button press state
        float halfWidthLeft = replayTexture.getRegionWidth() / 2f;
        float halfHeightLeft = replayTexture.getRegionHeight() / 2f;

        boolean inBoundXLeft = screenX >= posLeft[0]-halfWidthLeft && screenX <= posLeft[0]+halfWidthLeft;
        boolean inBoundYLeft = screenY >= posLeft[1]-halfHeightLeft && screenY <= posLeft[1]+halfHeightLeft;

        if (inBoundXLeft && inBoundYLeft) {
            pressStateLeft = 1;
        }

        // change right button press state
        float halfWidthRight = levelsTexture.getRegionWidth() / 2f;
        float halfHeightRight = levelsTexture.getRegionHeight() / 2f;

        boolean inBoundXRight = screenX >= posRight[0]-halfWidthRight && screenX <= posRight[0]+halfWidthRight;
        boolean inBoundYRight = screenY >= posRight[1]-halfHeightRight && screenY <= posRight[1]+halfHeightRight;

        if (inBoundXRight && inBoundYRight) {
            pressStateRight = 1;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pressStateLeft == 1) {
            pressStateLeft = 2;
            return true;
        }
        if (pressStateRight == 1) {
            pressStateRight = 2;
            return true;
        }
        return false;
    }

    @Override
    public void render(float delta) {
        if (active) {
            draw();

            if (pressStateLeft == 2 && listener != null) {
                listener.exitScreen(this, ExitCodes.LEVEL);
                // System.out.println("replay button clicked");

            } else if (pressStateRight == 2 && listener != null) {
                if (isWin) {
                    listener.exitScreen(this, ExitCodes.NEXT);
                }
                else {
                    listener.exitScreen(this, ExitCodes.SELECT);
                }
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        float sx = ((float)width) / STANDARD_WIDTH;
        float sy = ((float)height) / STANDARD_HEIGHT;
        scale = Math.max(sx, sy);
        heightY = height;

        posHeader[0] = width / 2;
        posHeader[1] = height * 7/8;
        posGraphic[0] = width / 2;
        posGraphic[1] = height / 2;
        posLeft[0] = width * 3/16;
        posLeft[1] = height / 8;
        posRight[0] = width * 13/16;
        posRight[1] = height / 8;
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
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void show() {
        active = true;
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
        pressStateLeft = 0;
        pressStateRight = 0;
        active = false;
    }
}
