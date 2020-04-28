package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.*;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

import static edu.cornell.gdiac.nightbite.entity.MovableModel.*;

public abstract class EnemyModel extends HumanoidModel {
    private enum State {
        IDLE,
        ATTACK,
        RETURN
    }
    public State state = State.IDLE;

    private int MAX_ATTACK_FRAME = 200;
    private int MAX_RETURN_FRAME = 75;
    private int attacking;
    private int returning;

    private PooledList<GridPoint2> path;
    public AIController aiController;
    public WorldModel worldModel;
    private static final float WALK_THRUST = 10f;
    private int walkCooldown;

    public EnemyModel(float x, float y, float width, float height, FilmStrip walk, FilmStrip fall, WorldModel world) {
        super(x, y, width, height, walk, fall);
        setDensity(MOVABLE_OBJ_DENSITY);
        setFriction(MOVABLE_OBJ_FRICTION);
        setRestitution(MOVABLE_OBJ_RESTITUTION);
        setPosition(x, y);
        setHomePosition(new Vector2(x, y));

        path = new PooledList<>();
        aiController = new AIController(world, this);
        worldModel = world;
    }

    public abstract void attack(PlayerModel p);

    public Vector2 update(PlayerModel p) {
        Vector2 targetPos = p.getPosition();
        Vector2 enemyPos = getPosition();
        Vector2 dir = new Vector2(0,0);
        switch (state) {
            case IDLE: // If idle and spots target, chase
                if (aiController.canSee(enemyPos, targetPos)) {
                    state = state.ATTACK;
                    attacking = MAX_ATTACK_FRAME;
                    aiController.forceReplan();
                }
                break;
            case ATTACK:
                attacking--;
                dir = move(p.getPosition(), p.getDimension(), worldModel.getAILattice());
                attack(p);
                if (attacking == 0) { // Timeout in chasing
                    setStateToReturn();
                    aiController.forceReplan();
                }
                break;
            case RETURN: // Go to origin
                dir = move(getHomePosition(), p.getDimension(), worldModel.getAILattice());
                returning--;
                if (returning == 0) { // Timeout in returning, change state to idle
                    state = State.IDLE;
                }
                break;
            default:
                break;
        }
        return dir;
    };

    public void setStateToReturn () {
        state = State.RETURN;
        returning = MAX_RETURN_FRAME;
    }


    public Vector2 move(Vector2 targetPos, Vector2 targetDims, AILattice aiLattice) {
//        body.setLinearVelocity(Vector2.Zero);

        if (walkCooldown > 0) {
            walkCooldown --;
            return new Vector2(0,0);
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
        body.applyLinearImpulse(dir.scl(WALK_THRUST), getPosition(), true);
        return dir;
    }

    public void forceReplan() {
        aiController.forceReplan();
    }

    public void respawn() {
        super.respawn();
        aiController.forceReplan();
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        aiController.drawDebug(canvas, drawScale);
    }

    public void setWalkCooldown(int cooldown) {
        walkCooldown = cooldown;
    }
}
