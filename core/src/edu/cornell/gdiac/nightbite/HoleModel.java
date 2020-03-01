package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;

public class HoleModel extends PolygonObstacle {
    public HoleModel(float[] points, float x, float y) {
        super(points, x, y);
        setBodyType(BodyDef.BodyType.StaticBody);

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
