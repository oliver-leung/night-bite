package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.nightbite.Assets;

public class HomeModel extends ImmovableModel {

    private String team;
    private int score;

    /**
     * @param x    X position of the home
     * @param y    Y position of the home
     * @param team Team that the home belongs to
     */
    public HomeModel(float x, float y, String team) {
        super(x, y, 0);
        this.team = team;
        score = 0;
        setTexture(Assets.TEXTURES.get("environment/StallHome1_64.png"));
        setWidth(2);
        setHeight(2);
        setName(team);
    }

    public int getScore() {
        return score;
    }

    public void incrementScore(int increase) {
        score = score + increase;
        //TODO: add animation
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
