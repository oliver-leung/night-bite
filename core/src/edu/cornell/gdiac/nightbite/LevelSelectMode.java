package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    /** Textures */ // TODO refactor asset manager
    private Texture background;
    private Texture tile1Texture;
    private Texture tile2Texture;
    private Texture tile3Texture;
    private Texture store1Texture;
    private Texture store2Texture;
    private Texture store3Texture;
    private Texture arrowTexture;
    private Texture backTexture;
    private Texture headerTexture;
    private TextureRegion playerTexture;

    /** Scaling factor for when player changes the resolution. */
    private float scale;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH = 1920;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 1080;
    /** Positioning stats */
    private int xpos1 = 0;
    private int xpos2 = 0;
    private int xpos3 = 0;
    private int yposStall = 0;
    private int yposTile = 0;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** Whether or not this player mode is still active */
    private boolean active;

    /** Flags */
    private boolean startGame;

    /** For movement ugh */
    private int moveCooldown;
    private int MOVE_COOLDOWN_PERIOD = 15;
    private int prevDir;

    // TODO whack shit
    private PooledList<Vector2> object_list = new PooledList<>();
    private int levelChoiceindex;
    private String[] levelJSONList = new String[] {"jsons/level.json", "jsons/level2.json", "jsons/level3.json"};
    private int[] xposList;

    public LevelSelectMode(GameCanvas canvas) {
        this.canvas = canvas;

        background = new Texture(BACKGROUND_FILE);
        tile1Texture = new Texture(LEVEL1_TILE_FILE);
        tile2Texture = new Texture(LEVEL2_TILE_FILE);
        tile3Texture = new Texture(LEVEL3_TILE_FILE);
        store1Texture = new Texture(LEVEL1_STALL_FILE);
        store2Texture = new Texture(LEVEL2_STALL_FILE);
        store3Texture = new Texture(LEVEL3_STALL_FILE);
        arrowTexture = new Texture(ARROW_BUTTON_FILE);
        backTexture = new Texture(BACK_BUTTON_FILE);
        headerTexture = new Texture(HEADER_FILE);
        playerTexture = new TextureRegion(new Texture(PLAYER_FILE));

        active = true;
        startGame = false;
        levelChoiceindex = 1;
        moveCooldown = 0;
        prevDir = 0;
    }

    public String getSelectedLevelJSON () {
        return levelJSONList[levelChoiceindex];
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    private void update(float delta) {
        // TODO only controlled by player one
        MechanicManager manager = MechanicManager.getInstance(object_list);
        manager.update();
        int playerHorizontal = (int) manager.getVelX(0);
        boolean playerDidThrow = manager.isThrowing(0);

        // move selection
        updateMoveCooldown();
        if (playerHorizontal != 0) {
            if (!((playerHorizontal == -1 && levelChoiceindex == 0) || (playerHorizontal == 1 && levelChoiceindex == (levelJSONList.length-1))) && canMove()) {
                levelChoiceindex += playerHorizontal;
                startMoveCooldown();

                // update player direction
                if (playerHorizontal != prevDir) {
                    playerTexture.flip(true, false);
                }
                prevDir = playerHorizontal;
            }
        }

        // choose selection
        if (playerDidThrow) {
            startGame = true;
        }
    }

    private void startMoveCooldown() {
        moveCooldown = MOVE_COOLDOWN_PERIOD;
    }

    private void updateMoveCooldown() {
        if (moveCooldown > 0) {
            moveCooldown--;
        }
    }

    private boolean canMove() {
        return moveCooldown == 0;
    }

    private void draw() {
        canvas.begin();

        canvas.draw(background, Color.WHITE, 0, 0, 0, 0, 0, scale, scale);
        canvas.draw(tile1Texture, Color.WHITE, 0, 0, xpos1, yposTile, 0, scale, scale);
        canvas.draw(tile2Texture, Color.WHITE, 0, 0, xpos2, yposTile, 0, scale, scale);
        canvas.draw(tile3Texture, Color.WHITE, 0, 0, xpos3, yposTile, 0, scale, scale);

        canvas.draw(store1Texture, Color.WHITE, store1Texture.getWidth()/2.0f - tile1Texture.getWidth()/2.0f, store1Texture.getHeight()/2.0f - 10, xpos1, yposStall, 0, scale, scale);
        canvas.draw(store2Texture, Color.WHITE, store1Texture.getWidth()/2.0f - tile1Texture.getWidth()/2.0f, store1Texture.getHeight()/2.0f - 10, xpos2, yposStall, 0, scale, scale);
        canvas.draw(store3Texture, Color.WHITE, store1Texture.getWidth()/2.0f - tile1Texture.getWidth()/2.0f, store1Texture.getHeight()/2.0f - 10, xpos3, yposStall, 0, scale, scale);

        canvas.draw(playerTexture, Color.WHITE, playerTexture.getRegionWidth()/2.0f - tile1Texture.getWidth()/2.0f, 0, xposList[levelChoiceindex], yposTile, 0, scale, scale);

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
        xpos1 = width/4;
        xpos2 = width/2;
        xpos3 = width * 3/4;
        yposStall = height/2;
        yposTile = height/4;
        xposList = new int[] {xpos1, xpos2, xpos3};
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
