package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelectController implements Screen, InputProcessor {

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
    private static int STANDARD_HEIGHT = 960;
    /** Positioning stats */
    private int xpos1 = 0;
    private int xpos2 = 0;
    private int xpos3 = 0;
    private int xLeftEnd = 0;
    private int xRightEnd = 0;
    private int yposStall = 0;
    private int yposTile = 0;
    private int yposHeader = 0;
    private int yposTop = 0;
    private int heightY = 0;

    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;

    /** Whether or not this player mode is still active */
    private boolean active;

    /** The current state of the play button */
    private int pressState;

    /** Exit code for starting the game */
    public static final int EXIT_START = 0;
    /** Exit code for heading back to game menu */
    public static final int EXIT_MENU = 1;


    /** Flags */
    private boolean startGame;

    /** For movement ugh */
    private int moveCooldown;
    private int MOVE_COOLDOWN_PERIOD = 15;
    private int prevDir;

    // TODO whack shit
    private PooledList<Vector2> object_list = new PooledList<>();
    private int levelChoiceindex;
    private String[] levelJSONList = new String[]{"jsons/level_easy.json", "jsons/level_medium.json", "jsons/level_hard.json"};
    private int[] xposList;

    public LevelSelectController(GameCanvas canvas) {
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
        prevDir = 1;
    }

    public String getSelectedLevelJSON () {
        return levelJSONList[levelChoiceindex];
    }

    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
        Gdx.input.setInputProcessor(this);
    }

    private void update(float delta) {
        // TODO only controlled by player one
        MechanicManager manager = MechanicManager.getInstance();
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

        canvas.draw(background, Color.WHITE, 0, 30, 0, 0, 0, scale, scale);
        canvas.draw(headerTexture, Color.WHITE, headerTexture.getWidth()/2.0f, headerTexture.getHeight()/2.0f, xpos2, yposHeader, 0, scale, scale);
        canvas.draw(arrowTexture, Color.WHITE, 0, 0, xRightEnd, yposTile+5, 0, scale, scale);
        canvas.draw(backTexture, Color.WHITE, 0, 0, xLeftEnd, yposTop, 0, scale, scale);

        canvas.draw(tile1Texture, Color.WHITE, 0, 0, xpos1, yposTile, 0, scale, scale);
        canvas.draw(tile2Texture, Color.WHITE, 0, 0, xpos2, yposTile, 0, scale, scale);
        canvas.draw(tile3Texture, Color.WHITE, 0, 0, xpos3, yposTile, 0, scale, scale);

        canvas.draw(store1Texture, Color.WHITE, store1Texture.getWidth()/2.0f - tile1Texture.getWidth()/2.0f, store1Texture.getHeight()/2.0f - 10, xpos1, yposStall, 0, scale, scale);
        canvas.draw(store2Texture, Color.WHITE, store1Texture.getWidth()/2.0f - tile1Texture.getWidth()/2.0f, store1Texture.getHeight()/2.0f - 10, xpos2, yposStall, 0, scale, scale);
        canvas.draw(store3Texture, Color.WHITE, store1Texture.getWidth()/2.0f - tile1Texture.getWidth()/2.0f, store1Texture.getHeight()/2.0f - 10, xpos3, yposStall, 0, scale, scale);

        canvas.draw(playerTexture, Color.WHITE, playerTexture.getRegionWidth()/2.0f - tile1Texture.getWidth()/2.0f, -playerTexture.getRegionHeight()/4.0f, xposList[levelChoiceindex], yposTile, 0, scale, scale);

        canvas.end();
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (startGame && listener != null) {
                listener.exitScreen(this, EXIT_START);
            } else if (pressState == 2 && listener != null) {
                listener.exitScreen(this, EXIT_MENU);
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
        xLeftEnd = width/16;
        xRightEnd = width * 15/16;
        yposStall = height * 17/32;
        yposTile = height/4;
        yposHeader = height * 13/16;
        yposTop = height * 7/8;
        heightY = height;
        xposList = new int[] {xpos1, xpos2, xpos3};
    }

    @Override
    public void dispose() {
        // TODO
//        background.dispose();
//        tile1Texture.dispose();
//        tile2Texture.dispose();
//        tile3Texture.dispose();
//        store1Texture.dispose();
//        store2Texture.dispose();
//        store3Texture.dispose();
//        arrowTexture.dispose();
//        backTexture.dispose();
//        headerTexture.dispose();

//        Gdx.input.setInputProcessor(null);
        pressState = 0;

//        background = null;
//        tile1Texture= null;
//        tile2Texture= null;
//        tile3Texture= null;
//        store1Texture= null;
//        store2Texture= null;
//        store3Texture= null;
//        arrowTexture= null;
//        backTexture= null;
//        headerTexture= null;
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

    public boolean keyDown(int i) {
        return true;
    }

    public boolean keyUp(int i) {
        return true;
    }

    public boolean keyTyped(char c) {
        return true;
    }

    public boolean touchDown(int screenX, int screenY, int i2, int i3) {
        if (pressState == 2) {
            return true;
        }
        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        float radius = backTexture.getWidth()/2.0f;
        float dist = (screenX-xLeftEnd)*(screenX-xLeftEnd)+(screenY-yposTop)*(screenY-yposTop);
        if (dist < radius*radius) {
            pressState = 1;
        }
        return false;
    }

    public boolean touchUp(int i, int i1, int i2, int i3) {
        if (pressState == 1) {
            pressState = 2;
            return false;
        }
        return true;
    }

    public boolean touchDragged(int i, int i1, int i2) {
        return true;
    }

    public boolean mouseMoved(int i, int i1) {
        return true;
    }

    public boolean scrolled(int i) {
        return true;
    }
}
