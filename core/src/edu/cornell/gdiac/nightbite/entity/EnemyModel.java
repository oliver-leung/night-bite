package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.AIController;
import edu.cornell.gdiac.nightbite.AILattice;
import edu.cornell.gdiac.nightbite.GameCanvas;
import edu.cornell.gdiac.nightbite.WorldModel;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

public abstract class EnemyModel extends HumanoidModel {
    private enum State {
        IDLE,
        ATTACK,
        RETURN
    }
    public State state = State.IDLE;

    private float previousDistanceFromHome;

    private PooledList<GridPoint2> path;
    public AIController aiController;
    public WorldModel worldModel;

    private static final int WALK_COOLDOWN = 10;
    private static final float WALK_THRUST = 10f;
    private int walkCooldown;

    public EnemyModel(float x, float y, FilmStrip walk, FilmStrip fall, WorldModel worldModel) {
        super(x, y,0.6f, 1f, walk, fall); // TODO: DONT HARDCODE
        setPosition(x, y);
        setHomePosition(new Vector2(x, y));

        path = new PooledList<>();
        aiController = new AIController(worldModel, this);
        this.worldModel = worldModel;
        walkCooldown = WALK_COOLDOWN;
    }

    public abstract void attack(PlayerModel p);



    public Vector2 update(PlayerModel p) {
        Vector2 homePos = getHomePosition();
        Vector2 dir = new Vector2(0,0);
        switch (state) {
            case IDLE:
                if (aiController.canDetectPlayer()) { // Player is within detection radius - attack
                    state = State.ATTACK;
                    aiController.forceReplan();
                }
                break;
            case ATTACK:
                dir = move(p.getPosition(), p.getDimension(), worldModel.getAILattice());
                attack(p);
                if (!aiController.canDetectPlayer()) { // Player not within detection radius - return to origin
                    state = State.RETURN;
                    aiController.forceReplan();
                }
                break;
            case RETURN: // Go to origin
                dir = move(homePos, p.getDimension(), worldModel.getAILattice());
                float distanceFromHome = getPosition().dst(homePos);
                if (distanceFromHome == previousDistanceFromHome) { // Enemy is back at origin - idle
                    previousDistanceFromHome = 0f; // Reset
                    state = State.IDLE;
                    break;
                } else if (aiController.canDetectPlayer()) { // Player is within detection radius - attack
                    previousDistanceFromHome = 0f; // Reset
                    state = State.ATTACK;
                    aiController.forceReplan();
                    break;
                }
                previousDistanceFromHome = distanceFromHome; // Update info
                break;
            default:
                break;
        }
        return dir;
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

    public void setDetectionRadius(float radius) { aiController.setDetectionRadius(radius); }
}
