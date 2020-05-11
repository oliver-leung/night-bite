package edu.cornell.gdiac.nightbite.entity;

import com.badlogic.gdx.graphics.g2d.Sprite;
import edu.cornell.gdiac.nightbite.GameCanvas;

public class HoleModel extends ImmovableModel {
    private Sprite holeEdge;
    public HoleModel(float x, float y, int rotate) {
        super(x, y, rotate);
    }

    // I literally cannot be bothered to do this properly
    // This is for sorting for draw order
    @Override
    public float getBottom() {
        return Float.POSITIVE_INFINITY;
    }

    public void addHoleEdge(Sprite sprite) {
        holeEdge = sprite;
    }

    @Override
    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        if (holeEdge != null) {
            holeEdge.draw(GameCanvas.getInstance().getSpriteBatch());
        }
    }
}
