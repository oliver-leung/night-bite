package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;
import edu.cornell.gdiac.nightbite.entity.ImmovableModel;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.nightbite.obstacle.SimpleObstacle;
import edu.cornell.gdiac.util.PooledList;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;

public class AIController implements RayCastCallback {
    /** Reference to the WorldModel that the AI is in. Needed to determine line of sight. */
    private WorldModel worldModel;
    /** Reference to the enemy that is being controlled */
    private HumanoidModel enemy;
    /** During ray cast, the position of the first body that the ray collided with */
    private Vector2 contactPoint;

    public AIController(int w, int h, WorldModel worldModel, HumanoidModel enemy) {
        this.worldModel = worldModel;
        this.enemy = enemy;
    }

    public void updateAI(AILattice lattice) {
        
    }

    public void poll() {

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
}
