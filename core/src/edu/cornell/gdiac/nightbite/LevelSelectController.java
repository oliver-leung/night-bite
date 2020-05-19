package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import edu.cornell.gdiac.util.ExitCodes;
import edu.cornell.gdiac.util.ScreenListener;

public class LevelSelectController implements Screen, InputProcessor {

    /** Textures */
    private TextureRegion background;
    private TextureRegion tile1Texture;
    private TextureRegion tile2Texture;
    private TextureRegion tile3Texture;
    private TextureRegion tile4Texture;
    private TextureRegion tile5Texture;
    private TextureRegion tile6Texture;
    private TextureRegion tile7Texture;
    private TextureRegion tile8Texture;
    private TextureRegion tile9Texture;
    private TextureRegion tile10Texture;
    private TextureRegion tile11Texture;
    private TextureRegion tile12Texture;
    private TextureRegion store1Texture;
    private TextureRegion store2Texture;
    private TextureRegion store3Texture;
    private TextureRegion bokchoiStallTexture;
    private TextureRegion carrotStallTexture;
    private TextureRegion eggStallTexture;
    private TextureRegion fishStallTexture;
    private TextureRegion greenOnionStallTexture;
    private TextureRegion milkStallTexture;
    private TextureRegion arrowTexture;
    private TextureRegion arrowTextureFlipped;
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

    /** Flags */
    private boolean startGame;

    /** For movement ugh */
    private int moveCooldown;
    private int MOVE_COOLDOWN_PERIOD = 15;
    private int prevDir;
    private int levelChoiceindex = 0;

//    private String[] levelJSONList = new String[]{
//            "jsons/01_golden_tutorial_basic.json",
//            "jsons/02_golden_tutorial_firecracker.json",
//            "jsons/03_golden_tutorial_oil.json",
//            "jsons/04_golden_spiral.json",
//            "jsons/05_golden_moat.json",
//            "jsons/06_golden_doublehole.json",
//            "jsons/07_golden_holegrid.json",
//            "jsons/08_golden_hard_nothief.json",
//            "jsons/09_golden_zigzag.json",
//            "jsons/10_golden_diagonalhole.json",
//            "jsons/11_golden_medium.json",
//            "jsons/12_golden_messy.json"
//    };

    private String[] levelJSONList = new String[]{
            "jsons/01_showcase_tutorial_basic.json",
            "jsons/02_showcase_tutorial_firecracker.json",
            "jsons/03_golden_tutorial_oil.json",
            "jsons/04_showcase_spiral.json",
            "jsons/05_showcase_semimoat.json",
            "jsons/06_showcase_doublehole.json",
            "jsons/07_showcase_holegrid.json",
            "jsons/08_showcase_longmoats.json",
            "jsons/09_showcase_zigzag.json",
            "jsons/10_showcase_diagonalhole.json",
            "jsons/11_showcase_medium.json",
            "jsons/12_showcase_messy.json"
    };
    private int[] xposList;

    public LevelSelectController(GameCanvas canvas) {
        this.canvas = canvas;

        active = true;
        startGame = false;
        moveCooldown = 0;
        prevDir = 1;
    }

    public void loadContent() {
        background = Assets.getTextureRegion("level_select/Background.png");

        bokchoiStallTexture = Assets.getTextureRegion("level_select/levelselect_bokchoi.png");
        carrotStallTexture = Assets.getTextureRegion("level_select/levelselect_carrot.png");
        eggStallTexture = Assets.getTextureRegion("level_select/levelselect_egg.png");
        fishStallTexture = Assets.getTextureRegion("level_select/levelselect_fish.png");
        greenOnionStallTexture = Assets.getTextureRegion("level_select/levelselect_greenonion.png");
        milkStallTexture = Assets.getTextureRegion("level_select/levelselect_milk.png");

        tile1Texture = Assets.getTextureRegion("level_select/1.png");
        tile2Texture = Assets.getTextureRegion("level_select/2.png");
        tile3Texture = Assets.getTextureRegion("level_select/3.png");
        tile4Texture = Assets.getTextureRegion("level_select/4.png");
        tile5Texture = Assets.getTextureRegion("level_select/5.png");
        tile6Texture = Assets.getTextureRegion("level_select/6.png");
        tile7Texture = Assets.getTextureRegion("level_select/#7.png");
        tile8Texture = Assets.getTextureRegion("level_select/#8.png");
        tile9Texture = Assets.getTextureRegion("level_select/#9.png");
        tile10Texture = Assets.getTextureRegion("level_select/#10.png");
        tile11Texture = Assets.getTextureRegion("level_select/#11.png");
        tile12Texture = Assets.getTextureRegion("level_select/#12.png");

        store1Texture = Assets.getTextureRegion("level_select/LVL1_Stall.png");
        store2Texture = Assets.getTextureRegion("level_select/LVL2_Stall.png");
        store3Texture = Assets.getTextureRegion("level_select/LVL3_Stall.png");

        arrowTexture = Assets.getTextureRegion("level_select/Arrow.png");
        backTexture = Assets.getTextureRegion("level_select/Back.png");
        headerTexture = Assets.getTextureRegion("level_select/Header.png");
        playerTexture = Assets.getTextureRegion("level_select/Lin_128px.png");


        arrowTexture = Assets.getTextureRegion("level_select/Arrow.png");
        arrowTextureFlipped = Assets.getTextureRegion("level_select/Arrow.png");
        arrowTextureFlipped.flip(true, false);
    }

    public String getSelectedLevelJSON () {
        return levelJSONList[levelChoiceindex];
    }

    public String getItemTheme () {
        String itemName;
        switch(levelChoiceindex) {
            case 0:
            case 6:
                itemName = "bokchoi";
                break;
            case 1:
            case 7:
                itemName = "carrot";
                break;
            case 2:
            case 8:
                itemName = "egg";
                break;
            case 3:
            case 9:
                itemName = "fish";
                break;
            case 4:
            case 10:
                itemName = "greenonion";
                break;
            case 5:
            case 11:
                itemName = "milk";
                break;
            default:
                itemName = "bokchoi";
                break;
        }
        return itemName;
    }

    /** Increment the level selection index if in bounds*/
    public void incrSelectedLevelJSON() {
        if (levelChoiceindex < levelJSONList.length - 1) {
            levelChoiceindex++;
        }
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

    private int getPage() {
        return levelChoiceindex / 3;
    }

    private void draw() {
        canvas.begin();

        canvas.draw(background, Color.WHITE, 0, 30, 0, 0, 0, scale, scale);
        canvas.draw(headerTexture, Color.WHITE, headerTexture.getRegionWidth() / 2.0f, headerTexture.getRegionHeight() / 2.0f, xpos2, yposHeader, 0, scale, scale);
        canvas.draw(backTexture, Color.WHITE, 0, 0, xLeftEnd, yposTop, 0, scale, scale);

        float storeOx = store1Texture.getRegionWidth() / 2.0f - tile1Texture.getRegionWidth() / 2.0f;
        float storeOy = store1Texture.getRegionHeight() / 2.0f - 10;

        if (getPage() == 0) {
            canvas.draw(arrowTexture, Color.WHITE, 0, 0, xRightEnd, yposTile + 5, 0, scale, scale);
            canvas.draw(tile1Texture, Color.WHITE, 0, 0, xpos1, yposTile, 0, scale, scale);
            canvas.draw(tile2Texture, Color.WHITE, 0, 0, xpos2, yposTile, 0, scale, scale);
            canvas.draw(tile3Texture, Color.WHITE, 0, 0, xpos3, yposTile, 0, scale, scale);

            canvas.draw(bokchoiStallTexture, Color.WHITE, storeOx, storeOy, xpos1, yposStall, 0, scale, scale);
            canvas.draw(carrotStallTexture, Color.WHITE, storeOx, storeOy, xpos2, yposStall, 0, scale, scale);
            canvas.draw(eggStallTexture, Color.WHITE, storeOx, storeOy, xpos3, yposStall, 0, scale, scale);
        } else if (getPage() == 1) {
            canvas.draw(arrowTextureFlipped, Color.WHITE, 0, 0, xLeftEnd, yposTile + 5, 0, scale, scale);
            canvas.draw(tile4Texture, Color.WHITE, 0, 0, xpos1, yposTile, 0, scale, scale);
            canvas.draw(tile5Texture, Color.WHITE, 0, 0, xpos2, yposTile, 0, scale, scale);
            canvas.draw(tile6Texture, Color.WHITE, 0, 0, xpos3, yposTile, 0, scale, scale);
            canvas.draw(arrowTexture, Color.WHITE, 0, 0, xRightEnd, yposTile + 5, 0, scale, scale);

            canvas.draw(fishStallTexture, Color.WHITE, storeOx, storeOy, xpos1, yposStall, 0, scale, scale);
            canvas.draw(greenOnionStallTexture, Color.WHITE, storeOx, storeOy, xpos2, yposStall, 0, scale, scale);
            canvas.draw(milkStallTexture, Color.WHITE, storeOx, storeOy, xpos3, yposStall, 0, scale, scale);
        } else if (getPage() == 2) {
            canvas.draw(arrowTextureFlipped, Color.WHITE, 0, 0, xLeftEnd, yposTile + 5, 0, scale, scale);
            canvas.draw(tile7Texture, Color.WHITE, 0, 0, xpos1, yposTile, 0, scale, scale);
            canvas.draw(tile8Texture, Color.WHITE, 0, 0, xpos2, yposTile, 0, scale, scale);
            canvas.draw(tile9Texture, Color.WHITE, 0, 0, xpos3, yposTile, 0, scale, scale);
            canvas.draw(arrowTexture, Color.WHITE, 0, 0, xRightEnd, yposTile + 5, 0, scale, scale);

            canvas.draw(bokchoiStallTexture, Color.WHITE, storeOx, storeOy, xpos1, yposStall, 0, scale, scale);
            canvas.draw(carrotStallTexture, Color.WHITE, storeOx, storeOy, xpos2, yposStall, 0, scale, scale);
            canvas.draw(eggStallTexture, Color.WHITE, storeOx, storeOy, xpos3, yposStall, 0, scale, scale);
        } else if (getPage() == 3) {
            canvas.draw(arrowTextureFlipped, Color.WHITE, 0, 0, xLeftEnd, yposTile + 5, 0, scale, scale);
            canvas.draw(tile10Texture, Color.WHITE, 0, 0, xpos1, yposTile, 0, scale, scale);
            canvas.draw(tile11Texture, Color.WHITE, 0, 0, xpos2, yposTile, 0, scale, scale);
            canvas.draw(tile12Texture, Color.WHITE, 0, 0, xpos3, yposTile, 0, scale, scale);

            canvas.draw(fishStallTexture, Color.WHITE, storeOx, storeOy, xpos1, yposStall, 0, scale, scale);
            canvas.draw(greenOnionStallTexture, Color.WHITE, storeOx, storeOy, xpos2, yposStall, 0, scale, scale);
            canvas.draw(milkStallTexture, Color.WHITE, storeOx, storeOy, xpos3, yposStall, 0, scale, scale);
        }

        canvas.draw(playerTexture, Color.WHITE, playerTexture.getRegionWidth() / 2.0f - tile1Texture.getRegionHeight() / 2.0f, -playerTexture.getRegionHeight() / 4.0f, xposList[(levelChoiceindex % 3)], yposTile, 0, scale, scale);

        canvas.end();
    }

    @Override
    public void render(float delta) {
        if (active) {
            update(delta);
            draw();

            if (startGame && listener != null) {
                listener.exitScreen(this, ExitCodes.LEVEL);
            } else if (pressState == 2 && listener != null) {
                listener.exitScreen(this, ExitCodes.TITLE);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // TODO
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (Math.max(sx, sy));
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
