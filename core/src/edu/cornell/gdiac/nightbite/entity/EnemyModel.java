package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.*;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.PooledList;

public abstract class EnemyModel extends HumanoidModel {
    protected final TextureRegion EXCLAMATION = Assets.getTextureRegion("character/Enemies/exclamation_64.png");

    public EnemyModel(float x, float y, FilmStrip walk, FilmStrip fall, WorldModel worldModel) {
        super(x, y, 0.6f, 1f, walk, fall); // TODO: DONT HARDCODE
        setPosition(x, y);
        setHomePosition(new Vector2(x+0.5f, y+0.5f));  // TODO

        super.DEFAULT_RESPAWN_COOLDOWN = 6 * 60;

        path = new PooledList<>();
        aiController = new AIController(worldModel, this);
        this.worldModel = worldModel;
        walkCooldown = WALK_COOLDOWN;
        setRespawnCooldown(6 * 60);

        aiClass = 1;
    }

    @Override
    public int getAiClass() {
        return aiClass;
    }

    public State state = State.IDLE;

    private float previousDistanceFromHome;

    private PooledList<GridPoint2> path;
    public AIController aiController;
    public WorldModel worldModel;

    protected static final int WALK_COOLDOWN = 10;
    private static final float WALK_THRUST = 7f;
    protected int walkCooldown;

    private static float STOP_DIST = 2;

    public void setStopDist(float stopDist) {
        STOP_DIST = stopDist;
    }

    // Only relevant for thief enemy
    public boolean isDoneAttacking = true;

    public void setIsDoneAttacking(boolean bool) {
        isDoneAttacking = bool;
    }

    protected enum State {
        IDLE,
        ATTACK,
        RETURN
    }

    public abstract Vector2 attack(PlayerModel p, AILattice aiLattice);

    @Override
    public void setWalkTexture() {
        FilmStrip filmStrip = (FilmStrip) this.texture;
        filmStrip.setFrame((walkCounter / 8) % filmStrip.getSize());
        if (prevHoriDir == 1) {
            texture.flip(true, false);
        }
        walkCounter++;
    }

    public Vector2 update(PlayerModel p) {
        Vector2 homePos = getHomePosition();
        Vector2 dir = new Vector2(0, 0);
        switch (state) {
            case IDLE:
                if (aiController.canDetectPlayer()) { // Player is within detection radius - attack
                    state = State.ATTACK;
                    aiController.forceReplan();
                }
                break;
            case ATTACK:
                dir = attack(p, worldModel.getAILattice());
                if (isDoneAttacking && !aiController.canChasePlayer()) { // Player not within chase radius - return to origin
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
                } else if (aiController.canChasePlayer()) { // Player is within chase radius - attack
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

        if (getPosition().sub(targetPos).len() < STOP_DIST &&
                aiController.canTarget(getPosition(), targetPos, STOP_DIST)) {
            return Vector2.Zero;
        }

        if (walkCooldown > 0) {
            walkCooldown --;
            return Vector2.Zero;
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

        aiController.updateAI(aiLattice, getFeetPosition(), getAiClass());
        Vector2 dir = aiController.vectorToNode(getFeetPosition(), aiLattice, getAiClass(),
                getPosition().sub(targetPos).len() < STOP_DIST).cpy().nor();
        body.applyLinearImpulse(dir.scl(WALK_THRUST), getPosition(), true);
        return dir;
    }

    public void forceReplan() {
        aiController.forceReplan();
    }

    public void respawn() {
        super.respawn();
        aiController.forceReplan();
        resetState();
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        aiController.drawDebug(canvas, drawScale);
    }

    public void resetState() {
        state = State.IDLE;
    }

    public void setDetectionRadius(float radius) { aiController.setDetectionRadius(radius); }
}
