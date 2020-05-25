package edu.cornell.gdiac.nightbite.entity;


public class WallModel extends ImmovableModel {
    public boolean onlyBottom;
    public WallModel(float x, float y, int rotate, boolean onlyBottom) {
        super(x, y, rotate);
        this.onlyBottom = onlyBottom;
    }

    @Override
    protected void resize(float width, float height) {
        if (onlyBottom) {
            vertices[0] = -width/2.0f;
            vertices[1] = -height/2.0f;
            vertices[2] = -width/2.0f;
            vertices[3] =  0;
            vertices[4] =  width/2.0f;
            vertices[5] =  0;
            vertices[6] =  width/2.0f;
            vertices[7] = -height/2.0f;
            shape.set(vertices);
        } else {
            super.resize(width, height);
        }

    }
}
