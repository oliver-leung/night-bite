package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.Assets;

public class HomeModel extends ImmovableModel {

    private String team;
    private int score;

    public HomeModel(float x, float y, String team) {
        super(x, y, 0);
        this.team = team;
        score = 0;
        setTexture(Assets.STAND);
    }

    public int getScore() {
        return score;
    }

    public void incrementScore(int increase) {
        score = score + increase;
    }

    public String getTeam() {
        return team;
    }

    public boolean activatePhysics(World world) {
        boolean ret = super.activatePhysics(world);
        if (!ret) {
            return false;
        }
        setSensor(true);
        return true;
    }

}
