package edu.cornell.gdiac.nightbite.ball;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public class BallModel extends BoxObstacle {

    /** The density of this ball */
    private static final float DEFAULT_DENSITY  =  1.0f;
    /** The friction of this ball */
    private static final float DEFAULT_FRICTION = 0.1f;
    /** The restitution of this ball */
    private static final float DEFAULT_RESTITUTION = 0.4f;
    /** The thrust factor to convert player input into thrust */
    private static final float DEFAULT_THRUST = 20.0f;
    /** The impulse for the character boost */
    private static final float BOOST_IMP = 30.0f;
    /** The amount to slow the character down */
    private static final float MOTION_DAMPING = 20f;

    /** Cache object for transforming the force according the object angle */
    public Affine2 affineCache = new Affine2();
    /** Cache for internal force calculations */
    private Vector2 forceCache = new Vector2();

    /** The force to apply to this rocket */
    private Vector2 impulse;

    public BallModel(float x, float y, float width, float height) {
        super(x,y,width,height);
        impulse = new Vector2();
        setDensity(DEFAULT_DENSITY);
        setFriction(DEFAULT_FRICTION);
        setRestitution(DEFAULT_RESTITUTION);
        setName("ball");
    }

    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (! ret) { return false; }
        body.setLinearDamping(getDamping());
        body.setFixedRotation(true);
        return true;
    }

    public void applyImpulse() {
        if (!isActive()) {
            return;
        }

        body.applyLinearImpulse(impulse.scl(getThrust()), getPosition(), true);

        // Orient the force with rotation.
        // affineCache.setToRotationRad(getAngle());
        // affineCache.applyTo(force);

        // getBody().applyForce(getForce(), getPosition(), true);
    }

    public Vector2 getImpulse() { return impulse; }

    public void setIX(float value) { impulse.x = value; }

    public void setIY(float value) { impulse.y = value; }

    public float getThrust() { return DEFAULT_THRUST; }

    public float getDamping() { return MOTION_DAMPING; }

    public void setBoost() {
        forceCache.set(0, BOOST_IMP);
        body.applyLinearImpulse(forceCache,getPosition(),true);
    }
}

