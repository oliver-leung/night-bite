package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.util.FilmStrip;

public class HomeModel extends ImmovableModel {

    private String team;
    private int score;

    /**
     * Home stall constructor
     * @param player The player number that belongs to this home stall, 1-4 inclusive.
     */
    public HomeModel(float x, float y, String team, int player) {
        super(x, y, 0);
        this.texture = Assets.HOME_STALL;
        setTexture(this.texture);

        this.team = team;
        score = 0;
    }

    public int getScore() {
        return score;
    }

    /**
     * Increments the score and updates the home texture accordingly
     * @param increase Amount to increase score by
     */
    public void incrementScore(int increase) {
        score = score + increase;
        // TODO this is unsafe
        if (score < 4) ((FilmStrip) texture).setFrame(score);

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
