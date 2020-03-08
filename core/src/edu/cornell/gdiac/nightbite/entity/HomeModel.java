package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;

public class HomeModel extends BoxObstacle {
    private String team;

    private int score;

    public int getScore() { return score; }

    public void incrementScore() {
        score++;
    }

    public HomeModel(float x, float y, float width, float height, String team) {
        super(x, y, width, height);
        this.team = team;
        this.setName("foo");
        this.score = 0;
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
