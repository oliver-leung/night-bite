package edu.cornell.gdiac.nightbite.obstacle;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.MechanicManager;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelectMode implements Screen {

    /** Resources */
    private static final String BACKGROUND_FILE = "level_select/Background.png";
    private static final String LEVEL1_TILE_FILE = "level_select/#1.png";
    private static final String LEVEL2_TILE_FILE = "level_select/#2.png";
    private static final String LEVEL3_TILE_FILE = "level_select/#3.png";
    private static final String LEVEL1_STALL_FILE = "level_select/LVL1_Stall.png";
    private static final String LEVEL2_STALL_FILE = "level_select/LVL2_Stall.png";
    private static final String LEVEL3_STALL_FILE = "level_select/LVL3_Stall.png";
    private static final String ARROW_BUTTON_FILE = "level_select/Arrow.png";
    private static final String BACK_BUTTON_FILE = "level_select/Back.png";
    private static final String HEADER_FILE = "level_select/Header.png";
    private static final String PLAYER_FILE = "level_select/Lin_128px.png";

    /** Textures */
    private Texture background;

    /** Scaling factor for when player changes the resolution. */
    private float scale;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH = 1920;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 1080;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** Whether or not this player mode is still active */
    private boolean active;

    /** Flags */
    private boolean startGame;

    // TODO
    private PooledList<Vector2> object_list = new PooledList<>();

    public LevelSelectMode(GameCanvas canvas) {
        this.canvas = canvas;

        background = new Texture(BACKGROUND_FILE);

        active = true;
        startGame = false;
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    private void update(float delta) {
        // TODO only controlled by player one
        MechanicManager manager = MechanicManager.getInstance(object_list);
        manager.update();
        float playerHorizontal = manager.getVelX(0);
        float playerVertical = manager.getVelY(0);
        boolean playerDidThrow = manager.isThrowing(0);

        if (playerDidThrow) {
            startGame = true;
        }
    }

    private void draw() {
        canvas.begin();
        canvas.draw(background, Color.WHITE, 0, 0, 0, 0, 0, scale, scale);
        canvas.end();
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (startGame && listener != null) {
                listener.exitScreen(this, 0);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // TODO
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (Math.min(sx, sy));
    }

    @Override
    public void dispose() {
        // TODO
    }

    @Override
    public void show() {
        active = true;
    }

    @Override
    public void hide() {
        active = false;
    }

    /** auto-generated method stubs */

    @Override
    public void pause() { }

    @Override
    public void resume() { }
}
