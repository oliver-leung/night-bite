package edu.cornell.gdiac.nightbite.entity;

import edu.cornell.gdiac.nightbite.Assets;

public class HoleModel extends ImmovableModel {
    public HoleModel(float x, float y, int rotate) {
        super(x, y, rotate);
        setTexture(Assets.HOLE);
    }
}
