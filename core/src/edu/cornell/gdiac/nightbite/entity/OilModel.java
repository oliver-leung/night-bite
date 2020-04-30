package edu.cornell.gdiac.nightbite.entity;

import edu.cornell.gdiac.nightbite.Assets;
import edu.cornell.gdiac.util.FilmStrip;

public class OilModel extends ImmovableModel {
    private int spillngFrame = 12;
    private FilmStrip defaultTexture;

    public OilModel(float x, float y){
        super(x, y, 0);
        setSensor(true);
        setTexture(Assets.getFilmStrip("item/oil_64_filmstrip.png"));
    }

    public void setTexture(FilmStrip texture) {
        if (defaultTexture == null) { defaultTexture = texture; }
        super.setTexture(texture);
    }

    public void update(float delta) {
        super.update(delta);

        if (spillngFrame == 0) { // Done with spilling animation
            setTexture(Assets.getFilmStrip("item/oiltile_64.png"));
        } else {
            defaultTexture.setFrame(12 - spillngFrame);
            spillngFrame--;
        }
    }
}
