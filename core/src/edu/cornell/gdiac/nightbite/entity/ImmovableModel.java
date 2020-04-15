package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public abstract class ImmovableModel extends BoxObstacle {
    private static final float DEFAULT_DENSITY = 0f;
    private static final float DEFAULT_FRICTION = 1f;
    private static final float DEFAULT_RESTITUTION = 0f;

    public ImmovableModel(float x, float y, int rotate) {
        // TODO: Changing the width and height does nothing right now.
        // TODO: Oliver please make this use x and y please if you need
        // TODO: me to adjust the canonical scale so that it works better
        // TODO: I can do that ok bye
        super(2 * x + 1f, 2 * y + 1f, 2, 2);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(DEFAULT_DENSITY);
        setFriction(DEFAULT_FRICTION);
        setRestitution(DEFAULT_RESTITUTION);
        setAngle((float) (rotate * Math.PI / -2.0f));
    }

    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setFixedRotation(true);
        return true;
    }
}
