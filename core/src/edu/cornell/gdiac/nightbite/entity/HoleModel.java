package edu.cornell.gdiac.nightbite.entity;

public class HoleModel extends ImmovableModel {
    public HoleModel(float x, float y, int rotate) {
        super(x, y, rotate);
    }

    // I literally cannot be bothered to do this properly
    // This is for sorting for draw order
    @Override
    public float getBottom() {
        return Float.POSITIVE_INFINITY;
    }
}
