package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelectController implements Screen, InputProcessor {

    /** Textures */ // TODO refactor asset manager
    private TextureRegion background;
    private TextureRegion tile1Texture;
    private TextureRegion tile2Texture;
    private TextureRegion tile3Texture;
    private TextureRegion store1Texture;
    private TextureRegion store2Texture;
    private TextureRegion store3Texture;
    private TextureRegion arrowTexture;
    private TextureRegion backTexture;
    private TextureRegion headerTexture;
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

    // TODO: switch to levels made with new JSON schema
    private String[] levelJSONList = new String[]{
            "jsons/level_test.json",
            "jsons/beta_medium.json",
            "jsons/beta_hard.json"};
    private int[] xposList;

    public LevelSelectController(GameCanvas canvas) {
        this.canvas = canvas;

        active = true;
        startGame = false;
        levelChoiceindex = 1;
        moveCooldown = 0;
        prevDir = 1;
    }

    public void loadContent() {
        background = Assets.LEVEL_SELECT_BACKGROUND;
        tile1Texture = Assets.TILE_1_TEXTURE;
        tile2Texture = Assets.TILE_2_TEXTURE;
        tile3Texture = Assets.TILE_3_TEXTURE;
        store1Texture = Assets.STORE_1_TEXTURE;
        store2Texture = Assets.STORE_2_TEXTURE;
        store3Texture = Assets.STORE_3_TEXTURE;
        arrowTexture = Assets.ARROW_TEXTURE;
        backTexture = Assets.BACK_TEXTURE;
        headerTexture = Assets.HEADER_TEXTURE;
        playerTexture = Assets.PLAYER_TEXTURE;
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
        canvas.draw(headerTexture, Color.WHITE, headerTexture.getRegionWidth()/2.0f, headerTexture.getRegionHeight()/2.0f, xpos2, yposHeader, 0, scale, scale);
        canvas.draw(arrowTexture, Color.WHITE, 0, 0, xRightEnd, yposTile+5, 0, scale, scale);
        canvas.draw(backTexture, Color.WHITE, 0, 0, xLeftEnd, yposTop, 0, scale, scale);

        canvas.draw(tile1Texture, Color.WHITE, 0, 0, xpos1, yposTile, 0, scale, scale);
        canvas.draw(tile2Texture, Color.WHITE, 0, 0, xpos2, yposTile, 0, scale, scale);
        canvas.draw(tile3Texture, Color.WHITE, 0, 0, xpos3, yposTile, 0, scale, scale);

        canvas.draw(store1Texture, Color.WHITE, store1Texture.getRegionWidth()/2.0f - tile1Texture.getRegionWidth()/2.0f, store1Texture.getRegionHeight()/2.0f - 10, xpos1, yposStall, 0, scale, scale);
        canvas.draw(store2Texture, Color.WHITE, store1Texture.getRegionWidth()/2.0f - tile1Texture.getRegionWidth()/2.0f, store1Texture.getRegionHeight()/2.0f - 10, xpos2, yposStall, 0, scale, scale);
        canvas.draw(store3Texture, Color.WHITE, store1Texture.getRegionWidth()/2.0f - tile1Texture.getRegionWidth()/2.0f, store1Texture.getRegionHeight()/2.0f - 10, xpos3, yposStall, 0, scale, scale);

        canvas.draw(playerTexture, Color.WHITE, playerTexture.getRegionWidth()/2.0f - tile1Texture.getRegionHeight()/2.0f, -playerTexture.getRegionHeight()/4.0f, xposList[levelChoiceindex], yposTile, 0, scale, scale);

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
        startGame = false;

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

        float radius = backTexture.getRegionWidth()/2.0f;
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
