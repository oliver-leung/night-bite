package edu.cornell.gdiac.nightbite.entity;

import edu.cornell.gdiac.nightbite.Assets;

public class WallModel extends ImmovableModel {
    public WallModel(float x, float y, int rotate) {
        super(x, y, rotate);
        setTexture(Assets.WALL);
    }
}
