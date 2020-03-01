package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.PolygonObstacle;

public class HomeModel extends PolygonObstacle {
    public HomeModel(float[] points, float x, float y) {
        super(points, x, y);
        setSensor(true);
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
