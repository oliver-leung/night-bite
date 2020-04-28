package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.*;
import edu.cornell.gdiac.util.PooledList;

import static edu.cornell.gdiac.nightbite.entity.MovableModel.*;

public class TestEnemy extends HumanoidModel {
    private static final int THROW_COOLDOWN = 80;
    private static final int WALK_COOLDONW = 20;
    private static final float THROW_DIST = 3;
    private static final float THROW_FORCE = 5f;
    private static final float WALK_THRUST = 25f;
    private PooledList<GridPoint2> path;
    private AIController aiController;
    private Vector2 walkDir;
    private int throwCooldown;
    private int walkCooldown;

    public TestEnemy(float x, float y, float width, float height, WorldModel world) {
        super(x, y, width, height);
        this.texture = Assets.get("character/P3_64_v2.png");
        setTexture(texture);
        setDensity(MOVABLE_OBJ_DENSITY);
        setFriction(MOVABLE_OBJ_FRICTION);
        setRestitution(MOVABLE_OBJ_RESTITUTION);

        path = new PooledList<>();
        aiController = new AIController(world, this);
        walkDir = new Vector2();
    }


    public void move(Vector2 targetPos, Vector2 targetDims, AILattice aiLattice) {
        body.setLinearVelocity(Vector2.Zero);

        if (walkCooldown > 0) {
            walkCooldown --;
            return;
        }

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
        Vector2 dir = aiController.vectorToNode(getFeetPosition()).cpy().nor();
        System.out.println(dir);
        body.applyLinearImpulse(dir.scl(WALK_THRUST), getPosition(), true);
    }

    public Vector2 attack(Vector2 targetPos) {
        if (throwCooldown > 0) {
            throwCooldown --;
            return null;
        }

        if (aiController.canSee(getPosition(), targetPos)
                && getPosition().sub(targetPos).len() < THROW_DIST) {
            throwCooldown = THROW_COOLDOWN;
            walkCooldown = WALK_COOLDONW;
            return targetPos.cpy().sub(getPosition()).scl(THROW_FORCE);
        }

        return null;
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        aiController.drawDebug(canvas, drawScale);
    }

}
