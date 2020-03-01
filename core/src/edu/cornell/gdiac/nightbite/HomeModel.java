package edu.cornell.gdiac.nightbite;

import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public class HomeModel extends BoxObstacle {
    private String team;

    public HomeModel(float x, float y, float width, float height, String team) {
        super(x, y, width, height);
        this.team = team;
        this.setName("foo");
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        body.setFixedRotation(true);
        setSensor(true);
        return true;
    }
}
