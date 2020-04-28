package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;
import edu.cornell.gdiac.nightbite.entity.ImmovableModel;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.PooledList;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;

public class AIController implements RayCastCallback {
    private static final int REPLAN_TIME = 60;

    /** Reference to the WorldModel that the AI is in. Needed to determine line of sight. */
    private WorldModel worldModel;
    /** Reference to the enemy that is being controlled */
    private HumanoidModel enemy;
    /** During ray cast, the position of the first body that the ray collided with */
    private Vector2 contactPoint;

    private PooledList<GridPoint2> target;
    private GridPoint2 positionCache;

    private PooledList<GridPoint2> targetPath;
    private Vector2 walkDirectionCache;

    private int replanCountdown;


    public AIController(WorldModel worldModel, HumanoidModel enemy) {
        this.worldModel = worldModel;
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
    public boolean canSee(HumanoidModel target) {
        contactPoint = null;
        Vector2 myPosition = enemy.getPosition();

        worldModel.getWorld().rayCast(this, myPosition, target.getPosition());
        return contactPoint.equals(myPosition);
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
        target.add(new GridPoint2((int) t.x, (int) t.y));
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
            System.out.println("yeet");
            targetPath.poll();
            return Vector2.Zero;
        }
        System.out.println(targetPath.getHead());
        System.out.println(feet);
        walkDirectionCache.set(targetPath.getHead().x + 0.5f, targetPath.getHead().y + 0.5f).sub(feet);
        return walkDirectionCache;
    }

    public void drawDebug(GameCanvas canvas, Vector2 drawScale) {
        AILattice.drawPath(canvas, targetPath, drawScale, Color.BLUE);
    }
}
