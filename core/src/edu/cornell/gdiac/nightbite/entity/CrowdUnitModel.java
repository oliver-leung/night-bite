package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.*;
import edu.cornell.gdiac.util.FilmStrip;

import java.util.Random;

public class CrowdUnitModel extends HumanoidModel{

    private static final float WALK_THRUST = 3f;

    public AIController aiController;
    public Vector2 dir;

    public CrowdUnitModel(float x, float y, float width, float height, String textureFile, WorldModel worldModel) {
        super(
                x, y, width, height,
                Assets.getFilmStrip(textureFile),
                Assets.getFilmStrip("character/P1_Falling_5.png") // TODO
        );
        setPosition(x, y);
        setHomePosition(new Vector2(x, y));

        aiController = new AIController(worldModel, this);
        dir = new Vector2(0, 0);
    }

    public void move(Vector2 targetPos, Vector2 targetDims, AILattice aiLattice) {

        aiController.clearTarget();

        int ty = (int) (targetPos.y + targetDims.y/2);
        int by = (int) (targetPos.y - targetDims.y/2);
        int rx = (int) (targetPos.x + targetDims.x/2);
        int lx = (int) (targetPos.x - targetDims.x/2);

        aiController.addTarget(rx, ty);
        aiController.addTarget(lx, ty);
        aiController.addTarget(rx, by);
        aiController.addTarget(lx, by);

        aiController.updateAI(aiLattice, getFeetPosition());
        dir = aiController.vectorToNode(getFeetPosition()).cpy().nor();
        body.applyLinearImpulse(dir.scl(WALK_THRUST), getPosition(), true);

    }

    @Override
    public void setWalkTexture() {
        if (walkCounter <= 0) {
            walkCounter = 20; // TODO
            int currFrame = ((FilmStrip) texture).getFrame();
            currFrame++;
            if (currFrame == ((FilmStrip) texture).getSize()) {
                ((FilmStrip) texture).setFrame(0);
            } else {
                ((FilmStrip) texture).setFrame(currFrame);
            }
            if (getPrevHoriDir() == 1) {
                texture.flip(true, false);
            }
        } else {
            walkCounter--;
        }
    }

    public void setStaticTexture() {
        ((FilmStrip) texture).setFrame(0);
    }

    public Vector2 getDir() {
        return dir;
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        aiController.drawDebug(canvas, drawScale);
    }
}
