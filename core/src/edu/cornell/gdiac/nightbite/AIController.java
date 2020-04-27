package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import edu.cornell.gdiac.nightbite.entity.HumanoidModel;

public class AIController extends MechanicController implements RayCastCallback {
    /** Reference to the WorldModel that the AI is in. Needed to determine line of sight. */
    private WorldModel worldModel;
    /** Reference to the enemy that is being controlled */
    private HumanoidModel enemy;
    /** During ray cast, the position of the first body that the ray collided with */
    private Vector2 contactPoint;

    private int[][] staticMap;
    private int[][] dynamicMap;
    private int numW;
    private int numH;
    private float partX;
    private float partY;

    public AIController(int w, int h, Rectangle bounds, WorldModel worldModel, HumanoidModel enemy) {
        numW = w;
        numH = h;
        partX = bounds.x / numW;
        partY = bounds.y / numH;
        staticMap = new int[h][w];
        dynamicMap = new int[h][w];

        this.worldModel = worldModel;
        this.enemy = enemy;
    }

    public void populateStatic() {

    }

    public void updateAI() {
        // construct lattice
//        for (Vector2 v : objects) {
//            System.out.println(v.x);
//            System.out.println(v.y);
//        }
//        System.out.println("-------");
        // run a star
    }

    public void poll() {

    }

    public void drawDebug(GameCanvas canvas) {
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
