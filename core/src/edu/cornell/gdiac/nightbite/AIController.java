package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;
import edu.cornell.gdiac.nightbite.entity.PlayerModel;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;

public class AIController implements RayCastCallback {
    /** Number of frames between each path replan */
    private static final int REPLAN_TIME = 60;
    /** AI will detect whether the player is within this radius */
    private float DETECTION_RADIUS = 5f; // About 5 tiles

    /** Reference to the World that the AI is in. Needed to determine line of sight and AABB boxes. */
    private World world;
    /** Reference to the enemy that is being controlled */
    private HumanoidModel enemy;
    /** During ray cast, the position of the first body that the ray collided with */
    private Vector2 contactPoint;

    private PooledList<GridPoint2> target;
    /** Where the AI was during path planning */
    private GridPoint2 positionCache;

    private PooledList<GridPoint2> targetPath;
    private Vector2 walkDirectionCache;

    /** Number of frames until the next path replan */
    private int replanCountdown;


    public AIController(WorldModel worldModel, HumanoidModel enemy) {
        this.world = worldModel.getWorld();
        this.enemy = enemy;
        target = new PooledList<>();
        positionCache = new GridPoint2();
        targetPath = new PooledList<>();
        walkDirectionCache = new Vector2();
    }

    public void updateAI(AILattice lattice, Vector2 position) {
        if (!replan()) {
            return;
        }

        positionCache.set((int) position.x, (int) position.y);
        lattice.findPath(targetPath, target, positionCache);
    }

    /**
     * @param target Target to look at
     * @return True if the enemy can see the target
     */
    public boolean canSee(Vector2 source, Vector2 target) {
        contactPoint = null;

        world.rayCast(this, source, target);
        if (contactPoint == null) {
            return false;
        }
        return contactPoint.equals(target);
    }

    /**
     * Callback for AABB query of nearby players
     * For use with canDetectPlayer()
     */
    static class DetectionCallback implements QueryCallback {
        ArrayList<Body> foundBodies = new ArrayList<>();

        @Override
        public boolean reportFixture(Fixture fixture) {
            Body body = fixture.getBody();
            if (body.getUserData() instanceof PlayerModel) {
                foundBodies.add(fixture.getBody());
            }
            return true;
        }
    }

    /** Return true if a player is within DETECTION_RADIUS from enemy's origin position */
    public boolean canDetectPlayer() {
        Vector2 pos = enemy.getHomePosition();
        DetectionCallback callback = new DetectionCallback();
        world.QueryAABB(callback, pos.x-DETECTION_RADIUS, pos.y-DETECTION_RADIUS, pos.x+DETECTION_RADIUS, pos.y+DETECTION_RADIUS);
        return callback.foundBodies.size() > 0;
    }

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        // Continue the ray through sensors (which act as "transparent" bodies)
        if (fixture.isSensor()) return 1;

        // Stop the ray and record the position of the body with which it impacted
        contactPoint = fixture.getBody().getPosition();
        return 0;
    }

    private boolean replan() {
        if (replanCountdown > 0) {
            replanCountdown --;
            return false;
        }
        replanCountdown = REPLAN_TIME;
        return true;
    }

    public void clearTarget() {
        target.clear();
    }

    public void setTarget(GridPoint2 t) {
        this.target.clear();
        target.add(new GridPoint2(t.x, t.y));
    }

    public void setTarget(Vector2 t) {
        this.target.clear();
        target.add(new GridPoint2((int) t.x, (int) t.y));
    }

    public void setTarget(Iterable<GridPoint2> targets) {
        target.clear();
        for (GridPoint2 t : targets) {
            target.add(new GridPoint2(t));
        }
    }

    public void setTargetVectors(Iterable<Vector2> targets) {
        target.clear();
        for (Vector2 t : targets) {
            target.add(new GridPoint2((int)t.x, (int)t.y));
        }
    }

    public void setDetectionRadius(float radius) { DETECTION_RADIUS = radius; }

    public void addTarget(GridPoint2 target) {
        target.add(new GridPoint2(target));
    }

    public void addTarget(Vector2 target) {
        this.target.add(new GridPoint2((int) target.x, (int) target.y));
    }

    public void addTarget(int x, int y) {
        target.add(new GridPoint2(x, y));
    }

    public void addTarget(float x, float y) {
        target.add(new GridPoint2((int) x, (int) y));
    }

    public Vector2 getMove(Rectangle feet) {
        return null;
    }

    public void forceReplan() {
        replanCountdown = 0;
    }

    private boolean bounded(float val, float min, float max) {
        return val >= min && val < max;
    }


    public Vector2 vectorToNode(Vector2 feet) {
        if (targetPath.isEmpty()) {
            return Vector2.Zero;
        }

        // for (int i = 0; i < targetPath.size(); i ++) {
        //     if (bounded(feet.x, targetPath.get(i).x + 0.5f - 0.2f, targetPath.get(i).x + 0.5f + 0.2f)
        //             && bounded(feet.y, targetPath.get(i).y + 0.5f - 0.1f, targetPath.get(i).y + 0.5f + 0.1f))
        //     {
        //         System.out.println("yeet");
        //         while (i >= 0) {
        //             targetPath.poll();
        //             i --;
        //         }
        //         return Vector2.Zero;
        //     }
        // }

        if (bounded(feet.x, targetPath.getHead().x + 0.5f - 0.2f, targetPath.getHead().x + 0.5f + 0.2f)
                && bounded(feet.y, targetPath.getHead().y + 0.5f - 0.1f, targetPath.getHead().y + 0.5f + 0.1f))
        {
            targetPath.poll();
            return Vector2.Zero;
        }
        walkDirectionCache.set(targetPath.getHead().x + 0.5f, targetPath.getHead().y + 0.5f).sub(feet);
        return walkDirectionCache;
    }

    public void drawDebug(GameCanvas canvas, Vector2 drawScale) {
        AILattice.drawPath(canvas, targetPath, drawScale, Color.BLUE);
    }
}
