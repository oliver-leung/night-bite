package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.nightbite.entity.HoleModel;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;
import edu.cornell.gdiac.nightbite.entity.ImmovableModel;
import edu.cornell.gdiac.nightbite.entity.PlayerModel;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;
import java.util.List;

public class AIController {
    /** Number of frames between each path replan */
    private static final int REPLAN_TIME = 60;
    /** Half of the distance (in World units) between two parallel rays during raycasting */
    public static final float RAYCAST_OFFSET = 0.5f;
    /** AI will detect whether the player is within this radius */
    private float DETECTION_RADIUS = 5f; // About 5 tiles
    private float CHASE_RADIUS = 8f;

    /** Reference to the World that the AI is in. Needed to determine line of sight and AABB boxes. */
    private World world;
    /** Reference to the enemy that is being controlled */
    private HumanoidModel enemy;

    private PooledList<GridPoint2> target;
    /** Where the AI was during path planning */
    private GridPoint2 positionCache;

    private PooledList<GridPoint2> targetPath;
    private Vector2 walkDirectionCache;

    private Vector2 cache;

    /** Number of frames until the next path replan */
    private int replanCountdown;


    public AIController(WorldModel worldModel, HumanoidModel enemy) {
        this.world = worldModel.getWorld();
        this.enemy = enemy;
        target = new PooledList<>();
        positionCache = new GridPoint2();
        targetPath = new PooledList<>();
        walkDirectionCache = new Vector2();
        cache = new Vector2();
    }

    public void updateAI(AILattice lattice, Vector2 position, int aiClass) {
        if (!replan()) {
            return;
        }

        positionCache.set((int) position.x, (int) position.y);
        lattice.findPath(targetPath, target, positionCache, aiClass);
    }

    public boolean canTarget(Vector2 source, Vector2 target, float dist) {
        return canTarget(source, target, RAYCAST_OFFSET, dist);
    }

    // like canSee but instead of checking if it hits the target,
    // checks if there's an immovable object blocking the ray within dist
    public boolean canTarget(Vector2 source, Vector2 target, float offset, float dist) {
        if (cache.set(source).sub(target).len2() <= 0) {
            return true;
        }
        VisionCallback callback = new VisionCallback();
        Vector2 normal = new Vector2(target).sub(source);
        normal.set(-normal.y, normal.x).nor().scl(offset);

        // Cast two parallel, offset rays
        world.rayCast(callback, new Vector2(source).add(normal), new Vector2(target).add(normal));
        world.rayCast(callback, new Vector2(source).sub(normal), new Vector2(target).sub(normal));

        for (Body body : callback.seenBodies) {
//            System.out.println(callback.seenBodies);
            if (body.getUserData() instanceof ImmovableModel && !(body.getUserData() instanceof HoleModel)
            && body.getPosition().sub(source).len() < dist) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param source Position of source of vision
     * @param target Position of target to look at
     * @return True if the source can see the target
     */
    public boolean canSee(Vector2 source, Vector2 target) {
        return canSee(source, target, RAYCAST_OFFSET);
    }

    public boolean canSee(Vector2 source, Vector2 target, float offset) {
        if (cache.set(source).sub(target).len2() <= 0) {
            return true;
        }
        VisionCallback callback = new VisionCallback();
        Vector2 normal = new Vector2(target).sub(source);
        normal.set(-normal.y, normal.x).nor().scl(offset);

        // Cast two parallel, offset rays
        world.rayCast(callback, new Vector2(source).add(normal), new Vector2(target).add(normal));
        world.rayCast(callback, new Vector2(source).sub(normal), new Vector2(target).sub(normal));

        for (Body body : callback.seenBodies) {
//            System.out.println(callback.seenBodies);
            if (!body.getPosition().equals(target)) {
                return false;
            }
        }
        return true;
    }

    public void drawRays(GameCanvas canvas, Vector2 source, Vector2 target, Color color, Vector2 drawScale) {
        drawRays(canvas, source, target, RAYCAST_OFFSET, color, drawScale);
    }

    public void drawRays(GameCanvas canvas, Vector2 source, Vector2 target, float offset, Color color, Vector2 drawScale) {
        Vector2 normal = new Vector2(target).sub(source);
        normal = new Vector2(-normal.y, normal.x).nor().scl(offset);
        float x1, x2, y1, y2;
        x1 = (source.x + normal.x) * drawScale.x;
        y1 = (source.y + normal.y) * drawScale.y;
        x2 = (target.x + normal.x) * drawScale.x;
        y2 = (target.y + normal.y) * drawScale.y;
        canvas.drawLine(x1, y1, x2, y2, color);
        x1 = (source.x - normal.x) * drawScale.x;
        y1 = (source.y - normal.y) * drawScale.y;
        x2 = (target.x - normal.x) * drawScale.x;
        y2 = (target.y - normal.y) * drawScale.y;
        canvas.drawLine(x1, y1, x2, y2, color);
    }

    static class VisionCallback implements RayCastCallback {
        List<Body> seenBodies = new ArrayList<>();

        // TODO: The current method of ray casting doesn't account for thrown firecrackers, and they momentarily block
        // line of sight between the enemy and the player.
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            // Continue the ray through sensors (which act as "transparent" bodies)
            if (fixture.isSensor()) return 1;
            // Continue the ray through Holes
            if (fixture.getBody().getUserData() instanceof HoleModel) return 1;

            // Stop the ray and record the position of the body with which it impacted
            seenBodies.add(fixture.getBody());
            return 0;
        }
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

    public boolean canChasePlayer() {
        Vector2 pos = enemy.getHomePosition();
        DetectionCallback callback = new DetectionCallback();
        world.QueryAABB(callback, pos.x-CHASE_RADIUS, pos.y-CHASE_RADIUS, pos.x+CHASE_RADIUS, pos.y+CHASE_RADIUS);
        return callback.foundBodies.size() > 0;
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

    public Vector2 vectorToNode(Vector2 feet, AILattice aiLattice, int aiClass) {
        return vectorToNode(feet, aiLattice, aiClass, false);
    }

    public Vector2 vectorToNode(Vector2 feet, AILattice aiLattice, int aiClass, boolean replan) {
        if (targetPath.isEmpty()) {
            if (replan) {
                forceReplan();
                updateAI(aiLattice, feet, aiClass);
                if (targetPath.isEmpty()) {
                    return Vector2.Zero;
                }
            } else {
                return Vector2.Zero;
            }
        }

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
