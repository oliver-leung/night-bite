package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.*;
import edu.cornell.gdiac.util.PooledList;

import static edu.cornell.gdiac.nightbite.entity.MovableModel.*;

public class TestEnemy extends HumanoidModel {
    private PooledList<GridPoint2> path;
    private AIController aiController;

    public TestEnemy(float x, float y, float width, float height, WorldModel world) {
        super(x, y, width, height);
        this.texture = Assets.get("character/P3_64_v2.png");
        setTexture(texture);
        setDensity(MOVABLE_OBJ_DENSITY);
        setFriction(300);
        setRestitution(MOVABLE_OBJ_RESTITUTION * 0.1f);

        path = new PooledList<>();
        aiController = new AIController(world, this);
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
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        aiController.drawDebug(canvas, drawScale);
    }

}
