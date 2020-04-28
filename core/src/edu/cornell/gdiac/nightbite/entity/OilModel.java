package edu.cornell.gdiac.nightbite.entity;

import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.util.FilmStrip;

public class OilModel extends ImmovableModel {
    private int spillngFrame = 12;
    private FilmStrip defaultTexture = Assets.OIL_SPILLING;

    public OilModel(float x, float y){
        super(x, y, 0);
        setSensor(true);
    }

    public void setTexture(FilmStrip texture) {
        if (defaultTexture == null) { defaultTexture = texture; }
        super.setTexture(texture);
    }

    public void update(float delta) {
        super.update(delta);

        if (spillngFrame == 0) { // Done with spilling animation
            setTexture(Assets.OIL_TILE);
        } else {
            spillngFrame--;
        }
    }
}
