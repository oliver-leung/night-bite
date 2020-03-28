package edu.cornell.gdiac.nightbite.entity;

public class WallModel extends ImmovableModel {
    public WallModel(float[] points, float x, float y) {
        super(points, x, y);
    }

    public WallModel(int x, float y, float width, float height) {
        super(x, y, width, height);
    }
}
