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
        super(x, y, 1, 1);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(DEFAULT_DENSITY);
        setFriction(DEFAULT_FRICTION);
        setRestitution(DEFAULT_RESTITUTION);
        setAngle((float) (rotate * Math.PI / -2.0f));
    }

    public I

    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setFixedRotation(true);
        return true;
    }
}
