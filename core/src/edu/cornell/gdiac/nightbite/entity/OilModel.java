package edu.cornell.gdiac.nightbite.entity;

import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.nightbite.obstacle.BoxObstacle;
import edu.cornell.gdiac.nightbite.obstacle.Obstacle;
import edu.cornell.gdiac.util.FilmStrip;

public class OilModel extends ImmovableModel {
    private int id;
    private int spillngFrame = 12;
    private FilmStrip defaultTexture = Assets.OIL_SPILLING;

    public OilModel(float x, float y, int id){
        super(x, y, 0);
        this.id = id;
    }

    /**
     * Set the texture of this firecracker
     */
    public void setTexture(FilmStrip texture) {
        if (defaultTexture == null) { defaultTexture = texture; }
        super.setTexture(texture);
    }

    public void update(float delta) {
        super.update(delta);

        if (spillngFrame == 0) {
            setTexture(Assets.OIL_TILE);
        } else {
            spillngFrame--;
        }
    }
}
