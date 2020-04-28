package edu.cornell.gdiac.nightbite.entity;

import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;

public class OilModel extends ImmovableModel {
    private int id;
    public OilModel(float x, float y, int id){
        super(x, y, 0);
        this.id = id;
        // TODO: setTexture
    }

}
