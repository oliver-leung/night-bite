package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.physics.box2d.World;

public class HomeModel extends ImmovableModel {
    private String team;

    private int score;

    public HomeModel(float x, float y, float width, float height, String team) {
        super(x, y, width, height);
        this.team = team;
        this.score = 0;
    }

    public int getScore() {
        return score;
    }

    public void incrementScore() {
        score++;
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
