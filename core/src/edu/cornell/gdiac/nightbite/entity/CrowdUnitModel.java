package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.nightbite.AIController;
import edu.cornell.gdiac.nightbite.AILattice;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.WorldModel;

import java.util.Random;

public class CrowdUnitModel extends HumanoidModel{

    private static final float WALK_THRUST = 5f;
    private static final int SPAWN_TO_ROAM_RATE = 3;
    private static final int ROAM_RADIUS = 2;

    public AIController aiController;
    public WorldModel worldModel;
    private enum State {
        IDLE,
        ROAM
    }
    public State state;
    public Vector2 targetPos;
    private float previousDistanceFromTarget;

    public CrowdUnitModel(float x, float y, float width, float height, WorldModel worldModel) {
        super(
                x, y, width, height,
                Assets.getFilmStrip("character/Filmstrip/NPC1_Walk_8.png"),
                Assets.getFilmStrip("character/P1_Falling_5.png")
        );
        setPosition(x, y);
        setHomePosition(new Vector2(x, y));

        aiController = new AIController(worldModel, this);
        this.worldModel = worldModel;
        state = State.IDLE;
        targetPos = new Vector2(x, y);
    }

    public Vector2 update() {
        Vector2 dir = new Vector2(0, 0);
        switch (state) {
            case IDLE:
                if (new Random().nextInt(SPAWN_TO_ROAM_RATE) == 0) {
                    targetPos.x = new Random().nextInt(ROAM_RADIUS*2) - ROAM_RADIUS + getHomePosition().x;
                    targetPos.y = new Random().nextInt(ROAM_RADIUS*2) - ROAM_RADIUS + getHomePosition().y;

                    // bounding to screen lmfao
                    targetPos.x = Math.min(worldModel.getWidth(), targetPos.x);
                    targetPos.y = Math.min(worldModel.getHeight(), targetPos.y);
                    targetPos.x = Math.max(0, targetPos.x);
                    targetPos.y = Math.max(0, targetPos.y);

                    dir = move(targetPos, getDimension(), worldModel.getAILattice());
                    state = State.ROAM;
                }
                break;
            case ROAM:
                dir = move(targetPos, getDimension(), worldModel.getAILattice());
                float distanceFromTarget = getPosition().dst(targetPos); // TODO does this actualy work
                if (distanceFromTarget == previousDistanceFromTarget) {
                    previousDistanceFromTarget = 0f;
                    state = State.IDLE;
                    break;
                }
                previousDistanceFromTarget = distanceFromTarget;
                break;
        }
        return dir;
    }

    public Vector2 move(Vector2 targetPos, Vector2 targetDims, AILattice aiLattice) {

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
}
