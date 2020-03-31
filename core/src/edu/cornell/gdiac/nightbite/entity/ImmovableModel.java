package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;

public abstract class ImmovableModel extends PolygonObstacle {
    private static final float DEFAULT_DENSITY = 0f;
    private static final float DEFAULT_FRICTION = 1f;
    private static final float DEFAULT_RESTITUTION = 0f;

    public ImmovableModel(float[] points, float x, float y) {
        super(points, x, y);
        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(DEFAULT_DENSITY);
        setFriction(DEFAULT_FRICTION);
        setRestitution(DEFAULT_RESTITUTION);
    }

    public ImmovableModel(float x, float y, float width, float height) {
        super(
                new float[]{
                        x - width / 2, y + height / 2,
                        x + width / 2, y + height / 2,
                        x + width / 2, y - height / 2,
                        x - width / 2, y - height / 2},
                x, y);
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
